package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.state.components.battlefield.CountersComponent
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.CounterType
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Drill Too Deep (EOE #133) — {1}{R} Instant.
 *
 * "Choose one — • Put five charge counters on target Spacecraft or Planet you control.
 *  • Destroy target artifact."
 *
 * The charge-counter mode may only target a Spacecraft or Planet you control.
 */
class DrillTooDeepScenarioTest : ScenarioTestBase() {

    init {
        context("Drill Too Deep") {

            test("puts five charge counters on a Spacecraft you control") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardInHand(1, "Drill Too Deep")
                    .withLandsOnBattlefield(1, "Mountain", 2)
                    .withCardOnBattlefield(1, "Specimen Freighter")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val freighter = game.findPermanent("Specimen Freighter")!!
                game.castSpellWithMode(1, "Drill Too Deep", 0, freighter).error shouldBe null
                game.resolveStack()

                game.state.getEntity(freighter)?.get<CountersComponent>()
                    ?.getCount(CounterType.CHARGE) shouldBe 5
            }

            test("can't target a Spacecraft an opponent controls") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardInHand(1, "Drill Too Deep")
                    .withLandsOnBattlefield(1, "Mountain", 2)
                    .withCardOnBattlefield(2, "Specimen Freighter")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val theirs = game.findPermanent("Specimen Freighter")!!
                withClue("the charge-counter mode only targets Spacecraft or Planets you control") {
                    game.castSpellWithMode(1, "Drill Too Deep", 0, theirs).error shouldNotBe null
                }
                (game.state.getEntity(theirs)?.get<CountersComponent>()
                    ?.getCount(CounterType.CHARGE) ?: 0) shouldBe 0
            }
        }
    }
}
