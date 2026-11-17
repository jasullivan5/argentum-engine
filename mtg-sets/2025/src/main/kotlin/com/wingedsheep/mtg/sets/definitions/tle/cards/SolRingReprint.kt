package com.wingedsheep.mtg.sets.definitions.tle.cards

import com.wingedsheep.sdk.model.Printing
import com.wingedsheep.sdk.model.Rarity

/**
 * Sol Ring reprint in TLE. Canonical CardDefinition lives in Limited Edition Alpha.
 */
val SolRingReprint = Printing(
    oracleId = "6ad8011d-3471-4369-9d68-b264cc027487",
    name = "Sol Ring",
    setCode = "TLE",
    collectorNumber = "316",
    scryfallId = "c1f93f78-645b-4f21-a80b-5542f3328111",
    artist = "Barbara Rosiak",
    imageUri = "https://cards.scryfall.io/normal/front/c/1/c1f93f78-645b-4f21-a80b-5542f3328111.jpg?1783904751",
    releaseDate = "2025-11-21",
    rarity = Rarity.RARE,
    frameEffects = listOf("showcase"),
    borderColor = "borderless",
)
