package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.CastSpell
import com.wingedsheep.engine.core.PassPriority
import com.wingedsheep.engine.state.components.stack.ChosenTarget
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Diplomatic Relations ({2}{G}, Instant, EOE #177):
 * "Target creature you control gets +1/+0 and gains vigilance until end of turn. It deals damage
 * equal to its power to target creature an opponent controls."
 *
 * Regression: the script applied the +1/+0 and the damage but never granted vigilance.
 */
class DiplomaticRelationsScenarioTest : ScenarioTestBase() {

    init {
        context("Diplomatic Relations") {

            test("pumps and grants vigilance to your creature, which then deals damage equal to its power") {
                val game = scenario()
                    .withPlayers("Alice", "Bob")
                    .withCardInHand(1, "Diplomatic Relations")
                    .withCardOnBattlefield(1, "Grizzly Bears", summoningSickness = false)
                    .withLandsOnBattlefield(1, "Forest", 3)
                    .withCardOnBattlefield(2, "Hill Giant")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val aliceBears = game.findPermanent("Grizzly Bears")!!
                val bobGiant = game.findPermanent("Hill Giant")!!

                val cast = game.execute(
                    CastSpell(
                        game.player1Id,
                        game.findCardsInHand(1, "Diplomatic Relations").single(),
                        listOf(ChosenTarget.Permanent(aliceBears), ChosenTarget.Permanent(bobGiant))
                    )
                )
                withClue("Diplomatic Relations should cast: ${cast.error}") { cast.error shouldBe null }

                while (game.state.stack.isNotEmpty()) {
                    game.execute(PassPriority(game.state.priorityPlayerId ?: break))
                }

                val projected = game.state.projectedState
                withClue("Alice's bears should get +1/+0") { projected.getPower(aliceBears) shouldBe 3 }
                withClue("Alice's bears should gain vigilance") {
                    projected.hasKeyword(aliceBears, Keyword.VIGILANCE) shouldBe true
                }
                withClue("Bob's Hill Giant takes 3 damage and dies") {
                    game.state.getBattlefield().contains(bobGiant) shouldBe false
                }
            }
        }
    }
}
