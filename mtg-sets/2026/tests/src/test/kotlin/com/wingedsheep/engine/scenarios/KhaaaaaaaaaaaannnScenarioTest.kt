package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.CastSpell
import com.wingedsheep.engine.state.ZoneKey
import com.wingedsheep.engine.state.components.battlefield.DamageComponent
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.state.components.stack.ChosenTarget
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.core.Zone
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe

/**
 * Khaaaaaaaaaaaannn! (TRK #154) — {X}{R} Instant.
 *
 * "Khaaaaaaaaaaaannn! deals twice X damage to target creature. If that creature would die this
 *  turn, exile it instead."
 *
 * X is chosen at cast time and doubled at resolution; a creature the damage kills is exiled.
 */
class KhaaaaaaaaaaaannnScenarioTest : ScenarioTestBase() {

    init {
        context("Khaaaaaaaaaaaannn!") {

            fun TestGame.castKhan(x: Int, targetName: String) {
                val target = findPermanent(targetName)!!
                val spell = state.getHand(player1Id).first {
                    state.getEntity(it)?.get<CardComponent>()?.name == "Khaaaaaaaaaaaannn!"
                }
                execute(
                    CastSpell(player1Id, spell, listOf(ChosenTarget.Permanent(target)), xValue = x)
                ).error shouldBe null
                resolveStack()
            }

            test("X = 2 deals 4 damage, and the creature it kills is exiled instead of dying") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardInHand(1, "Khaaaaaaaaaaaannn!")
                    .withLandsOnBattlefield(1, "Mountain", 3)
                    .withCardOnBattlefield(2, "Giant Spider") // 2/4
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val spider = game.findPermanent("Giant Spider")!!
                game.castKhan(2, "Giant Spider")

                withClue("4 damage kills the 2/4, which goes to exile, not the graveyard") {
                    game.state.getZone(ZoneKey(game.player2Id, Zone.EXILE)) shouldContain spider
                    game.state.getZone(ZoneKey(game.player2Id, Zone.GRAVEYARD)) shouldNotContain spider
                }
            }

            test("X = 1 deals 2 damage, so a 2/4 survives with 2 damage marked") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardInHand(1, "Khaaaaaaaaaaaannn!")
                    .withLandsOnBattlefield(1, "Mountain", 2)
                    .withCardOnBattlefield(2, "Giant Spider")
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val spider = game.findPermanent("Giant Spider")!!
                game.castKhan(1, "Giant Spider")

                game.state.getBattlefield() shouldContain spider
                game.state.getEntity(spider)?.get<DamageComponent>()?.amount shouldBe 2
            }
        }
    }
}
