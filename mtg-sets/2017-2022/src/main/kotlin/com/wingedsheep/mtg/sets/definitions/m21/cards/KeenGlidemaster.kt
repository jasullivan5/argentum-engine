package com.wingedsheep.mtg.sets.definitions.m21.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter

/**
 * Keen Glidemaster
 * {1}{U}
 * Creature — Human Soldier
 * 2/1
 * {2}{U}: Target creature gains flying until end of turn.
 */
val KeenGlidemaster = card("Keen Glidemaster") {
    manaCost = "{1}{U}"
    colorIdentity = "U"
    typeLine = "Creature — Human Soldier"
    power = 2
    toughness = 1
    oracleText = "{2}{U}: Target creature gains flying until end of turn."

    activatedAbility {
        cost = Costs.Mana("{2}{U}")
        val creature = target(TargetFilter.Creature)
        effect = Effects.GrantKeyword(Keyword.FLYING, creature)
        description = "{2}{U}: Target creature gains flying until end of turn."
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "54"
        artist = "Caroline Gariba"
        flavorText = "\"Fear is a rational response, but waiting won't make it any easier. Just jump!\""
        imageUri = "https://cards.scryfall.io/normal/front/c/c/cce6289e-f665-4faa-8285-c843447f3e52.jpg?1783930726"
    }
}
