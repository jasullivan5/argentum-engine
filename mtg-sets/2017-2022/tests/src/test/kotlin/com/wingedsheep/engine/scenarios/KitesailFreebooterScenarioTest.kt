package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.ChooseTargetsDecision
import com.wingedsheep.engine.core.SelectCardsDecision
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Kitesail Freebooter (XLN #110) — "When this creature enters, target opponent reveals their hand.
 * You choose a noncreature, nonland card from it. Exile that card until this creature leaves the
 * battlefield."
 *
 * Pins the linked exile out of the opponent's hand: only noncreature, nonland cards are choosable,
 * and the card goes back to its owner's hand when the Freebooter leaves.
 */
class KitesailFreebooterScenarioTest : ScenarioTestBase() {

    private fun freebooterTakingShock(): TestGame {
        val game = scenario()
            .withPlayers("Player1", "Player2")
            .withCardInHand(1, "Kitesail Freebooter")
            .withCardInHand(1, "Lightning Bolt")
            .withLandsOnBattlefield(1, "Swamp", 2)
            .withLandsOnBattlefield(1, "Mountain", 1)
            .withCardInHand(2, "Grizzly Bears")
            .withCardInHand(2, "Forest")
            .withCardInHand(2, "Shock")
            .withActivePlayer(1)
            .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
            .build()

        game.castSpell(1, "Kitesail Freebooter").error shouldBe null
        game.resolveStack()
        if (game.getPendingDecision() is ChooseTargetsDecision) {
            game.selectTargets(listOf(game.player2Id)).error shouldBe null
            game.resolveStack()
        }

        val decision = game.getPendingDecision().shouldBeInstanceOf<SelectCardsDecision>()
        withClue("the Freebooter's controller chooses") { decision.playerId shouldBe game.player1Id }
        val shock = game.findCardsInHand(2, "Shock").single()
        withClue("only the noncreature, nonland card is choosable") {
            decision.options shouldBe listOf(shock)
        }
        game.selectCards(listOf(shock)).error shouldBe null
        game.resolveStack()
        return game
    }

    init {
        test("exiles the chosen noncreature, nonland card until the Freebooter leaves") {
            val game = freebooterTakingShock()
            game.isInExile(2, "Shock") shouldBe true
            game.isInHand(2, "Grizzly Bears") shouldBe true
            game.isInHand(2, "Forest") shouldBe true

            val freebooter = game.findPermanent("Kitesail Freebooter").shouldNotBeNull()
            game.castSpell(1, "Lightning Bolt", freebooter).error shouldBe null
            game.resolveStack()

            withClue("the Freebooter died, so Shock returns to its owner's hand") {
                game.isInGraveyard(1, "Kitesail Freebooter") shouldBe true
                game.isInExile(2, "Shock") shouldBe false
                game.isInHand(2, "Shock") shouldBe true
            }
        }
    }
}
