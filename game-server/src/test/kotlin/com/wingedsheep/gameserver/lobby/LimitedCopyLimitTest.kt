package com.wingedsheep.gameserver.lobby

import com.wingedsheep.engine.limited.BoosterGenerator
import com.wingedsheep.engine.registry.CardRegistry
import com.wingedsheep.gameserver.persistence.restoreTournamentLobby
import com.wingedsheep.gameserver.persistence.toPersistent
import com.wingedsheep.gameserver.session.PlayerIdentity
import com.wingedsheep.sdk.core.ManaCost
import com.wingedsheep.sdk.core.Subtype
import com.wingedsheep.sdk.model.CardDefinition
import com.wingedsheep.sdk.model.EntityId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

/**
 * Limited decks may play every copy of a card the player opened or drafted; the 4-of cap is a
 * host-set lobby option ([TournamentLobby.limitedCopyLimit]), off by default.
 */
class LimitedCopyLimitTest : FunSpec({
    val host = EntityId("host")

    fun bear(n: Int) = CardDefinition.creature(
        name = "Bear $n",
        manaCost = ManaCost.parse("{1}{G}"),
        subtypes = setOf(Subtype.BEAR),
        power = 2,
        toughness = 2,
    )

    /** A pool holding six copies of Bear 1, and a deck that plays all six. */
    fun lobby(copyLimit: Boolean? = null): TournamentLobby {
        val lobby = TournamentLobby(
            setCodes = listOf("TST"),
            setNames = listOf("Test"),
            boosterGenerator = BoosterGenerator(emptyMap()),
            format = TournamentFormat.SEALED,
        )
        copyLimit?.let { lobby.limitedCopyLimit = it }
        lobby.addPlayer(PlayerIdentity(playerId = host, playerName = "Host"))
        lobby.players[host] = lobby.players.getValue(host).copy(
            cardPool = List(6) { bear(1) } + (2..30).map(::bear),
        )
        lobby.restoreFromPersistence(LobbyState.DECK_BUILDING, hostPlayerId = host)
        return lobby
    }

    val sixOfDeck = mapOf("Bear 1" to 6) + (2..18).associate { "Bear $it" to 1 } + ("Forest" to 17)

    test("by default every opened copy is playable") {
        lobby().limitedCopyCap shouldBe null

        lobby().submitDeck(host, sixOfDeck) shouldBe TournamentLobby.DeckSubmissionResult.Success(allReady = true)
    }

    test("the pool still bounds the copies") {
        val result = lobby().submitDeck(host, sixOfDeck + ("Bear 1" to 7))

        (result as TournamentLobby.DeckSubmissionResult.Error).message shouldContain
            "Not enough copies of Bear 1 in pool"
    }

    test("the host can turn the 4-of cap on") {
        val lobby = lobby(copyLimit = true)
        lobby.limitedCopyCap shouldBe 4

        val result = lobby.submitDeck(host, sixOfDeck)

        (result as TournamentLobby.DeckSubmissionResult.Error).message shouldBe
            "Cannot have more than 4 copies of Bear 1 (have 6)"
    }

    test("the setting survives persistence") {
        val lobby = lobby(copyLimit = true)

        val (restored, _) = restoreTournamentLobby(lobby.toPersistent(), CardRegistry(), BoosterGenerator(emptyMap()))

        restored.limitedCopyLimit shouldBe true
    }
})
