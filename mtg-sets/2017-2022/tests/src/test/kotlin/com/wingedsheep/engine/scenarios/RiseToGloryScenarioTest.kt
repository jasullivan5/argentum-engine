package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.CastSpell
import com.wingedsheep.engine.core.ChooseTargetsDecision
import com.wingedsheep.engine.core.TargetsResponse
import com.wingedsheep.engine.state.components.battlefield.AttachedToComponent
import com.wingedsheep.engine.state.components.stack.ChosenTarget
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.lea.cards.HolyStrength
import com.wingedsheep.mtg.sets.definitions.thb.cards.RiseToGlory
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.model.EntityId
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Rise to Glory (THB #225) — "Choose one or both — • Return target creature card from your
 * graveyard to the battlefield. • Return target Aura card from your graveyard to the battlefield."
 *
 * Covers the two rulings that depend on resolution: the modes are performed in order, so the
 * returned Aura may enchant the creature just returned; and an Aura with nothing it can legally
 * enchant stays in the graveyard.
 */
class RiseToGloryScenarioTest : FunSpec({

    fun setup(): Pair<GameTestDriver, EntityId> {
        val d = GameTestDriver()
        d.registerCards(TestCards.all + listOf(RiseToGlory, HolyStrength))
        d.initMirrorMatch(deck = Deck.of("Plains" to 40), startingPlayer = 0)
        d.passPriorityUntil(Step.PRECOMBAT_MAIN)
        return d to d.activePlayer!!
    }

    fun GameTestDriver.castRise(me: EntityId, modes: List<Int>, modeTargets: List<List<ChosenTarget>>) {
        giveMana(me, Color.WHITE, 1)
        giveMana(me, Color.BLACK, 1)
        giveColorlessMana(me, 3)
        val spell = putCardInHand(me, "Rise to Glory")
        submit(
            CastSpell(
                playerId = me,
                cardId = spell,
                targets = modeTargets.flatten(),
                chosenModes = modes,
                modeTargetsOrdered = modeTargets,
            )
        ).error shouldBe null
        bothPass()
    }

    test("both modes — the returned Aura can enchant the creature the first mode returned") {
        val (d, me) = setup()
        val bears = d.putCardInGraveyard(me, "Grizzly Bears")
        val aura = d.putCardInGraveyard(me, "Holy Strength")

        d.castRise(
            me,
            modes = listOf(0, 1),
            modeTargets = listOf(
                listOf(ChosenTarget.Card(bears, me, Zone.GRAVEYARD)),
                listOf(ChosenTarget.Card(aura, me, Zone.GRAVEYARD)),
            ),
        )

        val returnedBears = d.findPermanent(me, "Grizzly Bears")
        returnedBears shouldNotBe null

        val choice = d.pendingDecision as ChooseTargetsDecision
        withClue("the creature returned by the first mode is already a legal host") {
            choice.legalTargets[0]!! shouldContain returnedBears!!
        }
        d.submitDecision(me, TargetsResponse(choice.id, mapOf(0 to listOf(returnedBears!!)))).error shouldBe null

        val holy = d.findPermanent(me, "Holy Strength")
        holy shouldNotBe null
        d.state.getEntity(holy!!)?.get<AttachedToComponent>()?.targetId shouldBe returnedBears
    }

    test("an Aura with no creature to enchant stays in the graveyard") {
        val (d, me) = setup()
        val aura = d.putCardInGraveyard(me, "Holy Strength")

        d.castRise(
            me,
            modes = listOf(1),
            modeTargets = listOf(listOf(ChosenTarget.Card(aura, me, Zone.GRAVEYARD))),
        )

        d.findPermanent(me, "Holy Strength") shouldBe null
        d.getGraveyardCardNames(me) shouldContain "Holy Strength"
    }
})
