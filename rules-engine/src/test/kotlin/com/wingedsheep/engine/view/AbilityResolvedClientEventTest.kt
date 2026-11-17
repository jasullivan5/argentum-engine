package com.wingedsheep.engine.view

import com.wingedsheep.engine.core.ActivateAbility
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Deck
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

/**
 * An activated or triggered ability resolving reaches clients as [ClientEvent.AbilityResolved],
 * the ability-side counterpart of [ClientEvent.SpellResolved] (issue #3673).
 */
class AbilityResolvedClientEventTest : FunSpec({

    val healer = card("Test Healer") {
        manaCost = "{W}"
        typeLine = "Creature — Cleric"
        power = 1
        toughness = 1
        triggeredAbility {
            trigger = Triggers.self.enters()
            effect = Effects.GainLife(2)
            description = "When this creature enters, you gain 2 life."
        }
        activatedAbility {
            cost = Costs.Tap
            effect = Effects.GainLife(1)
        }
    }

    fun driver(): GameTestDriver {
        val d = GameTestDriver()
        d.registerCards(TestCards.all + healer)
        d.initMirrorMatch(deck = Deck.of("Plains" to 40))
        d.passPriorityUntil(Step.PRECOMBAT_MAIN)
        return d
    }

    test("a resolving triggered ability is reported to both players with its text") {
        val d = driver()
        val me = d.activePlayer!!
        val opponent = d.getOpponent(me)
        d.giveMana(me, Color.WHITE)
        d.castSpell(me, d.putCardInHand(me, healer.name))
        d.bothPass() // the creature resolves; its enters trigger goes on the stack
        val healerId = d.findPermanent(me, healer.name)!!
        val triggerOnStack = d.state.stack.single()

        val resolution = d.bothPass()

        val mine = ClientEventTransformer.transform(resolution.events, me)
            .filterIsInstance<ClientEvent.AbilityResolved>().single()
        mine.sourceId shouldBe healerId
        mine.sourceName shouldBe healer.name
        mine.abilityDescription shouldBe "When this creature enters, you gain 2 life."
        mine.abilityId shouldBe triggerOnStack
        mine.controllerId shouldBe me
        mine.description shouldBe "Your Test Healer's ability resolved"

        val theirs = ClientEventTransformer.transform(resolution.events, opponent)
            .filterIsInstance<ClientEvent.AbilityResolved>().single()
        theirs.isYours shouldBe false
        theirs.description shouldBe "Opponent's Test Healer's ability resolved"
    }

    test("a resolving activated ability is reported once it leaves the stack, not when activated") {
        val d = driver()
        val me = d.activePlayer!!
        val healerId = d.putCreatureOnBattlefield(me, healer.name)
        d.removeSummoningSickness(healerId)

        val activation = d.submitSuccess(ActivateAbility(me, healerId, healer.activatedAbilities.first().id))
        ClientEventTransformer.transform(activation.events, me)
            .filterIsInstance<ClientEvent.AbilityResolved>().shouldBeEmpty()

        val resolution = d.bothPass()
        val resolved = ClientEventTransformer.transform(resolution.events, me)
            .filterIsInstance<ClientEvent.AbilityResolved>()
        resolved shouldHaveSize 1
        resolved.single().sourceName shouldBe healer.name
        resolved.single().abilityDescription shouldBe ""
    }
})
