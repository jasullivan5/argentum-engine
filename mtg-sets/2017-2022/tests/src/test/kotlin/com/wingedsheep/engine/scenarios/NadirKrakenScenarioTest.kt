package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.YesNoDecision
import com.wingedsheep.engine.state.components.battlefield.CountersComponent
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.thb.cards.NadirKraken
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.CounterType
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.model.EntityId
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Nadir Kraken (THB #55) — "Whenever you draw a card, you may pay {1}. If you do, put a +1/+1
 * counter on this creature and create a 1/1 blue Tentacle creature token."
 *
 * Covers the optional payment and the ruling that the Tentacle is still created when the Kraken
 * has left the battlefield before its trigger resolves.
 */
class NadirKrakenScenarioTest : FunSpec({

    val drawOne = card("Test Draw One") {
        manaCost = "{U}"
        colorIdentity = "U"
        typeLine = "Sorcery"
        spell { effect = Effects.DrawCards(1) }
    }

    fun setup(): Triple<GameTestDriver, EntityId, EntityId> {
        val d = GameTestDriver()
        d.registerCards(TestCards.all + listOf(NadirKraken, drawOne))
        d.initMirrorMatch(deck = Deck.of("Island" to 40), startingPlayer = 0)
        d.passPriorityUntil(Step.PRECOMBAT_MAIN)
        val me = d.activePlayer!!
        val kraken = d.putCreatureOnBattlefield(me, "Nadir Kraken")
        return Triple(d, me, kraken)
    }

    fun GameTestDriver.plusOneCounters(id: EntityId): Int =
        state.getEntity(id)?.get<CountersComponent>()?.getCount(CounterType.PLUS_ONE_PLUS_ONE) ?: 0

    fun GameTestDriver.tentacles(player: EntityId): Int =
        getCreatures(player).count { state.getEntity(it)?.get<CardComponent>()?.name?.contains("Tentacle") == true }

    /** Cast the draw spell and let it resolve, leaving the Kraken's draw trigger on the stack. */
    fun GameTestDriver.castDrawSpell(me: EntityId) {
        val spell = putCardInHand(me, "Test Draw One")
        giveMana(me, Color.BLUE, 1)
        castSpell(me, spell).error shouldBe null
        bothPass() // resolve the draw spell — the Kraken triggers
    }

    test("paying {1} puts a +1/+1 counter on the Kraken and creates a Tentacle") {
        val (d, me, kraken) = setup()
        d.giveColorlessMana(me, 1)

        d.castDrawSpell(me)
        d.bothPass() // resolve the draw trigger

        d.pendingDecision.shouldBeInstanceOf<YesNoDecision>()
        d.submitYesNo(me, true).error shouldBe null

        d.plusOneCounters(kraken) shouldBe 1
        d.tentacles(me) shouldBe 1
    }

    test("declining to pay does nothing") {
        val (d, me, kraken) = setup()
        d.giveColorlessMana(me, 1)

        d.castDrawSpell(me)
        d.bothPass()

        d.pendingDecision.shouldBeInstanceOf<YesNoDecision>()
        d.submitYesNo(me, false).error shouldBe null

        d.plusOneCounters(kraken) shouldBe 0
        d.tentacles(me) shouldBe 0
    }

    test("the Tentacle is still created when the Kraken left the battlefield before resolution") {
        val (d, me, kraken) = setup()
        d.giveColorlessMana(me, 1)

        d.castDrawSpell(me)
        d.moveToGraveyard(kraken) // the trigger is already on the stack
        d.bothPass()

        d.pendingDecision.shouldBeInstanceOf<YesNoDecision>()
        d.submitYesNo(me, true).error shouldBe null

        withClue("no counter lands on a Kraken that isn't there, but the Tentacle still arrives") {
            d.tentacles(me) shouldBe 1
        }
    }
})
