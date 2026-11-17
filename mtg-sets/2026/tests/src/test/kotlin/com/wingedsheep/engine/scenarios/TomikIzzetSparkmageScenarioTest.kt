package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.CastSpell
import com.wingedsheep.engine.state.components.battlefield.PreparedSpellCopyComponent
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.state.components.stack.ChosenTarget
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Tomik, Izzet Sparkmage: noncombat damage from a source you control to an
 * opponent deals 1 more — including a copy of a prepare spell cast from exile (Vicious Verse).
 */
class TomikIzzetSparkmageScenarioTest : ScenarioTestBase() {

    init {
        test("Lightning Strike to the opponent deals 4 with Tomik out") {
            val game = scenario()
                .withPlayers("Player", "Opponent")
                .withCardOnBattlefield(1, "Tomik, Izzet Sparkmage")
                .withCardInHand(1, "Lightning Strike")
                .withLandsOnBattlefield(1, "Mountain", 2)
                .withActivePlayer(1)
                .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                .build()

            game.castSpellTargetingPlayer(1, "Lightning Strike", 2).error shouldBe null
            game.resolveStack()

            game.getLifeTotal(2) shouldBe 16
        }

        test("Vicious Verse cast from a prepared Hallway Heckler deals 2 with Tomik out") {
            val game = scenario()
                .withPlayers("Player", "Opponent")
                .withCardOnBattlefield(1, "Tomik, Izzet Sparkmage")
                .withCardInHand(1, "Hallway Heckler")
                .withLandsOnBattlefield(1, "Mountain", 4)
                .withActivePlayer(1)
                .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                .build()

            game.castSpell(1, "Hallway Heckler").error shouldBe null
            game.resolveStack()

            val copy = game.state.getExile(game.player1Id).single { id ->
                val e = game.state.getEntity(id)
                e?.get<CardComponent>()?.name == "Hallway Heckler" && e.get<PreparedSpellCopyComponent>() != null
            }
            game.execute(
                CastSpell(game.player1Id, copy, listOf(ChosenTarget.Player(game.player2Id)), faceIndex = 0)
            ).error shouldBe null
            game.resolveStack()

            withClue("Vicious Verse's 1 damage is amplified to 2 by Tomik") {
                game.getLifeTotal(2) shouldBe 18
            }
        }
    }
}
