package com.wingedsheep.ai.engine

import com.wingedsheep.engine.core.ActivateAbility
import com.wingedsheep.engine.core.PassPriority
import com.wingedsheep.engine.core.PlayLand
import com.wingedsheep.engine.registry.CardRegistry
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.state.components.player.LandDropsComponent
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.lea.cards.HillGiant
import com.wingedsheep.mtg.sets.tokens.PredefinedTokens
import com.wingedsheep.sdk.core.Format
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Proves the built-in AI actually *uses* the Momir Basic avatar.
 *
 * The avatar's "{X}, Discard a card" ability is enumerated as a legal action, but the engine
 * defaults `ActivateAbility.xValue` to 0 — at X=0 the avatar looks for a mana-value-0 creature and
 * makes nothing, so a naive AI would always pass it over. The [Strategist] expands the X-cost,
 * no-target ability into concrete X candidates (paying the discard from hand), so simulation can
 * pick an X that actually produces a creature. This test pins that behaviour end to end.
 */
class MomirBasicAiTest : ScenarioTestBase() {

    private val avatar = "Momir Vig, Simic Visionary"

    // The AI runs its own registry/simulator. The Vanguard avatar is a custom card registered
    // outside MtgSetCatalog, so build the registry the same way ScenarioTestBase does (TestCards
    // includes the avatar + the pool creatures + basics).
    private val aiRegistry: CardRegistry = CardRegistry().apply {
        register(TestCards.all)
        register(HillGiant)
        register(PredefinedTokens.allTokens)
    }

    init {
        test("AI activates the Momir avatar with X>=1, discarding to pay") {
            val game = scenario()
                .withPlayers()
                .withFormat(
                    Format.MomirBasic(
                        // A small mana-value-1 creature and a meaty mana-value-5 one, so the AI has
                        // a clearly worthwhile X to flip into.
                        eligibleCreatureNames = listOf("Savannah Lions", "Trample Beast")
                    )
                )
                .withCardInCommandZone(1, avatar)
                .withLandsOnBattlefield(1, "Forest", 5)
                // A real Momir hand is all basic lands — cheap discard fodder (and nothing else to do
                // but ramp), so flipping a creature is the productive play.
                .withCardsInHand(1, "Mountain", 2)
                .build()
            // Land drop already spent, so the activation is the turn's play (with a drop left, the
            // land goes first — see the land-first test below).
            game.state = game.state.updateEntity(game.player1Id) { container ->
                container.with(LandDropsComponent(remaining = 0, maxPerTurn = 1))
            }

            val ai = AIPlayer.create(aiRegistry, game.player1Id)
            val action = ai.chooseAction(game.state)

            val activate = action.shouldBeInstanceOf<ActivateAbility>()
            // It's the avatar's command-zone ability.
            game.state.getEntity(activate.sourceId)?.get<CardComponent>()?.name shouldBe avatar
            // A real X was chosen (not the useless default of 0)...
            activate.xValue.shouldNotBeNull()
            activate.xValue!! shouldBeGreaterThanOrEqual 1
            // ...and the discard cost was paid with one card.
            activate.costPayment?.discardedCards?.size shouldBe 1
        }

        test("AI skips weak early Momir activations on the play") {
            // On the play the first three turns are skipped, so three mana is still too early.
            val game = scenario()
                .withPlayers()
                .withFormat(Format.MomirBasic(eligibleCreatureNames = listOf("Savannah Lions")))
                .withCardInCommandZone(1, avatar)
                .withLandsOnBattlefield(1, "Forest", 3)
                .withCardsInHand(1, "Mountain", 6)
                .withTurnNumber(3)
                .withActivePlayer(1)
                .build()
            game.state = game.state.updateEntity(game.player1Id) { container ->
                container.with(LandDropsComponent(remaining = 0, maxPerTurn = 1))
            }

            val ai = AIPlayer.create(aiRegistry, game.player1Id)
            ai.chooseAction(game.state).shouldBeInstanceOf<PassPriority>()
        }

        test("AI never activates the Momir avatar for X=0 with no mana available") {
            // X=0 is affordable — {X} with X=0 costs nothing — and before the fix the un-expanded
            // action went to simulation carrying the default xValue of 0. Mana value 0 holds no
            // castable creature, so the activation could only ever burn the turn's one activation
            // and a card. Savannah Lions is in the pool purely to prove the avatar is otherwise
            // live: the AI must still decline, because it has no mana for any worthwhile X.
            val game = scenario()
                .withPlayers()
                .withFormat(Format.MomirBasic(eligibleCreatureNames = listOf("Savannah Lions")))
                .withCardInCommandZone(1, avatar)
                .withCardsInHand(1, "Mountain", 6)
                .withTurnNumber(5)
                .withActivePlayer(1)
                .build()
            game.state = game.state.updateEntity(game.player1Id) { container ->
                container.with(LandDropsComponent(remaining = 0, maxPerTurn = 1))
            }

            val ai = AIPlayer.create(aiRegistry, game.player1Id)
            ai.chooseAction(game.state).shouldBeInstanceOf<PassPriority>()
        }

        test("AI aims Momir activations at eight drops once more mana is available") {
            val game = scenario()
                .withPlayers()
                .withFormat(
                    Format.MomirBasic(
                        eligibleCreatureNames = listOf("Akroma, Angel of Wrath", "Teeka's Dragon")
                    )
                )
                .withCardInCommandZone(1, avatar)
                .withLandsOnBattlefield(1, "Forest", 9)
                .withCardsInHand(1, "Mountain", 2)
                .withTurnNumber(17)
                .withActivePlayer(1)
                .build()
            game.state = game.state.updateEntity(game.player1Id) { container ->
                container.with(LandDropsComponent(remaining = 0, maxPerTurn = 1))
            }

            val ai = AIPlayer.create(aiRegistry, game.player1Id)
            val activate = ai.chooseAction(game.state).shouldBeInstanceOf<ActivateAbility>()

            game.state.getEntity(activate.sourceId)?.get<CardComponent>()?.name shouldBe avatar
            activate.xValue shouldBe 8
            activate.costPayment?.discardedCards?.size shouldBe 1
        }
        test("AI plays its land drop before activating the Momir avatar, for one more X") {
            // 2026-10-31 AI-vs-human Momir game, AI turn 9 (and again turns 11 and 13): four lands
            // out, a hand of five basics and the land drop unused, the AI activated for X=4 and
            // only then played the land. Land first makes the same activation an X=5 — one more
            // point of mana value every turn, for the same card.
            val game = scenario()
                .withPlayers()
                .withFormat(
                    Format.MomirBasic(
                        startingLife = 24,
                        eligibleCreatureNames = listOf("Hill Giant", "Force of Nature"),
                    )
                )
                .withCardInCommandZone(1, avatar)
                .withLandsOnBattlefield(1, "Forest", 1)
                .withLandsOnBattlefield(1, "Swamp", 1)
                .withLandsOnBattlefield(1, "Plains", 1)
                .withLandsOnBattlefield(1, "Swamp", 1)
                .withCardsInHand(1, "Island", 3)
                .withCardInHand(1, "Swamp")
                .withCardInHand(1, "Forest")
                .withTurnNumber(9)
                .withActivePlayer(1)
                .build()

            val root = game.state
            for (profile in listOf(AiProfile.CURRENT, AiProfile.LIVE)) {
                game.state = root
                val ai = AIPlayer.create(aiRegistry, game.player1Id, profile)
                val first = ai.chooseAction(game.state)
                first.shouldBeInstanceOf<PlayLand>()

                game.execute(first).error shouldBe null
                val second = ai.chooseAction(game.state)
                val activate = second.shouldBeInstanceOf<ActivateAbility>()
                game.state.getEntity(activate.sourceId)?.get<CardComponent>()?.name shouldBe avatar
                activate.xValue shouldBe 5
            }
        }

        test("AI still activates the Momir avatar when its only card is the land it would play") {
            // Playing the last card as a land would leave nothing to discard, so no activation at
            // all this turn — the X=5 flip now is the better use of the card.
            val game = scenario()
                .withPlayers()
                .withFormat(Format.MomirBasic(eligibleCreatureNames = listOf("Force of Nature")))
                .withCardInCommandZone(1, avatar)
                .withLandsOnBattlefield(1, "Forest", 5)
                .withCardInHand(1, "Island")
                .withTurnNumber(9)
                .withActivePlayer(1)
                .build()

            val ai = AIPlayer.create(aiRegistry, game.player1Id)
            val activate = ai.chooseAction(game.state).shouldBeInstanceOf<ActivateAbility>()
            activate.xValue shouldBe 5
        }
    }
}
