package com.wingedsheep.gameserver.session

import com.wingedsheep.gameserver.handler.GamePlayHandler
import com.wingedsheep.gameserver.handler.LobbySharedContext
import com.wingedsheep.gameserver.lobby.LobbyState
import com.wingedsheep.gameserver.repository.GameRepository
import com.wingedsheep.gameserver.repository.LobbyRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class ZombieSessionSweeper(
    private val sessionRegistry: SessionRegistry,
    private val gameRepository: GameRepository,
    private val lobbyRepository: LobbyRepository,
    private val gamePlayHandler: GamePlayHandler,
    private val lobbySharedContext: LobbySharedContext,
    private val tournamentResultSink: com.wingedsheep.gameserver.stats.TournamentResultSink
) {
    private val logger = LoggerFactory.getLogger(ZombieSessionSweeper::class.java)

    companion object {
        /** Keep completed tournaments for 30 minutes so the host can add extra rounds. */
        const val TOURNAMENT_COMPLETE_GRACE_PERIOD_MS = 30L * 60 * 1000
    }

    @Scheduled(fixedRate = 60_000)
    fun sweep() {
        sweepAbandonedGames(Instant.now())
        sweepFinishedGames()
        sweepEmptyLobbies()
        sweepUnattendedLobbies(Instant.now())
        sweepDisconnectedIdentities()
        sweepDisconnectedSpectators()
        sweepStaleTrackingEntries()
    }

    /**
     * Drop spectators whose socket has closed. Leaving a game by closing the tab sends no
     * StopSpectating, so the entry would otherwise sit in the session for the rest of the game;
     * re-push the badge to the players whenever a game actually lost one.
     */
    private fun sweepDisconnectedSpectators() {
        for (game in gameRepository.findAll()) {
            if (game.pruneDisconnectedSpectators()) {
                lobbySharedContext.broadcastSpectatorCount(game)
            }
        }
    }

    /**
     * End games nobody is playing any more — see [AbandonedGamePolicy]. Also keeps each game's
     * [GameSession.unattendedSince] clock, which that policy and the admin Live overview read.
     * Runs before [sweepFinishedGames]; the normal game-over path removes what it ends.
     */
    internal fun sweepAbandonedGames(now: Instant) {
        for (game in gameRepository.findAll()) {
            if (game.isGameOver()) continue
            val facts = AbandonedGamePolicy.of(game)
            if (facts.hasConnectedHuman) {
                game.unattendedSince = null
                continue
            }
            if (game.unattendedSince == null) game.unattendedSince = now
            val verdict = AbandonedGamePolicy.verdict(facts.copy(unattendedSince = game.unattendedSince), now) ?: continue
            runCatching { gamePlayHandler.abandonGame(game, verdict) }
                .onFailure { logger.error("Failed to end abandoned game ${game.sessionId}", it) }
        }
    }

    private fun sweepFinishedGames() {
        val finished = gameRepository.findAll().filter { it.isGameOver() }
        for (game in finished) {
            logger.info("Sweeping finished game: ${game.sessionId}")
            gameRepository.remove(game.sessionId)
            gameRepository.removeLobbyLink(game.sessionId)
        }
    }

    internal fun sweepEmptyLobbies() {
        val now = System.currentTimeMillis()
        // Lobbies whose host seat fell to an AI before host transfer preferred humans.
        for (lobby in lobbyRepository.findAllLobbies()) {
            if (lobby.state == LobbyState.WAITING_FOR_PLAYERS && lobby.repairHost()) {
                logger.info("Handed host of lobby ${lobby.lobbyId} back to a human player")
                lobbyRepository.saveLobby(lobby)
                lobbySharedContext.broadcastLobbyUpdate(lobby)
            }
        }
        val empty = lobbyRepository.findAllLobbies().filter { lobby ->
            lobby.playerCount == 0 ||
                lobby.isAbandonedByHumans ||
                (lobby.state == LobbyState.TOURNAMENT_COMPLETE &&
                    lobby.completedAt != null &&
                    now - lobby.completedAt!! > TOURNAMENT_COMPLETE_GRACE_PERIOD_MS)
        }
        for (lobby in empty) {
            logger.info("Sweeping lobby: ${lobby.lobbyId} (state=${lobby.state}, players=${lobby.playerCount})")
            closeLobby(lobby)
        }
    }

    /**
     * Close lobbies whose humans have all been gone for a while — see [AbandonedLobbyPolicy].
     * Also keeps each lobby's [com.wingedsheep.gameserver.lobby.TournamentLobby.unattendedSince] clock.
     */
    internal fun sweepUnattendedLobbies(now: Instant) {
        for (lobby in lobbyRepository.findAllLobbies()) {
            if (!AbandonedLobbyPolicy.applies(lobby.state) || !AbandonedLobbyPolicy.isUnattended(lobby)) {
                lobby.unattendedSince = null
                continue
            }
            if (lobby.unattendedSince == null) lobby.unattendedSince = now
            if (!AbandonedLobbyPolicy.shouldClose(lobby.unattendedSince, now)) continue

            logger.info("Sweeping unattended lobby: ${lobby.lobbyId} (state=${lobby.state}, since=${lobby.unattendedSince})")
            // Close the lobby first, so the games ended below report into nothing instead of
            // pairing the next round of a bracket nobody is in.
            closeLobby(lobby)
            val games = gameRepository.findAll().filter {
                !it.isGameOver() && gameRepository.getLobbyForGame(it.sessionId) == lobby.lobbyId
            }
            for (game in games) {
                runCatching {
                    gamePlayHandler.abandonGame(game, "The game was ended because everyone left its lobby.")
                }.onFailure { logger.error("Failed to end game ${game.sessionId} of unattended lobby ${lobby.lobbyId}", it) }
            }
        }
    }

    private fun closeLobby(lobby: com.wingedsheep.gameserver.lobby.TournamentLobby) {
        // No-op unless this was a still-live tournament with an in-progress stats row.
        lobby.vacateSeats().forEach { sessionRegistry.removeIdentity(it.token) }
        tournamentResultSink.recordAbandoned(lobby.lobbyId)
        lobbyRepository.removeLobby(lobby.lobbyId)
        lobbyRepository.removeTournament(lobby.lobbyId)
    }

    private fun sweepDisconnectedIdentities() {
        val disconnected = sessionRegistry.getAllIdentities().filter { identity ->
            !identity.isConnected &&
                identity.disconnectTimer == null &&
                identity.currentGameSessionId == null &&
                identity.currentLobbyId == null
        }
        for (identity in disconnected) {
            logger.info("Sweeping orphaned identity: ${identity.playerName} (${identity.token})")
            sessionRegistry.removeIdentity(identity.token)
        }
    }

    private fun sweepStaleTrackingEntries() {
        val activeGameIds = gameRepository.findAll().map { it.sessionId }.toSet()
        val activeLobbyIds = lobbyRepository.findAllLobbies().map { it.lobbyId }.toSet()
        gamePlayHandler.sweepStaleEntries(activeGameIds, activeLobbyIds)
        lobbySharedContext.sweepStaleLocks(activeLobbyIds)
    }
}
