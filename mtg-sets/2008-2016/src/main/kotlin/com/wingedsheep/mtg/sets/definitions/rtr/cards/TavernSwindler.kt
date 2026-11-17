package com.wingedsheep.mtg.sets.definitions.rtr.cards

import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity

/**
 * Tavern Swindler
 * {1}{B}
 * Creature — Human Rogue
 * 2/2
 * {T}, Pay 3 life: Flip a coin. If you win the flip, you gain 6 life.
 */
val TavernSwindler = card("Tavern Swindler") {
    manaCost = "{1}{B}"
    colorIdentity = "B"
    typeLine = "Creature — Human Rogue"
    power = 2
    toughness = 2
    oracleText = "{T}, Pay 3 life: Flip a coin. If you win the flip, you gain 6 life."

    activatedAbility {
        cost = Costs.Composite(Costs.Tap, Costs.PayLife(3))
        effect = Effects.FlipCoin(wonEffect = Effects.GainLife(6))
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "79"
        artist = "Cynthia Sheppard"
        flavorText = "Rakdos cultists are her best customers. They never flinch at pain and are seldom good at math."
        imageUri = "https://cards.scryfall.io/normal/front/4/7/47240ed3-f256-45fb-ab38-7b07e672d2ed.jpg"
    }
}
