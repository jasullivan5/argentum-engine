package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.ActivateAbility
import com.wingedsheep.engine.state.components.stack.ChosenTarget
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.scripting.AbilityCost
import io.kotest.matchers.shouldBe

/**
 * Keral Keep Disciples (M21 #334) — "Whenever you activate a loyalty ability of a Chandra
 * planeswalker, this creature deals 1 damage to each opponent."
 *
 * Pins the source-scoped loyalty trigger: a Chandra's loyalty ability fires it, another
 * planeswalker's does not.
 */
class KeralKeepDisciplesScenarioTest : ScenarioTestBase() {

    private fun plusOneOf(name: String) = cardRegistry.getCard(name)!!.script.activatedAbilities
        .single { (it.cost as? AbilityCost.Loyalty)?.change == 1 }.id

    init {
        test("a Chandra loyalty ability deals 1 damage to each opponent") {
            val game = scenario().withPlayers()
                .withCardOnBattlefield(1, "Keral Keep Disciples")
                .withCardOnBattlefield(1, "Chandra Nalaar")
                .withCardInLibrary(1, "Mountain")
                .withCardInLibrary(2, "Mountain")
                .withActivePlayer(1)
                .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                .build()

            val chandra = game.findPermanent("Chandra Nalaar")!!
            game.execute(
                ActivateAbility(
                    game.player1Id,
                    chandra,
                    plusOneOf("Chandra Nalaar"),
                    targets = listOf(ChosenTarget.Player(game.player2Id)),
                )
            ).error shouldBe null
            game.resolveStack()

            game.getLifeTotal(2) shouldBe 18 // 1 from the Disciples, 1 from Chandra's +1
            game.getLifeTotal(1) shouldBe 20
        }

        test("a non-Chandra planeswalker's loyalty ability doesn't trigger it") {
            val game = scenario().withPlayers()
                .withCardOnBattlefield(1, "Keral Keep Disciples")
                .withCardOnBattlefield(1, "Ajani Goldmane")
                .withCardInLibrary(1, "Mountain")
                .withCardInLibrary(2, "Mountain")
                .withActivePlayer(1)
                .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                .build()

            val ajani = game.findPermanent("Ajani Goldmane")!!
            game.execute(ActivateAbility(game.player1Id, ajani, plusOneOf("Ajani Goldmane"))).error shouldBe null
            game.resolveStack()

            game.getLifeTotal(2) shouldBe 20
        }
    }
}
