package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.SelectCardsDecision
import com.wingedsheep.engine.core.SelectManaSourcesDecision
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Titanbones, Towering Heart — "When you discard this card, you gain 3 life."
 *
 * Regression: a Titanbones on the battlefield fired its discard trigger whenever its controller
 * discarded *any* card (landcycling Vinelasher Adept gained them 3 life).
 */
class TitanbonesToweringHeartScenarioTest : ScenarioTestBase() {
    init {
        test("Titanbones on the battlefield does not trigger when you discard another card") {
            val game = scenario().withPlayers()
                .withCardOnBattlefield(1, "Titanbones, Towering Heart")
                .withCardInHand(1, "Vinelasher Adept")
                .withCardInLibrary(1, "Forest")
                .withLandsOnBattlefield(1, "Forest", 2)
                .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN).build()

            game.typecycleCard(1, "Vinelasher Adept").error shouldBe null
            if (game.getPendingDecision() is SelectManaSourcesDecision) game.submitManaSourcesAutoPay()
            game.resolveStack()
            (game.getPendingDecision() as? SelectCardsDecision)?.let { game.selectCards(it.options.take(1)) }
            game.resolveStack()

            game.isInHand(1, "Forest") shouldBe true
            withClue("Titanbones' discard trigger watches only its own discard") {
                game.getLifeTotal(1) shouldBe 20
            }
        }

        test("discarding Titanbones itself gains 3 life") {
            val game = scenario().withPlayers()
                .withCardInHand(1, "Titanbones, Towering Heart")
                .withCardInHand(2, "Mind Rot")
                .withLandsOnBattlefield(2, "Swamp", 3)
                .withActivePlayer(2)
                .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN).build()

            game.castSpellTargetingPlayer(2, "Mind Rot", 1).error shouldBe null
            game.resolveStack()

            game.isInGraveyard(1, "Titanbones, Towering Heart") shouldBe true
            game.getLifeTotal(1) shouldBe 23
        }
    }
}
