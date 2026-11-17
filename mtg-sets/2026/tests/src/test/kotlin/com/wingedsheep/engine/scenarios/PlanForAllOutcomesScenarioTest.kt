package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.OptionChosenResponse
import com.wingedsheep.engine.state.components.battlefield.CountersComponent
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.CounterType
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.matchers.shouldBe

/**
 * Plan for All Outcomes (Reality Fracture #35) — {3}{U} Enchantment:
 *   When this enchantment enters, the owner of up to one other target nonland permanent puts it on
 *   their choice of the top or bottom of their library.
 *   Whenever you cast your first noncreature spell each turn, empower Jace 1.
 */
class PlanForAllOutcomesScenarioTest : ScenarioTestBase() {

    private fun ScenarioTestBase.TestGame.jaceTokenLoyalty(): Int? =
        findPermanents("Jace").singleOrNull()
            ?.let { state.getEntity(it)?.get<CountersComponent>()?.getCount(CounterType.LOYALTY) }

    init {
        test("ETB: the target's owner chooses, and bottom puts it under their library") {
            val game = scenario().withPlayers()
                .withCardInHand(1, "Plan for All Outcomes")
                .withLandsOnBattlefield(1, "Island", 4)
                .withCardOnBattlefield(2, "Grizzly Bears")
                .withCardInLibrary(1, "Island")
                .withCardInLibrary(2, "Forest")
                .withActivePlayer(1)
                .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN).build()

            val bears = game.findPermanent("Grizzly Bears")!!
            game.castSpell(1, "Plan for All Outcomes").error shouldBe null
            game.resolveStack()
            game.selectTargets(listOf(bears)).error shouldBe null
            game.resolveStack()

            val decision = game.getPendingDecision()!!
            decision.playerId shouldBe game.player2Id
            game.submitDecision(OptionChosenResponse(decision.id, 1)).error shouldBe null
            game.resolveStack()

            game.isOnBattlefield("Grizzly Bears") shouldBe false
            game.state.getLibrary(game.player2Id).last() shouldBe bears
            // Casting Plan itself doesn't trigger its own second ability — it wasn't on the battlefield.
            game.findPermanents("Jace") shouldBe emptyList()
        }

        test("ETB target is optional") {
            val game = scenario().withPlayers()
                .withCardInHand(1, "Plan for All Outcomes")
                .withLandsOnBattlefield(1, "Island", 4)
                .withCardOnBattlefield(2, "Grizzly Bears")
                .withCardInLibrary(1, "Island")
                .withCardInLibrary(2, "Forest")
                .withActivePlayer(1)
                .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN).build()

            game.castSpell(1, "Plan for All Outcomes").error shouldBe null
            game.resolveStack()
            game.skipTargets().error shouldBe null
            game.resolveStack()

            game.isOnBattlefield("Grizzly Bears") shouldBe true
            game.isOnBattlefield("Plan for All Outcomes") shouldBe true
        }

        test("only the first noncreature spell each turn empowers Jace") {
            val game = scenario().withPlayers()
                .withCardOnBattlefield(1, "Plan for All Outcomes")
                .withCardInHand(1, "Raging Goblin")
                .withCardInHand(1, "Lightning Bolt")
                .withCardInHand(1, "Shock")
                .withLandsOnBattlefield(1, "Mountain", 4)
                .withCardInLibrary(1, "Island")
                .withCardInLibrary(2, "Island")
                .withActivePlayer(1)
                .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN).build()

            // A creature spell doesn't count.
            game.castSpell(1, "Raging Goblin").error shouldBe null
            game.resolveStack()
            game.findPermanents("Jace") shouldBe emptyList()

            game.castSpellTargetingPlayer(1, "Lightning Bolt", 2).error shouldBe null
            game.resolveStack()
            game.jaceTokenLoyalty() shouldBe 1

            game.castSpellTargetingPlayer(1, "Shock", 2).error shouldBe null
            game.resolveStack()
            game.jaceTokenLoyalty() shouldBe 1
            game.getLifeTotal(2) shouldBe 15
        }

        test("a creature spell cast after the first noncreature spell doesn't trigger") {
            val game = scenario().withPlayers()
                .withCardOnBattlefield(1, "Plan for All Outcomes")
                .withCardInHand(1, "Lightning Bolt")
                .withCardInHand(1, "Raging Goblin")
                .withLandsOnBattlefield(1, "Mountain", 2)
                .withCardInLibrary(1, "Island")
                .withCardInLibrary(2, "Island")
                .withActivePlayer(1)
                .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN).build()

            game.castSpellTargetingPlayer(1, "Lightning Bolt", 2).error shouldBe null
            game.resolveStack()
            game.jaceTokenLoyalty() shouldBe 1

            // The noncreature tally is still 1 here — the creature spell mustn't read that as "first".
            game.castSpell(1, "Raging Goblin").error shouldBe null
            game.resolveStack()
            game.isOnBattlefield("Raging Goblin") shouldBe true
            game.jaceTokenLoyalty() shouldBe 1
        }

        test("Plan itself was the turn's first noncreature spell, so a later one doesn't trigger") {
            val game = scenario().withPlayers()
                .withCardInHand(1, "Plan for All Outcomes")
                .withCardInHand(1, "Lightning Bolt")
                .withLandsOnBattlefield(1, "Volcanic Island", 5)
                .withCardInLibrary(1, "Island")
                .withCardInLibrary(2, "Island")
                .withActivePlayer(1)
                .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN).build()

            game.castSpell(1, "Plan for All Outcomes").error shouldBe null
            game.resolveStack()
            if (game.hasPendingDecision()) game.skipTargets()
            game.resolveStack()
            game.isOnBattlefield("Plan for All Outcomes") shouldBe true

            game.castSpellTargetingPlayer(1, "Lightning Bolt", 2).error shouldBe null
            game.resolveStack()
            game.getLifeTotal(2) shouldBe 17
            game.findPermanents("Jace") shouldBe emptyList()
        }
    }
}
