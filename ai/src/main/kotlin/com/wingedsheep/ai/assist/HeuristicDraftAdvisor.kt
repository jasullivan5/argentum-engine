package com.wingedsheep.ai.assist

import com.wingedsheep.ai.engine.LimitedPickScorer

/**
 * Keyword-heuristic draft engine: scores every card in the pack with the color-aware
 * [LimitedPickScorer]. Instant and free — no model or network call. The AI bot picks with the
 * Draftsim scorer instead, so [DraftsimDraftAdvisor] is the one that matches what a bot would take.
 */
object HeuristicDraftAdvisor : DraftAdvisor {
    override val id = "heuristic"
    override val displayName = "Heuristic"

    override fun suggestPick(request: DraftPickAdviceRequest): DraftPickAdvice {
        val committed = LimitedPickScorer.inferColors(request.pickedSoFar)

        val rated = request.pack.map { card ->
            val raw = LimitedPickScorer.score(card, committed, request.pickedSoFar)
            Triple(card, raw, LimitedPickScorer.reason(card, committed, request.pickedSoFar))
        }.sortedByDescending { it.second }

        val scores = rated.map { (card, raw, reason) ->
            CardScore(cardName = card.name, score = normalize(raw), reason = reason)
        }
        val recommended = rated.take(request.picksRequired.coerceAtLeast(1)).map { it.first.name }

        return DraftPickAdvice(advisorId = id, scores = scores, recommended = recommended)
    }

    /**
     * Map the scorer's unbounded raw value to a 0–100 display scale. The raw range runs roughly from
     * a rarity floor (~2) to a removal-laden bomb (~24); this clamps and stretches that band so a
     * bomb reads ~90+ and filler ~40.
     */
    private fun normalize(raw: Double): Int =
        (((raw - 2.0) / 22.0) * 100.0).coerceIn(0.0, 100.0).toInt()
}
