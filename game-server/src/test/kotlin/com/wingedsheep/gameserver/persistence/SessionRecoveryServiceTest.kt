package com.wingedsheep.gameserver.persistence

import com.wingedsheep.engine.limited.BoosterGenerator
import com.wingedsheep.gameserver.ai.AiGameManager
import com.wingedsheep.gameserver.lobby.LobbyPlayerState
import com.wingedsheep.gameserver.lobby.TournamentLobby
import com.wingedsheep.gameserver.repository.RedisGameRepository
import com.wingedsheep.gameserver.repository.RedisLobbyRepository
import com.wingedsheep.gameserver.session.GameSession
import com.wingedsheep.gameserver.session.PlayerIdentity
import com.wingedsheep.gameserver.session.SessionRegistry
import com.wingedsheep.sdk.model.EntityId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk

/**
 * A server restart (every deploy) restores a tournament player who was mid-match from two Redis
 * blobs — the game's and the lobby's — and each builds its own [PlayerIdentity] for the same token.
 * The lobby must end up holding the instance the registry hands to the reconnecting socket, or the
 * player reads as disconnected to the tournament forever and drops out of it.
 */
class SessionRecoveryServiceTest : FunSpec({

    fun identity(token: String, playerId: EntityId, lobbyId: String? = null, gameId: String? = null) =
        PlayerIdentity(token = token, playerId = playerId, playerName = "Alice").also {
            it.currentLobbyId = lobbyId
            it.currentGameSessionId = gameId
        }

    fun lobbyWith(vararg seats: PlayerIdentity): TournamentLobby {
        val lobby = TournamentLobby(
            lobbyId = "lobby-1",
            setCodes = listOf("BLB"),
            setNames = listOf("Bloomburrow"),
            boosterGenerator = mockk<BoosterGenerator>(relaxed = true),
        )
        seats.forEach { lobby.players[it.playerId] = LobbyPlayerState(identity = it) }
        return lobby
    }

    fun recover(
        registry: SessionRegistry,
        lobbies: List<Pair<TournamentLobby, List<PlayerIdentity>>>,
        games: List<Pair<GameSession, List<PlayerIdentity>>>,
    ) {
        val lobbyRepo = mockk<RedisLobbyRepository> {
            every { loadAllLobbiesFromRedis() } returns lobbies
            every { loadAllTournamentsFromRedis() } returns emptyMap()
        }
        val gameRepo = mockk<RedisGameRepository> { every { loadAllFromRedis() } returns games }
        SessionRecoveryService(gameRepo, lobbyRepo, registry, mockk<AiGameManager>(relaxed = true))
            .recoverSessions()
    }

    test("a player mid-match keeps one identity shared by the registry and the tournament lobby") {
        val playerId = EntityId("p1")
        val lobbyCopy = identity("tok", playerId, lobbyId = "lobby-1")
        val gameCopy = identity("tok", playerId, lobbyId = "lobby-1", gameId = "game-1")
        val lobby = lobbyWith(lobbyCopy)
        val registry = SessionRegistry()

        recover(registry, lobbies = listOf(lobby to listOf(lobbyCopy)), games = listOf(mockk<GameSession>() to listOf(gameCopy)))

        val registered = registry.getIdentityByToken("tok")!!
        lobby.players[playerId]!!.identity shouldBeSameInstanceAs registered
        registered.currentLobbyId shouldBe "lobby-1"
        registered.currentGameSessionId shouldBe "game-1"
        registry.destroy()
    }

    test("a lobby seat whose token another lobby registered first is rebound to that identity") {
        val playerId = EntityId("p1")
        val firstCopy = identity("tok", playerId, lobbyId = "lobby-0")
        val secondCopy = identity("tok", playerId, lobbyId = "lobby-1")
        val first = lobbyWith(firstCopy)
        val second = lobbyWith(secondCopy)
        val registry = SessionRegistry()

        recover(registry, lobbies = listOf(first to listOf(firstCopy), second to listOf(secondCopy)), games = emptyList())

        val registered = registry.getIdentityByToken("tok")!!
        first.players[playerId]!!.identity shouldBeSameInstanceAs registered
        second.players[playerId]!!.identity shouldBeSameInstanceAs registered
        registry.destroy()
    }
})
