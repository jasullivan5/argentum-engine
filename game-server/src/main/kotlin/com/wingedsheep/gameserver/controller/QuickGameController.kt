package com.wingedsheep.gameserver.controller

import com.wingedsheep.gameserver.lobby.QuickGameLobby
import com.wingedsheep.gameserver.lobby.QuickGameLobbyRepository
import com.wingedsheep.gameserver.repository.GameRepository
import com.wingedsheep.gameserver.session.SessionRegistry
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Public-facing browse endpoint for quick-game lobbies. Mirrors [TournamentController.listPublic]
 * so the home screen can merge tournaments and quick games into a single "Public Lobbies" list.
 *
 * AI lobbies and full lobbies are excluded — there's no second seat to join.
 */
@RestController
@RequestMapping("/api/quick-games")
class QuickGameController(
    private val lobbyRepository: QuickGameLobbyRepository,
    private val gameRepository: GameRepository,
    private val sessionRegistry: SessionRegistry,
) {

    data class PublicQuickGameDTO(
        val lobbyId: String,
        val playerCount: Int,
        val maxPlayers: Int,
        val setCode: String?,
        val hostName: String?,
        val format: String? = null,
        /** The host's account avatar id, or null for a guest — drawn beside "Hosted by". */
        val hostAvatar: String? = null,
        /** Seated players in join order, host first. */
        val playerNames: List<String> = emptyList(),
        val twoHeadedGiant: Boolean = false,
        val ranked: Boolean = false,
        val momirBasic: Boolean = false,
        /** Seconds the lobby has been open, measured on the server so client clock skew can't distort it. */
        val openSeconds: Long = 0,
    )

    data class LiveQuickGameDTO(
        val gameSessionId: String,
        val player1Name: String,
        val player2Name: String,
        val player1Life: Int,
        val player2Life: Int,
    )

    @GetMapping("/public")
    fun listPublic(): ResponseEntity<List<PublicQuickGameDTO>> {
        val now = System.currentTimeMillis()
        val publicLobbies = lobbyRepository.findAll()
            .filter { lobby -> lobby.isPublic && !lobby.vsAi && !lobby.started && !lobby.isFull }
            .sortedBy { it.lobbyId }
        // Quick-game seats carry no avatar of their own; the connected identity does.
        val avatarsByPlayerId = if (publicLobbies.isEmpty()) emptyMap() else
            sessionRegistry.getAllIdentities().associate { it.playerId to it.avatar }
        val dtos = publicLobbies
            .map { lobby ->
                val host = lobby.players.firstOrNull { !it.isAi }
                PublicQuickGameDTO(
                    lobbyId = lobby.lobbyId,
                    playerCount = lobby.players.count { !it.isAi },
                    // Reflects the lobby's actual seat count: 4 for Two-Headed Giant, else 2.
                    maxPlayers = lobby.maxPlayers,
                    setCode = lobby.setCode ?: host?.setCode,
                    hostName = host?.playerName,
                    format = lobby.format?.name,
                    hostAvatar = host?.let { avatarsByPlayerId[it.playerId] },
                    playerNames = lobby.players.filter { !it.isAi }.map { it.playerName },
                    twoHeadedGiant = lobby.twoHeadedGiant,
                    ranked = lobby.ranked,
                    momirBasic = lobby.momirBasic,
                    openSeconds = (now - lobby.createdAt).coerceAtLeast(0) / 1000,
                )
            }
        return ResponseEntity.ok(dtos)
    }

    /**
     * In-progress public quick games. Powers the Live Games section on the landing page so
     * anonymous visitors can drop in as a spectator. Tournament matches are exposed separately
     * by [TournamentController.listLive].
     */
    @GetMapping("/live")
    fun listLive(): ResponseEntity<List<LiveQuickGameDTO>> {
        val live = gameRepository.findAll()
            .filter { it.publicSpectate && !it.isGameOver() }
            .mapNotNull { session ->
                // Quick games are 2-player; the Live Games tile shows the two seats.
                val names = session.getPlayerNames()
                val life = session.getLifeTotals()
                if (names.size < 2 || life.size < 2) return@mapNotNull null
                LiveQuickGameDTO(
                    gameSessionId = session.sessionId,
                    player1Name = names[0],
                    player2Name = names[1],
                    player1Life = life[0],
                    player2Life = life[1],
                )
            }
            .sortedBy { it.gameSessionId }
        return ResponseEntity.ok(live)
    }
}
