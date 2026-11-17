package com.wingedsheep.ai.engine

import com.wingedsheep.ai.llm.CardSummary

/**
 * Color-aware draft-pick scoring behind the human-facing "Heuristic" "Suggest Pick" advisor
 * ([com.wingedsheep.ai.assist.HeuristicDraftAdvisor]). The AI bot no longer drafts with it — it picks
 * with the Draftsim scorer ([EngineAiPlayerController.chooseDraftPick]), the "Draftsim" advisor's
 * engine — but [parseCmc] is still shared.
 *
 * Operates on [CardSummary], which carries no effect tree, so it reads card quality off rarity and
 * oracle-text keywords. Higher score = better pick. Scores are unbounded raw values (rarity floor
 * ~2, bombs ~22+); the advisor normalizes them to a 0–100 display scale.
 */
object LimitedPickScorer {

    /** Colors committed to so far, tracked by counting colored mana symbols in picked cards. */
    fun inferColors(pickedSoFar: List<CardSummary>): Map<Char, Int> {
        val colorCounts = mutableMapOf<Char, Int>()
        for (card in pickedSoFar) {
            val cost = card.manaCost ?: continue
            for (symbol in listOf('W', 'U', 'B', 'R', 'G')) {
                val count = cost.count { it == symbol }
                if (count > 0) colorCounts[symbol] = (colorCounts[symbol] ?: 0) + count
            }
        }
        return colorCounts
    }

    /**
     * Rate a card for draft picking. Higher = better pick.
     * Considers rarity, creature stats, removal potential, mana curve, and color fit.
     */
    fun score(card: CardSummary, colorCommitment: Map<Char, Int>, pickedSoFar: List<CardSummary>): Double {
        var score = 0.0

        // Rarity bonus
        score += when (card.rarity?.uppercase()) {
            "MYTHIC" -> 12.0
            "RARE" -> 10.0
            "UNCOMMON" -> 6.0
            "COMMON" -> 3.0
            else -> 2.0
        }

        // Creature stats bonus
        if (card.power != null && card.toughness != null) {
            val stats = card.power + card.toughness
            val cmc = parseCmc(card.manaCost ?: "")
            // Efficient creatures score higher (stats relative to cost)
            if (cmc > 0) {
                score += (stats.toDouble() / cmc) * 2.0
            }
            score += 1.0 // Creatures are generally good in limited
        }

        // Removal / interaction bonus (heuristic: check oracle text)
        val oracle = card.oracleText?.lowercase() ?: ""
        if (oracle.contains("destroy") || oracle.contains("exile") || oracle.contains("damage") ||
            oracle.contains("fight") || oracle.contains("-") && oracle.contains("/-")) {
            score += 3.0
        }

        // Evasion bonus
        if (oracle.contains("flying") || oracle.contains("menace") || oracle.contains("trample") ||
            oracle.contains("unblockable") || oracle.contains("can't be blocked")) {
            score += 2.0
        }

        // Card advantage bonus
        if (oracle.contains("draw") || oracle.contains("create") && oracle.contains("token")) {
            score += 2.0
        }

        // Color fit — reward cards that match our committed colors, penalize off-color
        val cardColors = extractColors(card.manaCost)
        if (cardColors.isNotEmpty() && colorCommitment.isNotEmpty()) {
            val topColors = colorCommitment.entries.sortedByDescending { it.value }.take(2).map { it.key }.toSet()
            val onColor = cardColors.all { it in topColors }
            val splashable = cardColors.size == 1 && parseCmc(card.manaCost ?: "") >= 4
            when {
                onColor -> score += 2.0
                splashable -> score += 0.5
                pickedSoFar.size >= 8 -> score -= 3.0  // Penalize off-color after early picks
                else -> {} // Early picks: don't penalize too much, still exploring
            }
        }

        // Mana curve consideration — prefer 2-4 drops in limited
        val cmc = parseCmc(card.manaCost ?: "")
        if (cmc in 2..4) score += 1.0
        if (cmc >= 7) score -= 1.0

        // Lands are generally low priority in draft (you get basics)
        if (card.typeLine?.contains("Land", ignoreCase = true) == true) {
            score -= 2.0
            // But nonbasic dual lands are good
            if (oracle.contains("add") && (oracle.contains("or") || oracle.contains("any color"))) {
                score += 4.0
            }
        }

        return score
    }

    /**
     * A short human-readable justification for a card's pick score — surfaced as the tooltip on the
     * "Suggest Pick" overlay. Picks the single most salient reason rather than itemizing every bonus.
     *
     * Takes [pickedSoFar] so the "Off-color" label matches [score]'s gating: off-color cards are only
     * penalized (and so only flagged) once the player is past the early exploratory picks
     * ([pickedSoFar].size >= 8). Earlier, an off-color card isn't penalized and shouldn't read as one.
     */
    fun reason(card: CardSummary, colorCommitment: Map<Char, Int>, pickedSoFar: List<CardSummary>): String {
        val oracle = card.oracleText?.lowercase() ?: ""
        val cardColors = extractColors(card.manaCost)
        val offColor = cardColors.isNotEmpty() && colorCommitment.isNotEmpty() && pickedSoFar.size >= 8 &&
            run {
                val topColors = colorCommitment.entries.sortedByDescending { it.value }.take(2).map { it.key }.toSet()
                !cardColors.all { it in topColors } && !(cardColors.size == 1 && parseCmc(card.manaCost ?: "") >= 4)
            }
        return when {
            offColor -> "Off-color for your current picks"
            card.rarity?.uppercase() == "MYTHIC" || card.rarity?.uppercase() == "RARE" -> "High-impact ${card.rarity.lowercase()}"
            oracle.contains("destroy") || oracle.contains("exile") || oracle.contains("fight") -> "Removal — premium in limited"
            oracle.contains("flying") || oracle.contains("menace") || oracle.contains("trample") -> "Evasive threat"
            oracle.contains("draw") -> "Card advantage"
            card.power != null && card.toughness != null -> "Efficient creature for the curve"
            else -> "Playable filler"
        }
    }

    private fun extractColors(manaCost: String?): Set<Char> {
        if (manaCost == null) return emptySet()
        return setOf('W', 'U', 'B', 'R', 'G').filter { it in manaCost }.toSet()
    }

    fun parseCmc(manaCost: String): Int {
        var cmc = 0
        val regex = Regex("\\{([^}]+)\\}")
        for (match in regex.findAll(manaCost)) {
            val symbol = match.groupValues[1]
            val numericValue = symbol.toIntOrNull()
            if (numericValue != null) cmc += numericValue
            else if (symbol != "X") cmc += 1
        }
        return cmc
    }
}
