package com.wingedsheep.mtg.sets.definitions.trc.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity

/**
 * Mek'leth Berserker
 * {3}{R}
 * Creature — Klingon Berserker
 * 4/3
 * Menace
 */
val MeklethBerserker = card("Mek'leth Berserker") {
    manaCost = "{3}{R}"
    colorIdentity = "R"
    typeLine = "Creature — Klingon Berserker"
    oracleText = "Menace (This creature can't be blocked except by two or more creatures.)"
    power = 4
    toughness = 3
    keywords(Keyword.MENACE)

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "166"
        artist = "Johann Bodin"
        flavorText = "\"We Klingons have a reputation for ruthlessness. You will find that it is deserved.\"\n" +
            "—Commander Kor, military governor of Organia"
        imageUri = "https://cards.scryfall.io/normal/front/4/f/4f503eb1-d95d-4c72-bc58-a7b9e9f92eb6.jpg"
    }
}
