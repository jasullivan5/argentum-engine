package com.wingedsheep.gameserver.session

import com.wingedsheep.engine.limited.BoosterGenerator
import com.wingedsheep.gameserver.lobby.LobbyGameMode
import com.wingedsheep.gameserver.lobby.TournamentFormat
import com.wingedsheep.gameserver.lobby.TournamentLobby
import com.wingedsheep.gameserver.repository.InMemoryGameRepository
import com.wingedsheep.gameserver.repository.InMemoryLobbyRepository
import com.wingedsheep.sdk.core.Subtype
import com.wingedsheep.sdk.model.CardDefinition
import com.wingedsheep.sdk.model.EntityId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.web.socket.WebSocketSession
import java.time.Duration
import java.time.Instant

/**
 * A lobby whose humans all left used to stay in memory until the next deploy whenever no disconnect
 * timer closed it — any started lobby, and every lobby recovered after a restart.
 * [ZombieSessionSweeper.sweepUnattendedLobbies] closes one once no human has been connected for
 * [AbandonedLobbyPolicy.UNATTENDED_LIMIT].
 */
class UnattendedLobbySweepTest : FunSpec({
    val t0 = Instant.parse("2026-10-30T12:00:00Z")
    fun at(minutes: Long): Instant = t0 + Duration.ofMinutes(minutes)

    val generator = BoosterGenerator(
        mapOf(
            "TST" to BoosterGenerator.SetConfig(
                setCode = "TST",
                setName = "Test",
                cards = emptyList(),
                basicLands = listOf(CardDefinition.basicLand("Forest", Subtype.FOREST)),
            )
        )
    )

    fun human(id: String) = PlayerIdentity(playerId = EntityId(id), playerName = id)
    fun ai(id: String) = PlayerIdentity(playerId = EntityId(id), playerName = "[AI] $id", isAi = true)
    fun openSocket(): WebSocketSession = mockk(relaxed = true) { every { isOpen } returns true }

    /** A Free-for-All pod that has started, seating [seats]. */
    fun startedPod(vararg seats: PlayerIdentity) = TournamentLobby(
        setCodes = listOf("TST"),
        setNames = listOf("Test"),
        boosterGenerator = generator,
        format = TournamentFormat.PREMADE_DECKS,
        maxPlayers = 6,
        gameMode = LobbyGameMode.FREE_FOR_ALL,
    ).apply {
        seats.forEach { addPlayer(it) }
        activatePremadeTournament()
    }

    fun sweeperWith(lobbies: InMemoryLobbyRepository) = ZombieSessionSweeper(
        sessionRegistry = SessionRegistry(),
        gameRepository = InMemoryGameRepository(),
        lobbyRepository = lobbies,
        gamePlayHandler = mockk(relaxed = true),
        lobbySharedContext = mockk(relaxed = true),
        tournamentResultSink = mockk(relaxed = true),
    )

    test("a started pod whose human left is closed once the unattended limit passes") {
        val lobbies = InMemoryLobbyRepository()
        val lobby = startedPod(human("h"), ai("a1"), ai("a2"))
        lobbies.saveLobby(lobby)
        val sweeper = sweeperWith(lobbies)

        sweeper.sweepUnattendedLobbies(t0)
        lobby.unattendedSince shouldBe t0
        sweeper.sweepUnattendedLobbies(at(29))
        lobbies.findLobbyById(lobby.lobbyId).shouldNotBeNull()

        sweeper.sweepUnattendedLobbies(at(30))
        lobbies.findLobbyById(lobby.lobbyId).shouldBeNull()
    }

    test("a human reconnecting resets the clock") {
        val lobbies = InMemoryLobbyRepository()
        val h = human("h")
        val lobby = startedPod(h, ai("a1"))
        lobbies.saveLobby(lobby)
        val sweeper = sweeperWith(lobbies)

        sweeper.sweepUnattendedLobbies(t0)
        h.webSocketSession = openSocket()
        sweeper.sweepUnattendedLobbies(at(20))
        lobby.unattendedSince.shouldBeNull()

        h.webSocketSession = null
        sweeper.sweepUnattendedLobbies(at(25))
        sweeper.sweepUnattendedLobbies(at(50))
        lobbies.findLobbyById(lobby.lobbyId).shouldNotBeNull()
        sweeper.sweepUnattendedLobbies(at(55))
        lobbies.findLobbyById(lobby.lobbyId).shouldBeNull()
    }

    test("an AI-only lobby is left running") {
        val lobbies = InMemoryLobbyRepository()
        val lobby = startedPod(ai("a1"), ai("a2"))
        lobbies.saveLobby(lobby)
        val sweeper = sweeperWith(lobbies)

        sweeper.sweepUnattendedLobbies(t0)
        sweeper.sweepUnattendedLobbies(at(24 * 60))
        lobbies.findLobbyById(lobby.lobbyId).shouldNotBeNull()
        lobby.unattendedSince.shouldBeNull()
    }

    test("a gathering lobby whose host never came back after a restart is closed too") {
        val lobbies = InMemoryLobbyRepository()
        val lobby = TournamentLobby(
            setCodes = listOf("TST"),
            setNames = listOf("Test"),
            boosterGenerator = generator,
            format = TournamentFormat.PREMADE_DECKS,
            maxPlayers = 6,
            gameMode = LobbyGameMode.FREE_FOR_ALL,
        ).apply {
            addPlayer(human("h"))
            addPlayer(ai("a1"))
        }
        lobbies.saveLobby(lobby)
        val sweeper = sweeperWith(lobbies)

        sweeper.sweepUnattendedLobbies(t0)
        sweeper.sweepUnattendedLobbies(at(30))
        lobbies.findLobbyById(lobby.lobbyId).shouldBeNull()
    }
})
