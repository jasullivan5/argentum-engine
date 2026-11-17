package com.wingedsheep.ai.draftsim

import com.wingedsheep.ai.engine.LimitedCardRater
import com.wingedsheep.sdk.model.CardDefinition

/**
 * These tables plus a rating for every nonland card in [cards] they leave unrated, from
 * [LimitedCardRater.rate] — the same card's rating in another set's table when it is a reprint,
 * else the effect-tree heuristic over its [CardDefinition].
 *
 * Without this a set we ship no ratings file for (a new or custom set) is scored entirely off the
 * rarity ladder: every common ties with every other common, so a vanilla bear and the set's best
 * removal spell are the same pick, and the deckbuilder can't tell a playable from a blank. Lands are
 * left to Draftsim's own land handling. Returns the receiver unchanged when nothing is missing.
 */
fun DraftsimSetTables.withFallbackRatings(cards: Collection<CardDefinition>): DraftsimSetTables {
    val missing = LinkedHashMap<String, Double>()
    for (card in cards) {
        if (card.typeLine.isLand) continue
        val key = DraftsimData.nameKey(card.name)
        if (key in ratings || key in missing) continue
        missing[key] = LimitedCardRater.rate(card)
    }
    if (missing.isEmpty()) return this
    return copy(ratings = ratings + missing)
}
