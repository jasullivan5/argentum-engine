package com.wingedsheep.gameserver.lobby

import com.wingedsheep.engine.limited.BoosterGenerator
import com.wingedsheep.gameserver.session.PlayerIdentity
import com.wingedsheep.sdk.core.Subtype
import com.wingedsheep.sdk.model.CardDefinition
import com.wingedsheep.sdk.model.EntityId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Who hosts a tournament lobby once its creator is gone. An AI seat can't start or configure a
 * lobby, so a lobby "hosted" by one sat in the public list forever with nobody able to start it.
 */
class TournamentLobbyHostTest : FunSpec({
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

    fun lobby() = TournamentLobby(
        setCodes = listOf("TST"),
        setNames = listOf("Test"),
        boosterGenerator = generator,
        format = TournamentFormat.PREMADE_DECKS,
        maxPlayers = 6,
        gameMode = LobbyGameMode.FREE_FOR_ALL,
    )

    fun human(id: String) = PlayerIdentity(playerId = EntityId(id), playerName = id)
    fun ai(id: String) = PlayerIdentity(playerId = EntityId(id), playerName = "[AI] $id", isAi = true)

    test("host passes to a remaining human, never to an AI seat") {
        val lobby = lobby()
        lobby.addPlayer(human("creator"))
        // Several AIs so a hash-ordered "first seat" would very likely be one of them.
        listOf("a1", "a2", "a3", "a4").forEach { lobby.addPlayer(ai(it)) }
        lobby.addPlayer(human("guest"))

        lobby.forceRemovePlayer(EntityId("creator"))

        lobby.hostPlayerId shouldBe EntityId("guest")
        lobby.isAbandonedByHumans shouldBe false
    }

    test("the last human leaving a lobby of AIs abandons it") {
        val lobby = lobby()
        lobby.addPlayer(human("creator"))
        lobby.addPlayer(ai("a1"))
        lobby.addPlayer(ai("a2"))

        lobby.removePlayer(EntityId("creator"))

        lobby.isAbandonedByHumans shouldBe true
        lobby.vacateSeats().map { it.playerId } shouldBe lobby.players.keys.toList()
    }

    test("a human joining a lobby an AI ended up hosting takes the host seat") {
        val lobby = lobby()
        lobby.addPlayer(human("creator"))
        lobby.addPlayer(ai("a1"))
        lobby.removePlayer(EntityId("creator"))
        lobby.hostPlayerId shouldBe EntityId("a1")

        lobby.addPlayer(human("guest"))

        lobby.hostPlayerId shouldBe EntityId("guest")
    }

    test("repairHost hands an AI-held host seat to a seated human") {
        val lobby = lobby()
        lobby.addPlayer(ai("a1"))
        lobby.addPlayer(ai("a2"))
        lobby.players[EntityId("guest")] = LobbyPlayerState(identity = human("guest"))

        lobby.repairHost() shouldBe true
        lobby.hostPlayerId shouldBe EntityId("guest")
        lobby.repairHost() shouldBe false
    }

    test("an AI-only lobby keeps an AI host") {
        val lobby = lobby()
        lobby.addPlayer(ai("a1"))
        lobby.addPlayer(ai("a2"))

        lobby.hostPlayerId shouldBe EntityId("a1")
        lobby.repairHost() shouldBe false
    }
})
