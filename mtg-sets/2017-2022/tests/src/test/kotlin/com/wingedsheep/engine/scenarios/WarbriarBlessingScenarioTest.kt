package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.ChooseTargetsDecision
import com.wingedsheep.engine.core.TargetsResponse
import com.wingedsheep.engine.state.components.battlefield.DamageComponent
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.thb.cards.WarbriarBlessing
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Warbriar Blessing (THB #204) — "Enchant creature you control. When this Aura enters, enchanted
 * creature fights up to one target creature you don't control. Enchanted creature gets +0/+2."
 *
 * Covers the fight on entry and the ruling that, if the Aura leaves before its trigger resolves,
 * the creature it last enchanted still fights.
 */
class WarbriarBlessingScenarioTest : FunSpec({

    val shatterEnchantment = card("Test Enchantment Removal") {
        manaCost = "{G}"
        colorIdentity = "G"
        typeLine = "Instant"
        spell {
            val t = target(TargetFilter(GameObjectFilter.Enchantment))
            effect = Effects.Destroy(t)
        }
    }

    data class Board(val d: GameTestDriver, val me: EntityId, val courser: EntityId, val bears: EntityId)

    fun setup(): Board {
        val d = GameTestDriver()
        d.registerCards(TestCards.all + listOf(WarbriarBlessing, shatterEnchantment))
        d.initMirrorMatch(deck = Deck.of("Forest" to 40), startingPlayer = 0)
        d.passPriorityUntil(Step.PRECOMBAT_MAIN)
        val me = d.activePlayer!!
        val courser = d.putCreatureOnBattlefield(me, "Centaur Courser") // 3/3
        val bears = d.putCreatureOnBattlefield(d.getOpponent(me), "Grizzly Bears") // 2/2
        return Board(d, me, courser, bears)
    }

    /** Cast the Aura on the Courser and let it resolve; its enters trigger asks for a target. */
    fun Board.castBlessingTargeting(fightTarget: EntityId?) {
        d.giveMana(me, Color.GREEN, 2)
        val aura = d.putCardInHand(me, "Warbriar Blessing")
        d.castSpell(me, aura, listOf(courser)).error shouldBe null
        d.bothPass() // the Aura resolves and enters

        val choice = d.pendingDecision as ChooseTargetsDecision
        val chosen = if (fightTarget == null) emptyList() else listOf(fightTarget)
        d.submitDecision(me, TargetsResponse(choice.id, mapOf(0 to chosen))).error shouldBe null
    }

    fun GameTestDriver.damageOn(id: EntityId): Int = state.getEntity(id)?.get<DamageComponent>()?.amount ?: 0

    test("the enchanted creature fights the target creature you don't control") {
        val b = setup()
        b.castBlessingTargeting(b.bears)
        b.d.bothPass() // resolve the fight trigger

        b.d.findPermanent(b.d.getOpponent(b.me), "Grizzly Bears") shouldBe null
        b.d.damageOn(b.courser) shouldBe 2
        b.d.findPermanent(b.me, "Centaur Courser") shouldNotBe null
    }

    test("choosing no target means no fight") {
        val b = setup()
        b.castBlessingTargeting(null)
        b.d.bothPass()

        b.d.findPermanent(b.d.getOpponent(b.me), "Grizzly Bears") shouldNotBe null
        b.d.damageOn(b.courser) shouldBe 0
    }

    test("if the Aura is destroyed before the trigger resolves, the creature it last enchanted still fights") {
        val b = setup()
        b.castBlessingTargeting(b.bears)

        val aura = b.d.findPermanent(b.me, "Warbriar Blessing")!!
        b.d.giveMana(b.me, Color.GREEN, 1)
        val removal = b.d.putCardInHand(b.me, "Test Enchantment Removal")
        b.d.castSpell(b.me, removal, listOf(aura)).error shouldBe null
        b.d.bothPass() // the removal resolves first
        b.d.findPermanent(b.me, "Warbriar Blessing") shouldBe null

        b.d.bothPass() // now the fight trigger resolves

        withClue("the Courser, last enchanted by the Aura, fights the Bears") {
            b.d.findPermanent(b.d.getOpponent(b.me), "Grizzly Bears") shouldBe null
            b.d.damageOn(b.courser) shouldBe 2
        }
    }
})
