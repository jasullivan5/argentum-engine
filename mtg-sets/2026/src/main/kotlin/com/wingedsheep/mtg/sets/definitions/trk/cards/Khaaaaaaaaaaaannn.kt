package com.wingedsheep.mtg.sets.definitions.trk.cards

import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.dsl.times
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter

/**
 * Khaaaaaaaaaaaannn!
 * {X}{R}
 * Instant
 * Khaaaaaaaaaaaannn! deals twice X damage to target creature. If that creature would die this turn,
 * exile it instead.
 */
val Khaaaaaaaaaaaannn = card("Khaaaaaaaaaaaannn!") {
    manaCost = "{X}{R}"
    colorIdentity = "R"
    typeLine = "Instant"
    oracleText = "Khaaaaaaaaaaaannn! deals twice X damage to target creature. If that creature would die this turn, exile it instead."

    spell {
        val t = target(TargetFilter.Creature)
        effect = Effects.DealDamage(DynamicAmounts.xValue() * 2, t) then Effects.MarkExileOnDeath(t)
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "154"
        artist = "Evan Shipard"
        flavorText = "\"I've done far worse than kill you. I've hurt you, and I wish to go on hurting you.\"\n—Khan Noonien Singh"
        imageUri = "https://cards.scryfall.io/normal/front/2/2/22c60c17-74ec-460b-bf7a-e56e42239826.jpg?1785981536"
    }
}
