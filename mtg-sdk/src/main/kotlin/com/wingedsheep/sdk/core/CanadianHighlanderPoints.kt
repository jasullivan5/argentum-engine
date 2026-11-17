package com.wingedsheep.sdk.core

/**
 * The Canadian Highlander points list (<https://www.canadianhighlander.ca/points-list/>).
 *
 * Each listed card costs points; a [DeckFormat.CANADIAN_HIGHLANDER] deck may hold at most
 * [POINT_CAP] points in total. Unlisted cards cost nothing. The list is maintained by the format's
 * council and changes a few times a year — update [POINTS] and [AS_OF] together when it does.
 */
object CanadianHighlanderPoints {

    /** The most points one deck may spend. */
    const val POINT_CAP: Int = 10

    /** The list update [POINTS] reflects (the council's Fall 2026 update). */
    const val AS_OF: String = "2026-09-01"

    /** Card name (Scryfall's spelling, ASCII apostrophes) → points. */
    val POINTS: Map<String, Int> = mapOf(
        "Ancestral Recall" to 8,
        "Ancient Tomb" to 1,
        "Balance" to 1,
        "Black Lotus" to 7,
        "Demonic Tutor" to 3,
        "Dig Through Time" to 1,
        "Enlightened Tutor" to 1,
        "Flash" to 7,
        "Forth Eorlingas!" to 1,
        "Gifts Ungiven" to 1,
        "Imperial Seal" to 1,
        "Intuition" to 1,
        "Mana Crypt" to 5,
        "Mana Drain" to 1,
        "Mana Vault" to 1,
        "Merchant Scroll" to 1,
        "Mox Emerald" to 3,
        "Mox Jet" to 3,
        "Mox Pearl" to 3,
        "Mox Ruby" to 3,
        "Mox Sapphire" to 3,
        "Mystical Tutor" to 2,
        "Nadu, Winged Wisdom" to 1,
        "Natural Order" to 1,
        "Psychic Frog" to 1,
        "Reanimate" to 1,
        "Sol Ring" to 3,
        "Spellseeker" to 1,
        "Strip Mine" to 1,
        "Tainted Pact" to 1,
        "Tamiyo, Inquisitive Student" to 1,
        "Thassa's Oracle" to 5,
        "The Fantasticar" to 1,
        "The One Ring" to 1,
        "Time Vault" to 7,
        "Time Walk" to 5,
        "Tinker" to 3,
        "Tolarian Academy" to 2,
        "Treasure Cruise" to 1,
        "True-Name Nemesis" to 1,
        "Underworld Breach" to 3,
        "Urza's Saga" to 1,
        "Vampiric Tutor" to 2,
        "White Plume Adventurer" to 1,
        "Wishclaw Talisman" to 1,
        "Wrenn and Six" to 1,
    )

    /** Points [cardName] costs; 0 when it isn't on the list. */
    fun pointsFor(cardName: String): Int = POINTS[cardName] ?: 0
}
