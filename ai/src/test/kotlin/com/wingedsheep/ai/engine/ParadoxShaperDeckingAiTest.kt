package com.wingedsheep.ai.engine

import com.wingedsheep.engine.core.ActivateAbility
import com.wingedsheep.engine.core.GameAction
import com.wingedsheep.engine.core.PassPriority
import com.wingedsheep.engine.legalactions.EnumerationMode
import com.wingedsheep.engine.legalactions.LegalActionEnumerator
import com.wingedsheep.engine.legalactions.MeaningfulActionFilter
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.EntityId
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * 2026-10-31 AI misplay report: the AI decked itself with Paradox Shaper ("{2}: Put target card
 * from your graveyard on the bottom of your library") on the battlefield, ten lands and 22 cards
 * in its graveyard.
 *
 * Its own upkeep with an empty library is the last window before the draw that loses (CR 704.5b),
 * but the auto-pass rules passed every own upkeep unconditionally — the AI's enumeration-free fast
 * path and the server's auto-pass loop alike — so the activation was never even scored there.
 */
class ParadoxShaperDeckingAiTest : ScenarioTestBase() {

    private fun position(library: Int, phase: Phase, step: Step, active: Int): TestGame {
        val builder = scenario().withPlayers()
            .withTurnNumber(14)
            .withActivePlayer(active)
            .withPriorityPlayer(1)
            .inPhase(phase, step)
            .withLandsOnBattlefield(1, "Island", 5)
            .withCardOnBattlefield(1, "Paradox Shaper")
            .withCardOnBattlefield(1, "Hill Giant")
        repeat(library) { builder.withCardInLibrary(1, "Island") }
        repeat(8) { builder.withCardInGraveyard(1, if (it % 2 == 0) "Island" else "Hill Giant") }
        builder.withLandsOnBattlefield(2, "Forest", 5)
        builder.withCardOnBattlefield(2, "Grizzly Bears")
        repeat(20) { builder.withCardInLibrary(2, "Forest") }
        return builder.build()
    }

    private fun TestGame.choice(): GameAction =
        AIPlayer.create(cardRegistry, player1Id, AiProfile.LIVE).chooseAction(state)

    private fun TestGame.nameOf(id: EntityId) = state.getEntity(id)?.get<CardComponent>()?.name

    private fun TestGame.serverAutoPasses(): Boolean =
        MeaningfulActionFilter.autoPassVerdict(
            state, player1Id, LegalActionEnumerator.create(cardRegistry).enumerate(state, player1Id, EnumerationMode.ACTIONS_ONLY), cardRegistry = cardRegistry,
        ).autoPass

    init {
        test("empty library, own upkeep: Paradox Shaper puts a card back before the draw") {
            val game = position(library = 0, Phase.BEGINNING, Step.UPKEEP, active = 1)
            game.serverAutoPasses() shouldBe false
            val activate = game.choice().shouldBeInstanceOf<ActivateAbility>()
            game.nameOf(activate.sourceId) shouldBe "Paradox Shaper"
        }

        test("empty library, opponent's end step: Paradox Shaper puts a card back") {
            val game = position(library = 0, Phase.ENDING, Step.END, active = 2)
            val activate = game.choice().shouldBeInstanceOf<ActivateAbility>()
            game.nameOf(activate.sourceId) shouldBe "Paradox Shaper"
        }

        test("a library with cards in it: own upkeep still passes without a stop") {
            val game = position(library = 10, Phase.BEGINNING, Step.UPKEEP, active = 1)
            game.serverAutoPasses() shouldBe true
            game.choice().shouldBeInstanceOf<PassPriority>()
        }
    }
}
