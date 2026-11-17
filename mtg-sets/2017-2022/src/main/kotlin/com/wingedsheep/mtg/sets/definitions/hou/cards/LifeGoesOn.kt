package com.wingedsheep.mtg.sets.definitions.hou.cards

import com.wingedsheep.sdk.dsl.Conditions
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity

/**
 * Life Goes On
 * {G}
 * Instant
 * You gain 4 life. If a creature died this turn, you gain 8 life instead.
 *
 * "Instead" is one gain or the other, checked on resolution — never 4 + 8, and never more than 8
 * however many creatures died (2020-06-23 ruling).
 */
val LifeGoesOn = card("Life Goes On") {
    manaCost = "{G}"
    colorIdentity = "G"
    typeLine = "Instant"
    oracleText = "You gain 4 life. If a creature died this turn, you gain 8 life instead."

    spell {
        effect = Effects.If(
            condition = Conditions.CreatureDiedThisTurn,
            then = Effects.GainLife(8),
            otherwise = Effects.GainLife(4),
        )
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "121"
        artist = "Daarken"
        flavorText = "\"If you believe the God-Pharaoh tests you, charge with me to prove yourself. If you believe " +
            "he betrayed us all, join with me to fight for tomorrow!\"\n—Samut, former Tah-crop initiate"
        imageUri = "https://cards.scryfall.io/normal/front/5/f/5f39c264-a9a5-46f3-bd67-01d93d69e457.jpg?1783936018"

        ruling("2020-06-23", "You won't gain more than 8 life if more than one creature died this turn.")
    }
}
