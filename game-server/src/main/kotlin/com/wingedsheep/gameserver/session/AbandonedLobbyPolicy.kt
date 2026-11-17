package com.wingedsheep.gameserver.session

import com.wingedsheep.gameserver.lobby.LobbyState
import com.wingedsheep.gameserver.lobby.TournamentLobby
import java.time.Duration
import java.time.Instant

/**
 * When a lobby nobody is in any more gets closed by [ZombieSessionSweeper]. A human's disconnect
 * timer normally does this — but only while gathering: in a started lobby (a Free-for-All pod between
 * games, a draft, deck building, a bracket) a leaver only concedes their seat, and a server restart
 * recovers every identity without a disconnect timer at all, in any state. Those lobbies sat in
 * memory — and gathering ones in the public lobby list — until the next deploy.
 *
 * The rule: the lobby has human seats and none of them has been connected for [UNATTENDED_LIMIT].
 * Lobbies without a human seat (dev AI tournaments) are exempt — being AI-only is their point. The
 * limit sits far beyond the disconnect grace period, so a player who refreshes or briefly drops
 * between games never loses their lobby.
 */
object AbandonedLobbyPolicy {
    val UNATTENDED_LIMIT: Duration = Duration.ofMinutes(30)

    /** A finished lobby has its own grace period ([ZombieSessionSweeper.TOURNAMENT_COMPLETE_GRACE_PERIOD_MS]). */
    fun applies(state: LobbyState): Boolean = state != LobbyState.TOURNAMENT_COMPLETE

    /** Whether [lobby] currently counts as unattended: it has human seats and none is connected. */
    fun isUnattended(lobby: TournamentLobby): Boolean {
        val humans = lobby.players.values.map { it.identity }.filter { !it.isAi }
        return humans.isNotEmpty() && humans.none { it.isConnected }
    }

    /** Whether a lobby unattended since [unattendedSince] should be closed at [now]. */
    fun shouldClose(unattendedSince: Instant?, now: Instant): Boolean =
        unattendedSince != null && unattendedSince + UNATTENDED_LIMIT <= now
}
