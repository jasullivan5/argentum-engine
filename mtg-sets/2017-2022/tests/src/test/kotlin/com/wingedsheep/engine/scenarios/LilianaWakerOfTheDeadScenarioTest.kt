package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.ActivateAbility
import com.wingedsheep.engine.core.SelectCardsDecision
import com.wingedsheep.engine.state.components.battlefield.CountersComponent
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.mtg.sets.definitions.m21.cards.LilianaWakerOfTheDead
import com.wingedsheep.sdk.core.CounterType
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.EntityId
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Liliana, Waker of the Dead (M21 #108) — the +1, "Each player discards a card. Each opponent who
 * can't loses 3 life."
 *
 * Per its ruling, each player chooses a card face down in APNAP order and all the chosen cards are
 * discarded at once; only then does each opponent who couldn't discard lose 3 life. The pipeline
 * records the empty-handed opponents up front, collects every player's choice, and discards them in
 * one move — these tests pin that nothing leaves a hand before every player has chosen, and that the
 * life loss hits only opponents who had nothing to discard.
 */
class LilianaWakerOfTheDeadScenarioTest : ScenarioTestBase() {

    private val plusOne = LilianaWakerOfTheDead.activatedAbilities[0].id

    init {
        context("Liliana, Waker of the Dead +1") {

            test("each player chooses before any card is discarded, then all discard together") {
                val game = scenario()
                    .withPlayers("Player", "Opponent")
                    .withCardOnBattlefield(1, "Liliana, Waker of the Dead")
                    .withCardInHand(1, "Grizzly Bears")
                    .withCardInHand(1, "Hill Giant")
                    .withCardInHand(2, "Grizzly Bears")
                    .withCardInHand(2, "Hill Giant")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()
                val liliana = game.findPermanent("Liliana, Waker of the Dead")!!
                seedLoyalty(game, liliana, 4)

                game.execute(
                    ActivateAbility(playerId = game.player1Id, sourceId = liliana, abilityId = plusOne)
                ).error shouldBe null
                game.resolveStack()

                val first = game.getPendingDecision().shouldBeInstanceOf<SelectCardsDecision>()
                withClue("the active player chooses first") { first.playerId shouldBe game.player1Id }
                game.selectCards(listOf(game.findCardsInHand(1, "Hill Giant").single())).error shouldBe null

                val second = game.getPendingDecision().shouldBeInstanceOf<SelectCardsDecision>()
                withClue("then the opponent") { second.playerId shouldBe game.player2Id }
                withClue("the active player's choice hasn't been discarded yet") {
                    game.handSize(1) shouldBe 2
                    game.graveyardSize(1) shouldBe 0
                }
                game.selectCards(listOf(game.findCardsInHand(2, "Grizzly Bears").single())).error shouldBe null
                game.resolveStack()

                withClue("both chosen cards were discarded") {
                    game.isInGraveyard(1, "Hill Giant") shouldBe true
                    game.isInGraveyard(2, "Grizzly Bears") shouldBe true
                    game.handSize(1) shouldBe 1
                    game.handSize(2) shouldBe 1
                }
                withClue("an opponent who could discard loses no life") { game.getLifeTotal(2) shouldBe 20 }
                loyalty(game, liliana) shouldBe 5
            }

            test("an opponent with an empty hand loses 3 life") {
                val game = scenario()
                    .withPlayers("Player", "Opponent")
                    .withCardOnBattlefield(1, "Liliana, Waker of the Dead")
                    .withCardInHand(1, "Grizzly Bears")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()
                val liliana = game.findPermanent("Liliana, Waker of the Dead")!!
                seedLoyalty(game, liliana, 4)

                game.execute(
                    ActivateAbility(playerId = game.player1Id, sourceId = liliana, abilityId = plusOne)
                ).error shouldBe null
                answerAllDiscards(game)

                game.isInGraveyard(1, "Grizzly Bears") shouldBe true
                game.getLifeTotal(2) shouldBe 17
                withClue("the controller never loses life") { game.getLifeTotal(1) shouldBe 20 }
            }

            test("an empty-handed controller just discards nothing and loses no life") {
                val game = scenario()
                    .withPlayers("Player", "Opponent")
                    .withCardOnBattlefield(1, "Liliana, Waker of the Dead")
                    .withCardInHand(2, "Grizzly Bears")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()
                val liliana = game.findPermanent("Liliana, Waker of the Dead")!!
                seedLoyalty(game, liliana, 4)

                game.execute(
                    ActivateAbility(playerId = game.player1Id, sourceId = liliana, abilityId = plusOne)
                ).error shouldBe null
                answerAllDiscards(game)

                game.isInGraveyard(2, "Grizzly Bears") shouldBe true
                game.getLifeTotal(1) shouldBe 20
                game.getLifeTotal(2) shouldBe 20
            }
        }
    }

    /** Resolves the stack, answering every discard choice with the first card offered. */
    private fun answerAllDiscards(game: TestGame) {
        game.resolveStack()
        var guard = 0
        while (game.getPendingDecision() != null && guard++ < 10) {
            val decision = game.getPendingDecision().shouldBeInstanceOf<SelectCardsDecision>()
            game.selectCards(decision.options.take(1)).error shouldBe null
            game.resolveStack()
        }
    }

    /** The scenario builder doesn't run the "enters with starting loyalty" step, so seed it. */
    private fun seedLoyalty(game: TestGame, id: EntityId, amount: Int) {
        game.state = game.state.updateEntity(id) { c ->
            c.with(CountersComponent().withAdded(CounterType.LOYALTY, amount))
        }
    }

    private fun loyalty(game: TestGame, id: EntityId): Int =
        game.state.getEntity(id)?.get<CountersComponent>()?.getCount(CounterType.LOYALTY) ?: 0
}
