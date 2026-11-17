package com.wingedsheep.engine.mechanics

import com.wingedsheep.engine.core.CastSpell
import com.wingedsheep.engine.handlers.PredicateContext
import com.wingedsheep.engine.handlers.PredicateEvaluator
import com.wingedsheep.engine.registry.CardRegistry
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.sdk.core.AbilityFlag
import com.wingedsheep.sdk.core.ManaCost
import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.scripting.GraveyardCastKeyword
import com.wingedsheep.sdk.scripting.KeywordAbility

/** Shared permission, pricing and return pools for printed and continuously granted beam me up. */
object BeamMeUp {
    /**
     * One way to beam a card up: its [cost], and the [creatures] that may be returned to pay it.
     * [sourceId] is the permanent granting it (Scotty), or null for the card's printed beam me up.
     */
    data class Offer(val sourceId: EntityId?, val cost: ManaCost, val creatures: List<EntityId>)

    /** The offer a beam-me-up cast chose, matched on its echoed [CastSpell.beamMeUpSource]. */
    fun selected(state: GameState, action: CastSpell, registry: CardRegistry, predicates: PredicateEvaluator): Offer? =
        options(state, action.cardId, action.playerId, registry, predicates).firstOrNull { it.sourceId == action.beamMeUpSource }

    fun options(state: GameState, cardId: EntityId, playerId: EntityId, registry: CardRegistry, predicates: PredicateEvaluator): List<Offer> {
        if (cardId !in state.getGraveyard(playerId)) return emptyList()
        val card = state.getEntity(cardId)?.get<CardComponent>() ?: return emptyList()
        // "Only instant and sorcery spells can be cast this way" (Scotty's reminder text).
        if (!card.typeLine.isInstant && !card.typeLine.isSorcery) return emptyList()
        val definition = registry.getCard(card.cardDefinitionId) ?: return emptyList()
        val printed = definition.keywordAbilities.filterIsInstance<KeywordAbility.BeamMeUp>()
        val grants = GraveyardCastGrants.castKeyword<GraveyardCastKeyword.BeamMeUp>(state, cardId, playerId, registry, predicates)
            .filterNot { (_, grant) -> grant.cost == null && definition.hasNoManaCost }
            .toList()
        if (printed.isEmpty() && grants.isEmpty()) return emptyList()

        val projected = state.projectedState
        val creatures = state.controlledBattlefield(playerId).filter {
            projected.isCreature(it) && !projected.hasKeyword(it, AbilityFlag.CANT_BE_BEAMED_UP)
        }
        return printed.map { Offer(null, it.cost, creatures) } + grants.map { (sourceId, grant) ->
            val returnFilter = (grant.keyword as GraveyardCastKeyword.BeamMeUp).returnFilter
            val context = PredicateContext(sourceId = sourceId, controllerId = playerId)
            Offer(sourceId, grant.cost ?: card.manaCost, creatures.filter { predicates.matches(state, projected, it, returnFilter, context) })
        }
    }
}
