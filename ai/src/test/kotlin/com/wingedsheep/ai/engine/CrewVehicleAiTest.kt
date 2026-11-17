package com.wingedsheep.ai.engine

import com.wingedsheep.ai.puzzles.advanceToPriority
import com.wingedsheep.engine.core.CrewVehicle
import com.wingedsheep.engine.core.GameAction
import com.wingedsheep.engine.core.SaddleMount
import com.wingedsheep.engine.state.components.battlefield.SummoningSicknessComponent
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.EntityId
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldNotBeInstanceOf

/**
 * Crew N and Saddle N are offered with no creatures named; the AI has to choose who taps, and
 * whether the ride is worth it.
 *
 * Game `556300fa` (2026-10-31 AI review), turns 8–13: the AI held Surgehacker Mech (5/5 menace,
 * crew 4) next to a 3/2 and a 1/1 and passed every main phase with "had: Crew Surgehacker Mech" —
 * the empty crew was refused once materialized and the activation silently dropped — and lost the
 * race at exactly 0 life. The same gap left Mounts unsaddled.
 */
class CrewVehicleAiTest : ScenarioTestBase() {

    private fun TestGame.choice(): GameAction = AIPlayer.create(cardRegistry, player1Id).chooseAction(state)

    private fun TestGame.ours(name: String): List<EntityId> =
        state.getBattlefield(player1Id).filter { state.getEntity(it)?.get<CardComponent>()?.name == name }

    private fun mechPosition(mechSick: Boolean = false) = scenario()
        .withPlayers()
        .withTurnNumber(10)
        .withActivePlayer(1)
        .withLifeTotal(1, 13)
        .withLifeTotal(2, 5)
        .withCardOnBattlefield(1, "Surgehacker Mech", summoningSickness = mechSick)
        .withCardOnBattlefield(1, "Norika Yamazaki, the Poet")
        .withCardOnBattlefield(1, "Spirited Companion")
        .withCardOnBattlefield(2, "Grizzly Bears")
        .build()
        .advanceToPriority(1, Step.PRECOMBAT_MAIN)

    init {
        // 556300fa's shape: the 3/2 and the 1/1 attacking themselves deal at most 3 past the Bears;
        // crewed, the 5/5 menace Mech can't be blocked by the lone Bear and deals exactly 5.
        test("crews Surgehacker Mech with a 3/2 and a 1/1 for lethal past a lone blocker") {
            val game = mechPosition()

            val action = game.choice()
            val crew = withClue("chose $action") { action.shouldBeInstanceOf<CrewVehicle>() }
            crew.crewCreatures shouldContainExactlyInAnyOrder
                game.ours("Norika Yamazaki, the Poet") + game.ours("Spirited Companion")
            game.execute(crew).error shouldBe null
        }

        // A Vehicle that came in this turn can't attack, so crewing it only taps the team.
        test("doesn't crew a Vehicle that can't attack this turn") {
            val game = mechPosition(mechSick = true)

            game.choice().shouldNotBeInstanceOf<CrewVehicle>()
        }

        // Saddle shares the payment. Saddled, the Charger attacks as a 5/5 menace past the lone
        // Hill Giant for lethal; unsaddled, the Giant blocks it. Either Bear's power saddles it, and
        // the summoning-sick one can't attack anyway, so it is the one tapped.
        test("saddles Quilled Charger for lethal with the creature that couldn't attack this turn") {
            val game = scenario()
                .withPlayers()
                .withTurnNumber(10)
                .withActivePlayer(1)
                .withLifeTotal(2, 5)
                .withCardOnBattlefield(1, "Quilled Charger")
                .withCardOnBattlefield(1, "Grizzly Bears", summoningSickness = true)
                .withCardOnBattlefield(1, "Grizzly Bears")
                .withCardOnBattlefield(2, "Hill Giant")
                .build()
                .advanceToPriority(1, Step.PRECOMBAT_MAIN)
            val sickBear = game.ours("Grizzly Bears")
                .single { game.state.getEntity(it)!!.has<SummoningSicknessComponent>() }

            val action = game.choice()
            val saddle = withClue("chose $action") { action.shouldBeInstanceOf<SaddleMount>() }
            saddle.saddleCreatures shouldBe listOf(sickBear)
            game.execute(saddle).error shouldBe null
        }
    }
}
