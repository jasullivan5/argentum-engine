package com.wingedsheep.ai.engine

import com.wingedsheep.engine.core.CastSpell
import com.wingedsheep.engine.core.PlayLand
import com.wingedsheep.engine.registry.CardRegistry
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.bfz.cards.EndlessOne
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.model.Deck
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * A land drop is scored by the bigger X it gives an X spell that was already castable —
 * [AiProfile.landDropUnlocksSpells], X-cost half.
 *
 * 2026-10-31 AI-vs-human Momir games: the AI activated the Momir avatar for X=4 and only then
 * played its land, a point of X short every turn. The same one-ply blind spot holds for any X
 * spell: before the land, `{X}` already casts for X=3, so the land "unlocks" nothing and scores as
 * a land. Land first casts the same card one bigger.
 */
class LandDropRaisesXAiTest : FunSpec({

    val cards = TestCards.all + listOf(EndlessOne)
    val registry = CardRegistry().apply { register(cards) }

    for (run in 1..2) {
        test("plays the land before casting Endless One, for X=4 rather than X=3 (run $run)") {
            val d = GameTestDriver()
            d.registerCards(cards)
            d.initMirrorMatch(deck = Deck.of("Forest" to 40), skipMulligans = true, startingPlayer = 0)
            d.passPriorityUntil(Step.PRECOMBAT_MAIN)
            val me = d.player1
            d.state.getZone(me, Zone.HAND).forEach { d.moveToGraveyard(it) }
            repeat(3) { d.putLandOnBattlefield(me, "Forest") }
            listOf("Endless One", "Forest").forEach { d.putCardInHand(me, it) }

            val ai = AIPlayer.create(registry, me, AiProfile.LIVE)

            val first = ai.chooseAction(d.state)
            first.shouldBeInstanceOf<PlayLand>()
            d.submit(first)

            val second = ai.chooseAction(d.state).shouldBeInstanceOf<CastSpell>()
            d.state.getEntity(second.cardId)?.get<CardComponent>()?.name shouldBe "Endless One"
            second.xValue shouldBe 4
        }
    }
})
