package com.wingedsheep.mtg.sets.definitions.trc.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity

/**
 * Worf, Chief Tactical Officer
 * {2}{W}
 * Legendary Creature — Klingon Warrior Officer
 * 2/3
 * Vigilance
 */
val WorfChiefTacticalOfficer = card("Worf, Chief Tactical Officer") {
    manaCost = "{2}{W}"
    colorIdentity = "W"
    typeLine = "Legendary Creature — Klingon Warrior Officer"
    oracleText = "Vigilance (Attacking doesn't cause this creature to tap.)"
    power = 2
    toughness = 3
    keywords(Keyword.VIGILANCE)

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "141"
        artist = "Ariel Orea"
        flavorText = "\"The true test of a warrior is not without. It is within.\""
        imageUri = "https://cards.scryfall.io/normal/front/c/e/ced54aa6-c8ee-451f-91cd-967fe766ec13.jpg?1786005790"
    }
}
