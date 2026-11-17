package com.wingedsheep.mtg.sets.definitions.trk.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.beamMeUp
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity

/**
 * Open Communications
 * {U}
 * Sorcery
 * Draw a card.
 * Beam me up {2}{U} (You may cast this card from your graveyard for {2}{U} if you also return a
 * creature you control to its owner's hand. Then exile this spell.)
 */
val OpenCommunications = card("Open Communications") {
    manaCost = "{U}"
    colorIdentity = "U"
    typeLine = "Sorcery"
    oracleText = "Draw a card.\nBeam me up {2}{U} (You may cast this card from your graveyard for {2}{U} " +
        "if you also return a creature you control to its owner's hand. Then exile this spell.)"
    beamMeUp("{2}{U}")
    spell { effect = Effects.DrawCards(1) }
    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "76"
        artist = "David Robert Hovey"
        flavorText = "Some gestures are universal."
        imageUri = "https://cards.scryfall.io/normal/front/8/e/8eeec269-13f0-4923-9f13-e995acd73e00.jpg?1786396013"
    }
}
