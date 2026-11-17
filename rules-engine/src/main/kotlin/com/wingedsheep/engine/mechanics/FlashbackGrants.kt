package com.wingedsheep.engine.mechanics

import com.wingedsheep.engine.handlers.EffectContext
import com.wingedsheep.engine.handlers.PredicateEvaluator
import com.wingedsheep.engine.registry.CardRegistry
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.sdk.model.CardDefinition
import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.scripting.GraveyardCardsHaveCastKeyword
import com.wingedsheep.sdk.scripting.GraveyardCastKeyword
import com.wingedsheep.sdk.scripting.KeywordAbility

/**
 * Single source of truth for "does this card have flashback, and at what cost?" — used by every
 * flashback read site (the cast-from-graveyard enumerator, the cast handler / zone resolver, and
 * the stack resolver's exile-on-resolution clause).
 *
 * Flashback (CR 702.34) can come from three sources, checked in priority order:
 *  1. **Printed** on the card ([KeywordAbility.Flashback] in the card's keyword abilities) —
 *     gated by its optional [KeywordAbility.Flashback.condition] when it has one.
 *  2. **Per-entity runtime grant** to a specific card (Archmage's Newt: "target instant or sorcery
 *     card in your graveyard gains flashback until end of turn").
 *  3. **Whole-graveyard group grant** from a battlefield static ([GraveyardCardsHaveCastKeyword],
 *     Iroh, Grand Lotus: "During your turn, each non-Lesson instant and sorcery card in your
 *     graveyard has flashback …"). This source is only consulted when the optional
 *     [controllerId] / [cardRegistry] / [predicateEvaluator] are supplied.
 *
 * Routing all call sites through here keeps the sources consistent so a granted flashback behaves
 * identically to a printed one (cost, exile on resolution). Mirrors [HarmonizeGrants].
 *
 * The group grant matches on the card's characteristics (type/subtype), which are zone-independent,
 * so it resolves the same whether the card is still in the graveyard (enumeration / cast) or has
 * moved to the stack (exile-on-resolution). It is gated to the controller's turn via
 * [GraveyardCardsHaveCastKeyword.duringYourTurnOnly]; the card must be cast from the graveyard for
 * the exile-on-resolution clause to fire, so a normal hand cast while the granter is in play is
 * unaffected.
 */
object FlashbackGrants {

    /**
     * The effective flashback ability for [cardId], or null if it has none. A printed flashback
     * on [cardDef] wins; then a per-entity runtime grant (a later grant overrides an earlier one
     * for the same card); then, when [controllerId]/[cardRegistry]/[predicateEvaluator] are given,
     * a battlefield group grant ([GraveyardCardsHaveCastKeyword]) controlled by [controllerId].
     */
    fun effectiveFlashback(
        state: GameState,
        cardId: EntityId,
        cardDef: CardDefinition?,
        controllerId: EntityId? = null,
        cardRegistry: CardRegistry? = null,
        predicateEvaluator: PredicateEvaluator? = null,
    ): KeywordAbility.Flashback? {
        cardDef?.keywordAbilities
            ?.firstOrNull { it is KeywordAbility.Flashback }
            ?.let { it as KeywordAbility.Flashback }
            ?.let { printed ->
                if (printedConditionHolds(state, cardId, printed, controllerId, predicateEvaluator)) return printed
            }

        state.grantedKeywordAbilities
            .lastOrNull { it.entityId == cardId && it.ability is KeywordAbility.Flashback }
            ?.let { return it.ability as KeywordAbility.Flashback }

        if (controllerId != null && cardRegistry != null && predicateEvaluator != null) {
            groupGrantFlashback(state, cardId, controllerId, cardRegistry, predicateEvaluator)
                ?.let { return it }
        }
        return null
    }

    /**
     * A printed flashback with no [KeywordAbility.Flashback.condition] always applies. A conditional
     * one (Viral Spawning: "As long as an opponent has three or more poison counters, this card has
     * flashback") applies only while its condition holds, evaluated with the card as the source and
     * [controllerId] — else the card's owner — as "you". With no evaluator to check it, a
     * conditional flashback fails closed.
     */
    private fun printedConditionHolds(
        state: GameState,
        cardId: EntityId,
        printed: KeywordAbility.Flashback,
        controllerId: EntityId?,
        predicateEvaluator: PredicateEvaluator?,
    ): Boolean {
        val condition = printed.condition ?: return true
        val evaluator = predicateEvaluator ?: return false
        val you = controllerId ?: state.getEntity(cardId)?.get<CardComponent>()?.ownerId ?: return false
        return evaluator.conditions.evaluate(state, condition, EffectContext(sourceId = cardId, controllerId = you))
    }

    /**
     * Scan [controllerId]'s battlefield for a [GraveyardCardsHaveCastKeyword] static whose filter
     * matches [cardId] (during their turn, if the grant requires it), synthesizing a
     * [KeywordAbility.Flashback] carrying the granted cost — the grant's fixed [cost], or the
     * card's own mana cost when the grant leaves it null ("equal to that card's mana cost"). The
     * scan itself is [GraveyardCastGrants].
     */
    private fun groupGrantFlashback(
        state: GameState,
        cardId: EntityId,
        controllerId: EntityId,
        cardRegistry: CardRegistry,
        predicateEvaluator: PredicateEvaluator,
    ): KeywordAbility.Flashback? {
        val cardComponent = state.getEntity(cardId)?.get<CardComponent>() ?: return null
        val (_, grant) = GraveyardCastGrants.castKeyword<GraveyardCastKeyword.Flashback>(
            state, cardId, controllerId, cardRegistry, predicateEvaluator
        ).firstOrNull() ?: return null
        return KeywordAbility.Flashback(grant.cost ?: cardComponent.manaCost)
    }
}
