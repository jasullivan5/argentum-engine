package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.state.components.identity.TokenComponent
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.EntityId
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Face Yourself (FRA #83) — {5}{R}{R} Sorcery.
 *
 * "For each creature target player controls, create a token that's a copy of that creature, except
 * it has haste and "At the beginning of the end step, if you don't control a planeswalker,
 * sacrifice this creature.""
 */
class FaceYourselfScenarioTest : ScenarioTestBase() {

    private fun TestGame.tokensNamed(name: String): List<EntityId> =
        findAllPermanents(name).filter { state.getEntity(it)?.has<TokenComponent>() == true }

    private fun TestGame.controller(id: EntityId): EntityId? = state.projectedState.getController(id)

    private fun builder(): ScenarioBuilder {
        var b = scenario()
            .withPlayers("Player", "Opponent")
            .withCardInHand(1, "Face Yourself")
            .withLandsOnBattlefield(1, "Mountain", 7)
            .withCardOnBattlefield(1, "Savannah Lions")
            .withCardOnBattlefield(2, "Grizzly Bears")
            .withCardOnBattlefield(2, "Hill Giant")
            .withActivePlayer(1)
            .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
        repeat(5) { b = b.withCardInLibrary(1, "Mountain") }
        repeat(5) { b = b.withCardInLibrary(2, "Mountain") }
        return b
    }

    init {
        test("copies each creature the target player controls, under your control and with haste") {
            val game = builder().build()

            game.castSpellTargetingPlayer(1, "Face Yourself", 2).error shouldBe null
            game.resolveStack()

            val bearsTokens = game.tokensNamed("Grizzly Bears")
            val giantTokens = game.tokensNamed("Hill Giant")
            withClue("one token per creature the opponent controls") {
                bearsTokens.size shouldBe 1
                giantTokens.size shouldBe 1
            }
            withClue("your own creatures are not copied") {
                game.tokensNamed("Savannah Lions").size shouldBe 0
            }
            for (token in bearsTokens + giantTokens) {
                game.controller(token) shouldBe game.player1Id
                game.state.projectedState.hasKeyword(token, Keyword.HASTE) shouldBe true
            }
            withClue("the originals stay with the opponent") {
                game.findAllPermanents("Grizzly Bears").size shouldBe 2
                game.findAllPermanents("Hill Giant").size shouldBe 2
            }
        }

        test("the tokens are sacrificed at the beginning of the end step when you control no planeswalker") {
            val game = builder().build()

            game.castSpellTargetingPlayer(1, "Face Yourself", 2).error shouldBe null
            game.resolveStack()
            game.tokensNamed("Grizzly Bears").size shouldBe 1

            game.passUntilPhase(Phase.ENDING, Step.END)
            game.resolveStack()

            game.tokensNamed("Grizzly Bears").size shouldBe 0
            game.tokensNamed("Hill Giant").size shouldBe 0
            withClue("the opponent's originals are untouched") {
                game.findAllPermanents("Grizzly Bears").size shouldBe 1
                game.findAllPermanents("Hill Giant").size shouldBe 1
            }
        }

        test("while you control a planeswalker the tokens survive the end step") {
            val game = builder()
                .withCardOnBattlefield(1, "Ajani Resolute")
                .build()

            game.castSpellTargetingPlayer(1, "Face Yourself", 2).error shouldBe null
            game.resolveStack()

            game.passUntilPhase(Phase.ENDING, Step.END)
            game.resolveStack()

            game.tokensNamed("Grizzly Bears").size shouldBe 1
            game.tokensNamed("Hill Giant").size shouldBe 1
        }

        test("targeting yourself copies your own creatures") {
            val game = builder().build()

            game.castSpellTargetingPlayer(1, "Face Yourself", 1).error shouldBe null
            game.resolveStack()

            game.tokensNamed("Savannah Lions").size shouldBe 1
            game.tokensNamed("Grizzly Bears").size shouldBe 0
        }

        test("the tokens enter together, so a copied Greenhouse Propagator sees the other copies enter") {
            val game = builder()
                .withCardOnBattlefield(1, "Grizzly Bears")
                .withCardOnBattlefield(1, "Greenhouse Propagator")
                .build()

            game.castSpellTargetingPlayer(1, "Face Yourself", 1).error shouldBe null
            game.resolveStack()

            game.tokensNamed("Greenhouse Propagator").size shouldBe 1
            withClue("original Propagator: 3 other tokens; token Propagator: the Lions and Bears tokens (CR 603.6a)") {
                game.getLifeTotal(1) shouldBe 25
            }
        }
    }
}
