package com.wingedsheep.engine.mechanics

import com.wingedsheep.engine.handlers.PredicateEvaluator
import com.wingedsheep.engine.registry.CardRegistry
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.sdk.model.CardDefinition
import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.scripting.GraveyardCardsHaveEscape
import com.wingedsheep.sdk.scripting.KeywordAbility

/** Escape costs available from printed abilities, individual grants, and battlefield statics. */
object EscapeCasts {
    fun printedEscape(cardDef: CardDefinition?): KeywordAbility.Escape? =
        cardDef?.keywordAbilities?.filterIsInstance<KeywordAbility.Escape>()?.firstOrNull()

    fun availableEscape(
        state: GameState,
        cardId: EntityId,
        cardDef: CardDefinition?,
        controllerId: EntityId,
        cardRegistry: CardRegistry,
        predicateEvaluator: PredicateEvaluator,
    ): List<KeywordAbility.Escape> = buildList {
        addAll(cardDef?.keywordAbilities?.filterIsInstance<KeywordAbility.Escape>().orEmpty())
        for (grant in state.grantedKeywordAbilities) {
            if (grant.entityId == cardId && grant.ability is KeywordAbility.Escape) add(grant.ability)
        }
        val cardComponent = state.getEntity(cardId)?.get<CardComponent>() ?: return@buildList
        val grants = GraveyardCastGrants.reaching<GraveyardCardsHaveEscape>(
            state, cardId, controllerId, cardRegistry, predicateEvaluator,
            filterOf = { it.filter }, yourTurnOnly = { it.duringYourTurnOnly },
        )
        for ((_, grant) in grants) {
            val cost = grant.cost ?: if (cardDef?.hasNoManaCost == true) continue else cardComponent.manaCost
            add(KeywordAbility.Escape(cost, grant.additionalCost))
        }
    }.distinct()

    /** Unspecified actions retain the first option; explicit selections must still be available. */
    fun effectiveEscape(
        state: GameState,
        cardId: EntityId,
        cardDef: CardDefinition?,
        controllerId: EntityId,
        cardRegistry: CardRegistry,
        predicateEvaluator: PredicateEvaluator,
        selected: KeywordAbility.Escape? = null,
    ): KeywordAbility.Escape? {
        val available = availableEscape(state, cardId, cardDef, controllerId, cardRegistry, predicateEvaluator)
        return if (selected == null) available.firstOrNull() else available.firstOrNull { it == selected }
    }
}
