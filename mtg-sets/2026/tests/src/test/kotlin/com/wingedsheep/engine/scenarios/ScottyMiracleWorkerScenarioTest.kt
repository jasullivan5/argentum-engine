package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.AlternativeCostType
import com.wingedsheep.engine.core.CastSpell
import com.wingedsheep.engine.core.Outcome
import com.wingedsheep.engine.core.PaymentStrategy
import com.wingedsheep.engine.legalactions.LegalActionEnumerator
import com.wingedsheep.engine.state.components.stack.ChosenTarget
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.trk.cards.OpenCommunications
import com.wingedsheep.mtg.sets.definitions.trk.cards.ScottyMiracleWorker
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
 * Scotty, Miracle Worker — "During your main phase, each card in your graveyard has beam me up. The
 * beam me up cost is equal to that spell's mana cost. Scotty can't be beamed up to cast a spell this
 * way."
 *
 * Pins the main-phase gate (written as `staticAbility { condition = … }`, which the grant scan must
 * resolve), the mana-cost price, the instants-and-sorceries limit, and that Scotty's own exclusion
 * covers only his grant: he can still be returned to pay a printed beam me up.
 */
class ScottyMiracleWorkerScenarioTest : FunSpec({

    fun driver(): GameTestDriver = GameTestDriver().also {
        it.registerCards(TestCards.all + listOf(ScottyMiracleWorker, OpenCommunications))
        it.initMirrorMatch(Deck.of("Island" to 40), skipMulligans = true, startingPlayer = 0)
        it.passPriorityUntil(Step.PRECOMBAT_MAIN)
    }

    fun beamOffers(d: GameTestDriver, player: EntityId) = LegalActionEnumerator.create(d.cardRegistry)
        .enumerate(d.state, player).filter { (it.action as? CastSpell)?.alternativeCostType == AlternativeCostType.BEAM_ME_UP }

    fun beam(d: GameTestDriver, player: EntityId, spell: EntityId, returned: EntityId, source: EntityId?, targets: List<ChosenTarget> = emptyList()) =
        d.submit(CastSpell(player, spell, useAlternativeCost = true, alternativeCostType = AlternativeCostType.BEAM_ME_UP,
            beamMeUpSource = source, additionalCostPayment = AdditionalCostPayment(bouncedPermanents = listOf(returned)),
            paymentStrategy = PaymentStrategy.FromPool, targets = targets))

    test("in your main phase an instant in your graveyard beams up for its mana cost, returning another creature") {
        val d = driver(); val you = d.activePlayer!!; val opponent = d.getOpponent(you)
        val scotty = d.putCreatureOnBattlefield(you, "Scotty, Miracle Worker")
        val bears = d.putCreatureOnBattlefield(you, "Grizzly Bears")
        val bolt = d.putCardInGraveyard(you, "Lightning Bolt")
        d.putCardInGraveyard(you, "Grizzly Bears")

        val offer = beamOffers(d, you).single()
        (offer.action as CastSpell).cardId shouldBe bolt
        offer.manaCostString shouldBe "{R}"
        offer.additionalCostInfo?.validBounceTargets.orEmpty() shouldContain bears
        offer.additionalCostInfo?.validBounceTargets.orEmpty() shouldNotContain scotty

        d.giveMana(you, Color.RED, 1)
        beam(d, you, bolt, scotty, source = scotty, targets = listOf(ChosenTarget.Player(opponent)))
            .outcome.shouldBeInstanceOf<Outcome.Rejected>()
        beam(d, you, bolt, bears, source = scotty, targets = listOf(ChosenTarget.Player(opponent))).outcome shouldBe Outcome.Done
        d.getHand(you) shouldContain bears
        d.bothPass()
        d.getLifeTotal(opponent) shouldBe 17
        d.getExile(you) shouldContain bolt
    }

    test("outside your main phase the grant is off") {
        val d = driver(); val you = d.activePlayer!!
        d.putCreatureOnBattlefield(you, "Scotty, Miracle Worker")
        d.putCreatureOnBattlefield(you, "Grizzly Bears")
        d.putCardInGraveyard(you, "Lightning Bolt")
        d.passPriorityUntil(Step.BEGIN_COMBAT)
        beamOffers(d, you) shouldBe emptyList()
    }

    test("Scotty can still be returned to pay a card's printed beam me up") {
        val d = driver(); val you = d.activePlayer!!
        val scotty = d.putCreatureOnBattlefield(you, "Scotty, Miracle Worker")
        val communications = d.putCardInGraveyard(you, "Open Communications")

        val bySource = beamOffers(d, you).associateBy { (it.action as CastSpell).beamMeUpSource }
        bySource[null]!!.additionalCostInfo?.validBounceTargets.orEmpty() shouldContain scotty
        // Scotty is the only creature, and his own grant can't return him: no granted offer at all.
        bySource.containsKey(scotty) shouldBe false

        d.giveMana(you, Color.BLUE, 3)
        beam(d, you, communications, scotty, source = null).outcome shouldBe Outcome.Done
        d.getHand(you) shouldContain scotty
    }
})
