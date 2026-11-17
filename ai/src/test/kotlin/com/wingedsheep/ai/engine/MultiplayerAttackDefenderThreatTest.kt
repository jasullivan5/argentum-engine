package com.wingedsheep.ai.engine

import com.wingedsheep.engine.core.DeclareAttackers
import com.wingedsheep.engine.registry.CardRegistry
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.model.EntityId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.maps.shouldNotBeEmpty
import io.kotest.matchers.shouldBe

/**
 * [AiProfile.threatAwareDefender]: in a pod the AI attacks the opponent whose board threatens the
 * table most — unless it can kill someone — instead of always the lowest life total.
 *
 * Off the 2026-10-31 AI-vs-humans review, Commander game c92b4495 turn 22: the attacking AI seat
 * (38 life, two 5-power attackers) faced an AI seat on 32 with a 7-power board, an AI seat on 40
 * with no creatures, and the human on 40 with Animar 8/8 (tapped) plus 7 more power. It attacked
 * the seat on 32, as every AI seat did all game; the human was never attacked and won on 38 life.
 */
class MultiplayerAttackDefenderThreatTest : FunSpec({

    val cards = TestCards.all
    val registry = CardRegistry().apply { register(cards) }

    fun advisor(threatAware: Boolean) = CombatAdvisor(
        GameSimulator(registry), AIPlayer.defaultEvaluator(), registry,
        threatAwareDefender = threatAware,
    )

    fun GameTestDriver.ready(player: EntityId, name: String): EntityId =
        putCreatureOnBattlefield(player, name).also { removeSummoningSickness(it) }

    class Pod(val driver: GameTestDriver, val me: EntityId, val weak: EntityId, val empty: EntityId, val leader: EntityId)

    /**
     * Turn 22 of c92b4495, in test cards. Us: two Force of Nature (5/5). [Pod.weak] on 32: Centaur
     * Courser, Savannah Lions, Goblin Guide (6 power). [Pod.empty] on 40: no creatures. [Pod.leader]
     * on 40: two tapped 5/5s (the Animar stand-in) plus an untapped Centaur Courser and Goblin
     * Guide — 15 power. [weakLife] lowers the weak seat; [weakTapped] taps its creatures.
     */
    fun pod(weakLife: Int = 32, weakTapped: Boolean = false, leaderBoard: Boolean = true): Pod {
        val driver = GameTestDriver()
        driver.registerCards(cards)
        val seats = driver.initMultiplayer(decks = List(4) { Deck.of("Plains" to 40) })
        val (me, weak, empty, leader) = seats
        repeat(2) { driver.ready(me, "Force of Nature") }
        listOf("Centaur Courser", "Savannah Lions", "Goblin Guide").forEach {
            val creature = driver.ready(weak, it)
            if (weakTapped) driver.tapPermanent(creature)
        }
        if (leaderBoard) {
            repeat(2) { driver.tapPermanent(driver.ready(leader, "Force of Nature")) }
            driver.ready(leader, "Centaur Courser")
            driver.ready(leader, "Goblin Guide")
        }
        driver.replaceState(
            driver.state.withLifeTotal(me, 38).withLifeTotal(weak, weakLife)
                .withLifeTotal(empty, 40).withLifeTotal(leader, 40),
        )
        driver.passPriorityUntil(Step.DECLARE_ATTACKERS)
        return Pod(driver, me, weak, empty, leader)
    }

    fun Pod.declare(advisor: CombatAdvisor): DeclareAttackers {
        val action = driver.legalActions(me).single { it.actionType == "DeclareAttackers" }
        return advisor.chooseAttackers(driver.state, action, me) as DeclareAttackers
    }

    test("c92b4495 T22: the attack goes at the runaway board, not the AI seat on the lowest life") {
        val pod = pod()
        val plan = pod.declare(advisor(threatAware = true))

        plan.attackers.shouldNotBeEmpty()
        plan.attackers.values.toSet() shouldBe setOf(pod.leader)
        pod.driver.submitSuccess(plan)
    }

    test("a seat the attack can kill is still finished off first") {
        // The weak seat on 10 with its creatures tapped: two unblocked 5/5s kill it.
        val pod = pod(weakLife = 10, weakTapped = true)
        val plan = pod.declare(advisor(threatAware = true))

        plan.attackers.values.toSet() shouldBe setOf(pod.weak)
        pod.driver.submitSuccess(plan)
    }

    test("without the runaway board, the seat whose creatures threaten most is the target") {
        // No leader board: the weak seat's 6 power is now the only threat at the table.
        val pod = pod(leaderBoard = false)
        val plan = pod.declare(advisor(threatAware = true))

        plan.attackers.values.toSet() shouldBe setOf(pod.weak)
    }

    test("with the flag off, the plan aims at the lowest life total and never at the leader (the legacy behaviour)") {
        val pod = pod()
        val action = pod.driver.legalActions(pod.me).single { it.actionType == "DeclareAttackers" }
        CombatSeed.primaryDefender(pod.driver.state, action) shouldBe pod.weak
        CombatSeed.primaryDefender(pod.driver.state, action, threatAware = true) shouldBe pod.leader

        val plan = pod.declare(advisor(threatAware = false))
        plan.attackers.values shouldNotContain pod.leader
    }

    test("LIVE ships the flag") {
        AiProfile.LIVE.threatAwareDefender shouldBe true
    }
})
