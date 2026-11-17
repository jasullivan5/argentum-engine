package com.wingedsheep.mtg.sets.definitions.trc.cards

import com.wingedsheep.sdk.model.Printing
import com.wingedsheep.sdk.model.Rarity

/**
 * Tempered Steel reprint in TRC. Canonical CardDefinition lives in its earliest set.
 */
val TemperedSteelReprint = Printing(
    oracleId = "e220138c-5fc6-487d-9fa9-f21b2fb1f12c",
    name = "Tempered Steel",
    setCode = "TRC",
    collectorNumber = "430",
    scryfallId = "81d14558-71ff-4945-84e5-e13e04e365ac",
    artist = "Camille Alquier",
    imageUri = "https://cards.scryfall.io/normal/front/8/1/81d14558-71ff-4945-84e5-e13e04e365ac.jpg?1793021074",
    releaseDate = "2026-11-13",
    rarity = Rarity.RARE,
    frameEffects = listOf("enchantment"),
)
