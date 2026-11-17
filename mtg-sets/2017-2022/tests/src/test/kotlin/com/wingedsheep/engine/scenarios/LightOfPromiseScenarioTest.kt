package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.state.components.battlefield.CountersComponent
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.CounterType
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * Light of Promise (M21 #25) — Enchanted creature has "Whenever you gain life, put that many +1/+1
 * counters on this creature."
 *
 * Pins the aura-granted life-gain trigger: "that many" is the amount gained, and "you" is the
 * enchanted creature's controller, not the Aura's.
 */
class LightOfPromiseScenarioTest : ScenarioTestBase() {

    private fun TestGame.plusCounters(id: EntityId): Int =
        state.getEntity(id)?.get<CountersComponent>()?.getCount(CounterType.PLUS_ONE_PLUS_ONE) ?: 0

    init {
        cardRegistry.register(card("Test Gain Three") {
            manaCost = "{0}"
            typeLine = "Instant"
            spell { effect = Effects.GainLife(3) }
        })
        cardRegistry.register(card("Test Opponent Gains Two") {
            manaCost = "{0}"
            typeLine = "Instant"
            spell { effect = Effects.GainLife(2, EffectTarget.PlayerRef(Player.EachOpponent)) }
        })

        test("gaining 3 life puts three +1/+1 counters on the enchanted creature") {
            val game = scenario().withPlayers()
                .withCardOnBattlefield(1, "Grizzly Bears")
                .withCardAttachedTo(1, "Light of Promise", "Grizzly Bears")
                .withCardInHand(1, "Test Gain Three")
                .withActivePlayer(1)
                .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                .build()
            val bears = game.findPermanent("Grizzly Bears")!!

            game.castSpell(1, "Test Gain Three").error shouldBe null
            game.resolveStack()

            game.getLifeTotal(1) shouldBe 23
            game.plusCounters(bears) shouldBe 3
            game.state.projectedState.getPower(bears) shouldBe 5
        }

        test("on an opponent's creature, it counts that player's life gain, not the Aura controller's") {
            val game = scenario().withPlayers()
                .withCardOnBattlefield(2, "Hill Giant")
                .withCardAttachedTo(1, "Light of Promise", "Hill Giant")
                .withCardInHand(1, "Test Gain Three")
                .withCardInHand(1, "Test Opponent Gains Two")
                .withActivePlayer(1)
                .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                .build()
            val giant = game.findPermanent("Hill Giant")!!

            game.castSpell(1, "Test Gain Three").error shouldBe null
            game.resolveStack()
            withClue("the Aura's controller gaining life doesn't trigger the creature's ability") {
                game.plusCounters(giant) shouldBe 0
            }

            game.castSpell(1, "Test Opponent Gains Two").error shouldBe null
            game.resolveStack()
            withClue("the creature's controller gained 2, so it gets two counters") {
                game.getLifeTotal(2) shouldBe 22
                game.plusCounters(giant) shouldBe 2
            }
        }
    }
}
