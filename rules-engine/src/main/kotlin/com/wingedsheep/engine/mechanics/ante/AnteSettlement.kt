package com.wingedsheep.engine.mechanics.ante

import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.state.components.identity.OriginalOwnerComponent
import com.wingedsheep.engine.state.components.identity.OwnerComponent
import com.wingedsheep.engine.state.components.identity.TokenComponent
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.model.EntityId

/** Why a card changed hands for keeps. */
enum class AnteTransferCause {
    /** It was in the ante when the game ended, and the winner took it (CR 407.2). */
    ANTE,

    /** An ante card's effect changed its owner during the game (CR 407.3 — Darkpact, Bronze Tablet). */
    OWNERSHIP,
}

/**
 * One card that leaves [fromPlayerId]'s collection for [toPlayerId]'s when a game played for
 * ante is settled.
 */
data class AnteTransfer(
    val cardId: EntityId,
    val cardName: String,
    val imageUri: String?,
    val fromPlayerId: EntityId,
    val toPlayerId: EntityId,
    val cause: AnteTransferCause,
)

/**
 * Settles a finished game played for ante: which cards end up owned by someone other than the
 * player whose deck they started in. Ante is played "for keeps" (CR 407.1), so this is what the
 * server carries out of the game — into a Limited tournament's pools, or a player's spoils.
 *
 * Two things move a card:
 * - **The ante pot.** "At the end of the game, the winner becomes the owner of all the cards in
 *   the ante zone" (CR 407.2). With no winner — a draw — nobody takes it, and every card in the
 *   ante stays with whoever owns it.
 * - **Ownership changes during the game.** Darkpact and Bronze Tablet change a card's owner
 *   (CR 407.3); `ChangeOwnershipExecutor` remembers the first owner on the card.
 *
 * Both reduce to one comparison per card: its owner at the end, after the winner takes the ante,
 * against its owner when the game began. A card that changed hands and came back reports nothing.
 * Tokens are never owned "for keeps", so they're skipped.
 */
object AnteSettlement {

    fun settle(state: GameState): List<AnteTransfer> {
        if (!state.playingForAnte) return emptyList()
        val winnerId = state.winnerId.takeIf { state.gameOver }
        val inAnte = state.zones
            .filterKeys { it.zoneType == Zone.ANTE }
            .values.flatten().toSet()

        return state.entities.mapNotNull { (id, container) ->
            val card = container.get<CardComponent>() ?: return@mapNotNull null
            if (container.has<TokenComponent>()) return@mapNotNull null
            val owner = container.get<OwnerComponent>()?.playerId ?: card.ownerId ?: return@mapNotNull null
            val original = container.get<OriginalOwnerComponent>()?.playerId ?: owner
            val wonFromAnte = winnerId != null && id in inAnte
            val finalOwner = if (wonFromAnte) winnerId else owner
            if (finalOwner == original) return@mapNotNull null
            AnteTransfer(
                cardId = id,
                cardName = card.name,
                imageUri = card.imageUri,
                fromPlayerId = original,
                toPlayerId = finalOwner,
                cause = if (wonFromAnte) AnteTransferCause.ANTE else AnteTransferCause.OWNERSHIP,
            )
        }.sortedWith(compareBy({ it.cause }, { it.cardName }))
    }
}
