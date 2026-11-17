package com.wingedsheep.mtg.sets.definitions.ncc.cards

import com.wingedsheep.sdk.model.Printing
import com.wingedsheep.sdk.model.Rarity

/**
 * Nadir Kraken reprint in New Capenna Commander. The canonical
 * [com.wingedsheep.sdk.model.CardDefinition] lives in Theros Beyond Death's `cards/` package; this
 * file contributes only the New Capenna Commander presentation row.
 */
val NadirKrakenReprint = Printing(
    oracleId = "5a73d439-bd78-42e1-90ae-eedd30536881",
    name = "Nadir Kraken",
    setCode = "NCC",
    collectorNumber = "228",
    scryfallId = "793f0ac2-39b8-4cb2-aeac-f901bf5bd7ea",
    artist = "Dan Murayama Scott",
    imageUri = "https://cards.scryfall.io/normal/front/7/9/793f0ac2-39b8-4cb2-aeac-f901bf5bd7ea.jpg?1783923278",
    releaseDate = "2022-04-29",
    rarity = Rarity.RARE,
)
