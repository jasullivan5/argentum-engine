package com.wingedsheep.ai.engine

import com.wingedsheep.ai.llm.CardSummary

/**
 * Reads a seven-card opening hand for what it can actually cast in its first turns — the check
 * behind [AiProfile.curveAwareMulligan]. It only ever turns a keep into a mulligan, and only for the
 * two shapes the land count alone can't see:
 *
 *  - **Lands of the wrong colours for every spell.** No nonland card's coloured pips are covered
 *    by the colours the hand's lands make.
 *  - **A two-lander that does nothing on two lands.** On the play it needs two spells it can cast
 *    off exactly those two lands (mana value ≤ 2, pips matched land by land); on the draw — one
 *    more card to find the third land — one such spell plus a second spell of mana value ≤ 3 in
 *    its colours.
 *
 * Everything else the land-count rule kept is still kept. The thresholds are ratios of lands seen,
 * so they read the same for a 40-card limited deck (17 lands), a 60-card deck (24) and a 100-card
 * Commander deck (~37) — all roughly 40% land.
 *
 * Land colours are read off the type line's basic land types and the "Add …" clauses of the rules
 * text. A land whose colours can't be read that way (a fetch land, a land with an unusual mana
 * ability) is treated as making every colour, so an unreadable land never causes a mulligan.
 */
object OpeningHandReader {

    private val COLOURS = listOf('W', 'U', 'B', 'R', 'G')
    private val BASIC_TYPES = mapOf(
        "Plains" to 'W', "Island" to 'U', "Swamp" to 'B', "Mountain" to 'R', "Forest" to 'G',
    )
    private val SYMBOL = Regex("\\{([^}]+)\\}")
    private val ADD_CLAUSE = Regex("\\bAdd\\b([^.]*)", RegexOption.IGNORE_CASE)

    fun isLand(card: CardSummary?): Boolean = card?.typeLine?.contains("Land", ignoreCase = true) == true

    /**
     * Whether a hand the land-count rule would keep (2–5 lands) should be mulliganed anyway.
     * [hand] is the full seven-card hand.
     */
    fun shouldMulligan(hand: List<CardSummary>, onThePlay: Boolean): Boolean {
        val lands = hand.filter(::isLand)
        val spells = hand.filterNot(::isLand).map(::spell)
        if (spells.isEmpty() || lands.isEmpty()) return false
        val landColours = lands.map(::coloursOf)
        val available = landColours.flatMapTo(mutableSetOf()) { it }

        if (spells.none { it.colourable(available) }) return true

        if (lands.size == 2) {
            val onTwo = spells.count { it.manaValue <= 2 && it.payableBy(landColours) }
            val byThree = spells.count { it.manaValue <= 3 && it.colourable(available) }
            return if (onThePlay) onTwo < 2 else onTwo < 1 || byThree < 2
        }
        return false
    }

    /** The colours [land] can make; every colour when they can't be read. */
    internal fun coloursOf(land: CardSummary): Set<Char> {
        val subtypes = land.typeLine?.substringAfter('—', "")?.split(' ')?.map(String::trim).orEmpty()
        val fromTypes = subtypes.mapNotNullTo(mutableSetOf()) { BASIC_TYPES[it] }
        val text = land.oracleText.orEmpty()
        val clauses = ADD_CLAUSE.findAll(text).map { it.groupValues[1] }.toList()
        if (clauses.any { "any color" in it || "any one color" in it || "any type" in it || "any combination of colors" in it }) {
            return COLOURS.toSet()
        }
        val fromText = clauses.flatMap { clause -> SYMBOL.findAll(clause).map { it.groupValues[1] }.toList() }
            .filter { it.length == 1 && it[0] in COLOURS }
            .mapTo(mutableSetOf()) { it[0] }
        val colours = fromTypes + fromText
        return when {
            colours.isNotEmpty() -> colours
            clauses.isNotEmpty() -> emptySet() // reads as a colourless-only land ({C})
            else -> COLOURS.toSet()
        }
    }

    /** One coloured mana pip: the colours any one of which pays it. */
    private class Spell(val manaValue: Int, val pips: List<Set<Char>>) {
        fun colourable(available: Set<Char>): Boolean = pips.all { pip -> pip.any { it in available } }

        /** Every pip paid by a different land of [lands]. */
        fun payableBy(lands: List<Set<Char>>): Boolean = assign(0, lands.indices.toSet(), lands)

        private fun assign(pip: Int, free: Set<Int>, lands: List<Set<Char>>): Boolean {
            if (pip == pips.size) return true
            return free.any { land -> pips[pip].any { it in lands[land] } && assign(pip + 1, free - land, lands) }
        }
    }

    private fun spell(card: CardSummary): Spell {
        var manaValue = 0
        val pips = mutableListOf<Set<Char>>()
        for (match in SYMBOL.findAll(card.manaCost.orEmpty())) {
            val symbol = match.groupValues[1].uppercase()
            val parts = symbol.split('/')
            when {
                symbol.toIntOrNull() != null -> manaValue += symbol.toInt()
                symbol == "X" || symbol == "Y" || symbol == "Z" -> {}
                // Phyrexian ({W/P}) can be paid with life; two-brid ({2/W}) with generic mana.
                "P" in parts -> manaValue += 1
                parts.any { it.toIntOrNull() != null } -> manaValue += parts.first { it.toIntOrNull() != null }.toInt()
                else -> {
                    manaValue += 1
                    val colours = parts.mapNotNullTo(mutableSetOf()) { p -> p.singleOrNull()?.takeIf { it in COLOURS } }
                    if (colours.isNotEmpty()) pips += colours
                }
            }
        }
        return Spell(manaValue, pips)
    }
}
