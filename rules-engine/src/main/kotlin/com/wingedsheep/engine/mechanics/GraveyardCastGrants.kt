package com.wingedsheep.engine.mechanics

import com.wingedsheep.engine.handlers.PredicateContext
import com.wingedsheep.engine.handlers.PredicateEvaluator
import com.wingedsheep.engine.registry.CardRegistry
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.components.battlefield.controlledActiveStaticAbilities
import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.GraveyardCardsHaveCastKeyword
import com.wingedsheep.sdk.scripting.StaticAbility

/**
 * The battlefield graveyard-cast grants ([GraveyardCardsHaveCastKeyword], `GraveyardCardsHaveEscape`)
 * [controllerId] holds that reach [cardId] right now — the one scan [FlashbackGrants],
 * [MayhemGrants], [EscapeCasts] and [BeamMeUp] share, so a grant behaves the same whichever keyword
 * it carries.
 *
 * Reads the controlled view (CR 109.5: "your graveyard" follows the granter's controller), the
 * live statics (Class levels), skips face-down granters and ones that lost all abilities, resolves
 * `staticAbility { condition = … }` against the granter, applies `duringYourTurnOnly`, and matches
 * the grant's filter with the granter as the source.
 */
object GraveyardCastGrants {

    inline fun <reified T : StaticAbility> reaching(
        state: GameState,
        cardId: EntityId,
        controllerId: EntityId,
        cardRegistry: CardRegistry,
        predicateEvaluator: PredicateEvaluator,
        crossinline filterOf: (T) -> GameObjectFilter,
        crossinline yourTurnOnly: (T) -> Boolean,
    ): Sequence<Pair<EntityId, T>> {
        val yourTurn = state.isActiveTurnFor(controllerId)
        return state.controlledActiveStaticAbilities(controllerId, cardRegistry, predicateEvaluator.conditions)
            .mapNotNull { (sourceId, ability) -> (ability as? T)?.let { sourceId to it } }
            .filter { (_, grant) -> yourTurn || !yourTurnOnly(grant) }
            .filter { (sourceId, grant) ->
                predicateEvaluator.matches(
                    state, state.projectedState, cardId, filterOf(grant),
                    PredicateContext(sourceId = sourceId, controllerId = controllerId),
                )
            }
    }

    /** [GraveyardCardsHaveCastKeyword] grants of keyword [K] reaching [cardId]. */
    inline fun <reified K> castKeyword(
        state: GameState,
        cardId: EntityId,
        controllerId: EntityId,
        cardRegistry: CardRegistry,
        predicateEvaluator: PredicateEvaluator,
    ): Sequence<Pair<EntityId, GraveyardCardsHaveCastKeyword>> =
        reaching<GraveyardCardsHaveCastKeyword>(
            state, cardId, controllerId, cardRegistry, predicateEvaluator,
            filterOf = { it.filter }, yourTurnOnly = { it.duringYourTurnOnly },
        ).filter { (_, grant) -> grant.keyword is K }
}
