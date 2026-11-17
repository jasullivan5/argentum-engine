package com.wingedsheep.ai.engine

import com.wingedsheep.ai.puzzles.advanceToDeclaration
import com.wingedsheep.ai.puzzles.seatId
import com.wingedsheep.engine.core.DeclareAttackers
import com.wingedsheep.engine.core.DeclareBlockers
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.EntityId
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotBeEmpty

/**
 * Free gang blocks ([AiProfile.freeGangBlocks]), from a logged game (`a41c9386`, turn 9): the AI
 * swung Theorix Metamage (2/3) into Marwyn, the Preserver (3/2) and two Greenhouse Propagators
 * (2/3), with every opposing land tapped. The Propagators double-blocked and killed it for
 * nothing. The block heuristic the attack search simulates against predicted Marwyn trading
 * instead, so the attack read as a fair trade (and, unblocked, two free damage).
 */
class PointlessAttackIntoBlockersTest : ScenarioTestBase() {

    private companion object {
        val WITHOUT = AiProfile.LIVE.copy(id = "live-without-free-gang-blocks", freeGangBlocks = false)
    }

    private fun ScenarioBuilder.on(seat: Int, vararg names: String) =
        names.fold(this) { b, n -> b.withCardOnBattlefield(seat, n) }

    /** The logged position at the AI's (seat 1's) declare-attackers step. */
    private fun loggedBoard(): TestGame {
        var b = scenario().withRngSeed(20261031L).withPlayers()
            .withLifeTotal(1, 22).withLifeTotal(2, 21)
            .on(1, "Theorix Metamage", "Arcane Amphisbaena", "Way of the Paradox")
            .withCardOnBattlefield(1, "Sureshot Sower", summoningSickness = true)
        repeat(2) { b = b.withCardOnBattlefield(1, "Island", tapped = true) }
        b = b.withCardOnBattlefield(1, "Forest", tapped = true)
        listOf("Semester Foreseer", "Archive Arbiter", "Paradox Shaper", "Twinned Vision")
            .forEach { b = b.withCardInHand(1, it) }
        b = b.on(2, "Marwyn, the Preserver", "Greenhouse Propagator", "Greenhouse Propagator")
        repeat(4) { b = b.withCardOnBattlefield(2, "Forest", tapped = true) }
        listOf("Garruk, Veiled Butcher", "Puppet Crafting", "Forest", "Greenhouse Propagator")
            .forEach { b = b.withCardInHand(2, it) }
        return b.build().advanceToDeclaration(1, Step.DECLARE_ATTACKERS)
    }

    private fun TestGame.nameOf(id: EntityId): String? = state.getEntity(id)?.get<CardComponent>()?.name

    private fun TestGame.attackersChosenBy(profile: AiProfile): List<String?> =
        (AIPlayer.create(cardRegistry, seatId(1), profile).chooseAction(state) as? DeclareAttackers)
            ?.attackers?.keys.orEmpty().map { nameOf(it) }

    init {
        test("does not attack a 2/3 into two 2/3s that can double-block it for free") {
            val game = loggedBoard()
            val attackers = game.attackersChosenBy(AiProfile.LIVE)
            withClue("attacked with $attackers") { attackers.shouldBeEmpty() }
        }

        test("without free gang blocks the logged attack comes back") {
            val game = loggedBoard()
            game.attackersChosenBy(WITHOUT).shouldNotBeEmpty()
        }

        test("as the defender, kills the attacker with a free double block instead of a lone Propagator bouncing it") {
            val game = scenario().withRngSeed(20261031L).withPlayers()
                .on(1, "Theorix Metamage")
                .on(2, "Marwyn, the Preserver", "Greenhouse Propagator", "Greenhouse Propagator")
                .build().advanceToDeclaration(1, Step.DECLARE_ATTACKERS)
            val metamage = game.findPermanents("Theorix Metamage").single()
            game.execute(DeclareAttackers(game.seatId(1), mapOf(metamage to game.seatId(2))))
            game.advanceToDeclaration(2, Step.DECLARE_BLOCKERS)

            val blocks = AIPlayer.create(cardRegistry, game.seatId(2), AiProfile.LIVE)
                .chooseAction(game.state) as DeclareBlockers
            val blockers = blocks.blockers.filterValues { metamage in it }.keys.map { game.nameOf(it) }
            withClue("blocked with $blockers") {
                blockers.shouldContainExactlyInAnyOrder("Greenhouse Propagator", "Greenhouse Propagator")
            }
        }
    }
}
