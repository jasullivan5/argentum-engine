package com.wingedsheep.gameserver.session

import com.wingedsheep.gameserver.ScenarioTestBase
import com.wingedsheep.sdk.model.EntityId
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.web.socket.WebSocketSession

/**
 * Full Control asks a player about every choice, so it also lets them order their own triggers
 * (CR 603.3b) — whatever their "order my triggers automatically" preference says. Leaving Full
 * Control goes back to that preference.
 */
class FullControlTriggerOrderingTest : ScenarioTestBase() {

    private fun mockWs(id: String): WebSocketSession =
        mockk(relaxed = true) { every { this@mockk.id } returns id }

    private fun session(): Pair<GameSession, EntityId> {
        val session = GameSession(cardRegistry = cardRegistry, maxPlayers = 2)
        val alice = EntityId.of("fc-alice")
        val deck = mapOf("Forest" to 30, "Grizzly Bears" to 10)
        session.addPlayer(PlayerSession(mockWs("fc-ws1"), alice, "Alice"), deck)
        session.addPlayer(PlayerSession(mockWs("fc-ws2"), EntityId.of("fc-bob"), "Bob"), deck)
        session.startGame()
        return session to alice
    }

    private fun GameSession.ordersOwn(player: EntityId) = getStateForTesting()!!.ordersOwnTriggers(player)

    init {
        test("Full Control orders your own triggers, and leaving it restores automatic ordering") {
            val (session, alice) = session()
            session.ordersOwn(alice) shouldBe false
            session.setPriorityMode(alice, GameSession.PriorityMode.FULL_CONTROL)
            session.ordersOwn(alice) shouldBe true
            session.setPriorityMode(alice, GameSession.PriorityMode.STOPS)
            session.ordersOwn(alice) shouldBe false
        }

        test("a player who prefers to order their triggers keeps doing so after leaving Full Control") {
            val (session, alice) = session()
            session.setManualTriggerOrdering(alice, true)
            session.setPriorityMode(alice, GameSession.PriorityMode.FULL_CONTROL)
            session.setManualTriggerOrdering(alice, false)
            session.ordersOwn(alice) shouldBe true
            session.setPriorityMode(alice, GameSession.PriorityMode.AUTO)
            session.ordersOwn(alice) shouldBe false
            session.setManualTriggerOrdering(alice, true)
            session.ordersOwn(alice) shouldBe true
        }
    }
}
