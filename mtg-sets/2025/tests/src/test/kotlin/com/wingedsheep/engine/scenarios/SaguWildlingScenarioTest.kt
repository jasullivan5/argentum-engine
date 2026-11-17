package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.CastSpell
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

/** Scenario tests for Sagu Wildling. */
class SaguWildlingScenarioTest : ScenarioTestBase() {

    init {
        context("Sagu Wildling") {
            test("ETB gains the controller 3 life") {
                val game = scenario()
                    .withPlayers("Player", "Opponent")
                    .withCardInHand(1, "Sagu Wildling")
                    .withLandsOnBattlefield(1, "Forest", 5)
                    .withLifeTotal(1, 20)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val cast = game.castSpell(1, "Sagu Wildling")
                withClue("Casting Sagu Wildling should succeed: ${cast.error}") {
                    cast.error shouldBe null
                }
                game.resolveStack()

                withClue("Sagu Wildling should be on the battlefield") {
                    game.isOnBattlefield("Sagu Wildling") shouldBe true
                }
                withClue("Controller should have gained 3 life (20 -> 23)") {
                    game.getLifeTotal(1) shouldBe 23
                }
            }

            test("Roost Seek fetches a basic land, then shuffles the card into the library, not exile") {
                val game = scenario()
                    .withPlayers("Player", "Opponent")
                    .withCardInHand(1, "Sagu Wildling")
                    .withLandsOnBattlefield(1, "Forest", 1)
                    .withCardInLibrary(1, "Forest")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val cardId = game.state.getHand(game.player1Id).first {
                    game.state.getEntity(it)?.get<CardComponent>()?.name == "Sagu Wildling"
                }
                val cast = game.execute(CastSpell(playerId = game.player1Id, cardId = cardId, faceIndex = 0))
                withClue("Casting Roost Seek should succeed: ${cast.error}") {
                    cast.error shouldBe null
                }
                game.resolveStack()
                if (game.hasPendingDecision()) {
                    game.selectCards(game.findCardsInLibrary(1, "Forest"))
                    game.resolveStack()
                }

                withClue("The basic Forest is put into hand") {
                    game.isInHand(1, "Forest") shouldBe true
                }
                withClue("Roost Seek shuffles Sagu Wildling into its owner's library") {
                    game.findCardsInLibrary(1, "Sagu Wildling").size shouldBe 1
                    game.state.getExile(game.player1Id).shouldBeEmpty()
                    game.isInGraveyard(1, "Sagu Wildling") shouldBe false
                }
            }
        }
    }
}
