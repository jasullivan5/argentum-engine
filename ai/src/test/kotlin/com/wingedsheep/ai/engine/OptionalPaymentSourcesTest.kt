package com.wingedsheep.ai.engine

import com.wingedsheep.ai.engine.knowledge.IntentCatalog
import com.wingedsheep.engine.core.PassPriority
import com.wingedsheep.engine.core.YesNoDecision
import com.wingedsheep.engine.core.YesNoResponse
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * An optional mana payment ("you may pay {1}") is answered yes only when the AI's own mana
 * payer would then go on and pay it.
 *
 * 2026-10-31 AI-vs-humans review, game eee158b2 T13/T17: Wooden Sphere asked "Pay {1}?" with the
 * lands tapped and Gene Pollinator ("{T}, Tap an untapped permanent you control: Add one mana of
 * any color") the only source. The engine rightly offered yes — Pollinator can pay — but the
 * AI's payer never picks a tap-another-permanent source on an optional payment, so it said yes
 * and then submitted an empty selection, every time a green spell was cast.
 */
class OptionalPaymentSourcesTest : ScenarioTestBase() {

    private val intents by lazy { IntentCatalog.of(cardRegistry) }

    /** Opponent casts a green spell; pass until Wooden Sphere's trigger asks its yes/no. */
    private fun TestGame.advanceToSphereYesNo(): YesNoDecision {
        castSpell(2, "Grizzly Bears").error shouldBe null
        var guard = 0
        while (state.pendingDecision == null && guard++ < 10) {
            execute(PassPriority(state.priorityPlayerId!!))
        }
        return state.pendingDecision.shouldBeInstanceOf<YesNoDecision>()
    }

    private fun TestGame.answer(decision: YesNoDecision): Boolean {
        val simulator = GameSimulator(cardRegistry)
        val responder = DecisionResponder(simulator, AIPlayer.defaultEvaluator(), intents = intents)
        // Wired as `AIPlayer` wires it, so the yes branch's mana prompt is answered by the AI too.
        simulator.decisionResolver = { s, d -> responder.respond(s, d, d.playerId) }
        return (responder.respond(state, decision, player1Id) as YesNoResponse).choice
    }

    init {
        test("eee158b2: no yes to Pay {1} when only a tap-another-permanent source could pay") {
            val game = scenario().withPlayers()
                .withCardOnBattlefield(1, "Wooden Sphere")
                .withCardOnBattlefield(1, "Gene Pollinator")
                .withCardOnBattlefield(1, "Forest", tapped = true)
                .withLandsOnBattlefield(2, "Forest", 2)
                .withCardInHand(2, "Grizzly Bears")
                .withActivePlayer(2)
                .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                .build()
            val decision = game.advanceToSphereYesNo()
            // The engine's offer is right: Pollinator, tapping Wooden Sphere, can pay {1}.
            decision.yesAvailable shouldBe true

            game.answer(decision) shouldBe false
        }

        test("an untapped land still pays for the life") {
            val game = scenario().withPlayers()
                .withCardOnBattlefield(1, "Wooden Sphere")
                .withCardOnBattlefield(1, "Gene Pollinator")
                .withCardOnBattlefield(1, "Forest")
                .withLandsOnBattlefield(2, "Forest", 2)
                .withCardInHand(2, "Grizzly Bears")
                .withActivePlayer(2)
                .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                .build()
            val decision = game.advanceToSphereYesNo()

            game.answer(decision) shouldBe true
        }
    }
}
