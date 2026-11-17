package com.wingedsheep.mtg.sets.definitions.trc.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity

/**
 * Spock, Logical Choice
 * {3}{U}{U}
 * Legendary Creature — Vulcan Human Scientist
 * 3/5
 * Vigilance
 * {T}: Draw a card.
 */
val SpockLogicalChoice = card("Spock, Logical Choice") {
    manaCost = "{3}{U}{U}"
    colorIdentity = "U"
    typeLine = "Legendary Creature — Vulcan Human Scientist"
    oracleText = "Vigilance (Attacking doesn't cause this creature to tap.)\n{T}: Draw a card."
    power = 3
    toughness = 5
    keywords(Keyword.VIGILANCE)

    activatedAbility {
        cost = Costs.Tap
        effect = Effects.DrawCards(1)
        description = "{T}: Draw a card."
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "194"
        artist = "Bastien Jez"
        flavorText = "\"In critical moments, men sometimes see exactly what they wish to see.\""
        imageUri = "https://cards.scryfall.io/normal/front/5/a/5a741052-e92d-4e9c-8cfa-3b5ed1f432f5.jpg?1784065332"
    }
}
