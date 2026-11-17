package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.ActivateAbility
import com.wingedsheep.engine.state.components.stack.ChosenTarget
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.trk.cards.PicardLeadingByExample
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.model.EntityId
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Picard, Leading by Example (TRK #29) — {1}{W} 2/2 Legendary Human Officer.
 *
 * "{1}, {T}: Another target creature gets +X/+X until end of turn, where X is Picard's power.
 *  Activate only as a sorcery."
 *
 * X is read once, as the ability resolves: from Picard's last known power if he has left the
 * battlefield, and it doesn't change if his power changes afterward.
 */
class PicardLeadingByExampleScenarioTest : FunSpec({

    val ability = PicardLeadingByExample.activatedAbilities[0].id

    data class Board(val d: GameTestDriver, val me: EntityId, val picard: EntityId, val bears: EntityId)

    fun setup(): Board {
        val d = GameTestDriver()
        d.registerCards(TestCards.all + PicardLeadingByExample)
        d.initMirrorMatch(deck = Deck.of("Mountain" to 40), startingPlayer = 0)
        d.passPriorityUntil(Step.PRECOMBAT_MAIN)
        val me = d.activePlayer!!
        val picard = d.putCreatureOnBattlefield(me, "Picard, Leading by Example")
        d.removeSummoningSickness(picard)
        val bears = d.putCreatureOnBattlefield(me, "Grizzly Bears")
        return Board(d, me, picard, bears)
    }

    fun Board.activateOn(target: EntityId) =
        d.submit(ActivateAbility(me, picard, ability, targets = listOf(ChosenTarget.Permanent(target))))

    fun Board.power(id: EntityId) = d.state.projectedState.getPower(id)
    fun Board.toughness(id: EntityId) = d.state.projectedState.getToughness(id)

    test("another target creature gets +X/+X, where X is Picard's power") {
        val b = setup()
        b.d.giveColorlessMana(b.me, 1)

        b.activateOn(b.bears).error shouldBe null
        b.d.bothPass()

        b.power(b.bears) shouldBe 4
        b.toughness(b.bears) shouldBe 4
    }

    test("Picard can't target himself") {
        val b = setup()
        b.d.giveColorlessMana(b.me, 1)

        b.activateOn(b.picard).error shouldNotBe null
    }

    test("X is locked in: raising Picard's power afterward doesn't change the bonus") {
        val b = setup()
        b.d.giveColorlessMana(b.me, 1)
        b.activateOn(b.bears).error shouldBe null
        b.d.bothPass()

        val growth = b.d.putCardInHand(b.me, "Giant Growth")
        b.d.giveMana(b.me, Color.GREEN, 1)
        b.d.castSpell(b.me, growth, listOf(b.picard)).error shouldBe null
        b.d.bothPass()

        b.power(b.picard) shouldBe 5
        withClue("the Bears keep the +2/+2 they got when the ability resolved") {
            b.power(b.bears) shouldBe 4
        }
    }

    test("if Picard dies in response, X uses his last known power") {
        val b = setup()
        val opp = b.d.getOpponent(b.me)
        b.d.giveColorlessMana(b.me, 1)
        b.activateOn(b.bears).error shouldBe null

        b.d.passPriority(b.me)
        val bolt = b.d.putCardInHand(opp, "Lightning Bolt")
        b.d.giveMana(opp, Color.RED, 1)
        b.d.castSpell(opp, bolt, listOf(b.picard)).error shouldBe null
        b.d.bothPass() // the Bolt resolves and Picard dies
        b.d.findPermanent(b.me, "Picard, Leading by Example") shouldBe null
        b.d.bothPass() // the ability resolves

        b.power(b.bears) shouldBe 4
    }
})
