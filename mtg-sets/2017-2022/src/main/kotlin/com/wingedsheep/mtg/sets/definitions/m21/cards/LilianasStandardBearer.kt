package com.wingedsheep.mtg.sets.definitions.m21.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.references.Player

/**
 * Liliana's Standard Bearer
 * {2}{B}
 * Creature — Zombie Knight
 * 3/1
 * Flash
 * When this creature enters, draw X cards, where X is the number of creatures that died under
 * your control this turn.
 *
 * X is read as the trigger resolves, so a Standard Bearer that dies in response counts itself
 * (ruling).
 */
val LilianasStandardBearer = card("Liliana's Standard Bearer") {
    manaCost = "{2}{B}"
    colorIdentity = "B"
    typeLine = "Creature — Zombie Knight"
    power = 3
    toughness = 1
    oracleText = "Flash\nWhen this creature enters, draw X cards, where X is the number of creatures " +
        "that died under your control this turn."

    keywords(Keyword.FLASH)

    triggeredAbility {
        trigger = Triggers.self.enters()
        effect = Effects.DrawCards(DynamicAmounts.creaturesDiedThisTurn(Player.You))
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "110"
        artist = "Josh Hass"
        flavorText = "The purple banner of the House of Vess is a symbol of death wherever it's flown."
        imageUri = "https://cards.scryfall.io/normal/front/3/d/3dc48b87-62cb-48f6-8979-e6fb98717b52.jpg?1783930705"
        ruling(
            "2020-06-23",
            "The value of X is determined only as the triggered ability resolves. If Liliana's Standard " +
                "Bearer dies while that ability's on the stack, it will count itself."
        )
    }
}
