package com.wingedsheep.engine.legalactions

import com.wingedsheep.sdk.model.EntityId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * The meaningful-action rules, exercised directly.
 *
 * [MeaningfulActionFilter.isMeaningful] is a pure function of an action's shape, so it needs no
 * game state — which is exactly why it is worth pinning here rather than only through the
 * game-server's much heavier `AutoPassManagerTest`. The whole-window rules that *do* need state are
 * covered by that suite (it drives `AutoPassManager`, which delegates here) and by
 * `AutoPassParityTest` in `:ai`.
 */
class MeaningfulActionFilterTest : FunSpec({

    fun action(
        type: String,
        affordable: Boolean = true,
        manaAbility: Boolean = false,
        costType: String? = null,
        requiresTargets: Boolean = false,
        validTargets: List<EntityId>? = null,
        validAttackers: List<EntityId>? = null,
        validBlockers: List<EntityId>? = null,
        unfillableRequirement: Boolean = false,
        minTargets: Int = 1,
    ) = object : PriorityAction {
        override val actionType = type
        override val requiresTargets = requiresTargets
        override val validTargets = validTargets
        override val minTargets = minTargets
        override val validAttackers = validAttackers
        override val validBlockers = validBlockers
        override val isManaAbility = manaAbility
        override val holdPriority = false
        override val isAffordableAction = affordable
        override val additionalCostType = costType
        override val hasUnfillableTargetRequirement = unfillableRequirement
    }

    val someone = EntityId("permanent-1")

    test("passing priority is never a candidate") {
        MeaningfulActionFilter.isMeaningful(action("PassPriority")) shouldBe false
    }

    test("a plain mana ability is invisible, but one with a sacrifice cost is not") {
        MeaningfulActionFilter.isMeaningful(action("ActivateAbility", manaAbility = true)) shouldBe false
        MeaningfulActionFilter.isMeaningful(
            action("ActivateAbility", manaAbility = true, costType = "SacrificePermanent")
        ) shouldBe true
    }

    test("a targeted spell with no legal target is not a candidate") {
        // The Phase 1 arena measured 889 of 945 rejected AI actions as exactly this shape:
        // `CastSpell: No valid targets available`, roughly one per game.
        MeaningfulActionFilter.isMeaningful(
            action("CastSpell", requiresTargets = true, validTargets = emptyList())
        ) shouldBe false
        MeaningfulActionFilter.isMeaningful(
            action("CastSpell", requiresTargets = true, validTargets = listOf(someone))
        ) shouldBe true
    }

    test("an up-to-N spell with no legal target is still a candidate — it is cast choosing none") {
        // Tam's Resistance ("put a +1/+1 counter on up to one target creature … Empower Jace 4")
        // on a board with no creatures: castable, and its empower is the whole point.
        MeaningfulActionFilter.isMeaningful(
            action("CastSpell", requiresTargets = true, validTargets = emptyList(), minTargets = 0)
        ) shouldBe true
    }

    test("a multi-requirement spell with an unfillable mandatory slot is not a candidate") {
        // The shape the flat `validTargets` field cannot see: "destroy target creature and target
        // artifact" with a legal creature but no artifact on the board. `validTargets` mirrors
        // requirement 0 and looks perfectly castable, and the engine then rejects the cast.
        MeaningfulActionFilter.isMeaningful(
            action(
                "CastSpell", requiresTargets = true,
                validTargets = listOf(someone), unfillableRequirement = true,
            )
        ) shouldBe false
    }

    test("an unaffordable cast, ability, cycle, crew, suspend or saddle is not a candidate") {
        // An activated ability the player can't pay for ("{2}: +1/+0" with no untapped mana) is
        // enumerated greyed out for the client, but must not stop auto-pass (issue #3672).
        listOf(
            "CastSpell", "CastWithFlashback", "ActivateAbility", "CycleCard", "ForetellCard",
            "CrewVehicle", "SuspendCardFromHand", "SaddleMount",
        ).forEach { type ->
            MeaningfulActionFilter.isMeaningful(action(type, affordable = false)) shouldBe false
            MeaningfulActionFilter.isMeaningful(action(type, affordable = true)) shouldBe true
        }
    }

    test("every cast action type is recognised as a spell cast") {
        // Missing one here is the bug that makes the client speed past a window where a card with
        // an alternative cost is legal — Sneak is only ever a CastWithAlternativeCost.
        MeaningfulActionFilter.SPELL_CAST_ACTION_TYPES.forEach { type ->
            MeaningfulActionFilter.isMeaningful(action(type, affordable = false)) shouldBe false
        }
        (MeaningfulActionFilter.SPELL_CAST_ACTION_TYPES - MeaningfulActionFilter.INSTANT_RESPONSE_ACTION_TYPES)
            .shouldBe(emptySet())
    }

    test("a combat declaration is meaningful only when there is something to declare") {
        MeaningfulActionFilter.isMeaningful(action("DeclareAttackers", validAttackers = emptyList())) shouldBe false
        MeaningfulActionFilter.isMeaningful(action("DeclareAttackers", validAttackers = listOf(someone))) shouldBe true
        MeaningfulActionFilter.isMeaningful(action("DeclareBlockers", validBlockers = emptyList())) shouldBe false
        MeaningfulActionFilter.isMeaningful(action("DeclareBlockers", validBlockers = listOf(someone))) shouldBe true
    }

    test("a land drop is always meaningful") {
        MeaningfulActionFilter.isMeaningful(action("PlayLand")) shouldBe true
    }

    test("filterMeaningful preserves the caller's own action type and order") {
        val actions = listOf(
            action("PassPriority"),
            action("PlayLand"),
            action("CastSpell", affordable = false),
            action("ActivateAbility"),
        )
        MeaningfulActionFilter.filterMeaningful(actions).map { it.actionType } shouldBe
            listOf("PlayLand", "ActivateAbility")
    }
})
