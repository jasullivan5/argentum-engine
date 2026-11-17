package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.AlternativeCostType
import com.wingedsheep.engine.core.CastSpell
import com.wingedsheep.engine.core.Outcome
import com.wingedsheep.engine.core.PaymentStrategy
import com.wingedsheep.engine.legalactions.LegalActionEnumerator
import com.wingedsheep.engine.state.components.battlefield.AttachedToComponent
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.trk.cards.Marooned
import com.wingedsheep.mtg.sets.definitions.trk.cards.OpenCommunications
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.scripting.AdditionalCostPayment
import com.wingedsheep.sdk.model.EntityId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Marooned — "Enchanted permanent doesn't untap during its controller's untap step and can't be
 * beamed up."
 *
 * The beam me up clause is the new part: the enchanted creature leaves every beam me up offer's
 * return pool and a forged payment naming it is rejected, while other creatures still pay.
 */
class MaroonedScenarioTest : FunSpec({

    test("the enchanted creature can't be returned to pay beam me up; another creature can") {
        val d = GameTestDriver()
        d.registerCards(TestCards.all + listOf(Marooned, OpenCommunications))
        d.initMirrorMatch(Deck.of("Island" to 40), skipMulligans = true, startingPlayer = 0)
        d.passPriorityUntil(Step.PRECOMBAT_MAIN)
        val you = d.activePlayer!!

        val marooned = d.putCreatureOnBattlefield(you, "Grizzly Bears")
        val free = d.putCreatureOnBattlefield(you, "Grizzly Bears")
        val aura = d.putPermanentOnBattlefield(you, "Marooned")
        d.addComponent(aura, AttachedToComponent(marooned))
        val communications = d.putCardInGraveyard(you, "Open Communications")

        val offer = LegalActionEnumerator.create(d.cardRegistry).enumerate(d.state, you)
            .single { (it.action as? CastSpell)?.alternativeCostType == AlternativeCostType.BEAM_ME_UP }
        offer.additionalCostInfo?.validBounceTargets.orEmpty() shouldNotContain marooned
        offer.additionalCostInfo?.validBounceTargets.orEmpty() shouldContain free

        d.giveMana(you, Color.BLUE, 3)
        fun beam(returned: EntityId) = d.submit(CastSpell(
            you, communications, useAlternativeCost = true, alternativeCostType = AlternativeCostType.BEAM_ME_UP,
            additionalCostPayment = AdditionalCostPayment(bouncedPermanents = listOf(returned)),
            paymentStrategy = PaymentStrategy.FromPool,
        ))
        beam(marooned).outcome.shouldBeInstanceOf<Outcome.Rejected>()
        beam(free).outcome shouldBe Outcome.Done
        d.getHand(you) shouldContain free
    }
})
