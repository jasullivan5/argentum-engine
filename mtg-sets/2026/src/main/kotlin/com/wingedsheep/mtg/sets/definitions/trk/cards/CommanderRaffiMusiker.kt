package com.wingedsheep.mtg.sets.definitions.trk.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.dsl.mode
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.effects.ModalEffect
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Commander Raffi Musiker
 * {2}{B}
 * Legendary Creature — Human Officer
 * 3/2
 * When Commander Musiker enters, choose one —
 * • Exile up to one target card from a graveyard. You gain 2 life.
 * • Investigate. You lose 2 life.
 */
val CommanderRaffiMusiker = card("Commander Raffi Musiker") {
    manaCost = "{2}{B}"
    colorIdentity = "B"
    typeLine = "Legendary Creature — Human Officer"
    oracleText = "When Commander Musiker enters, choose one —\n" +
        "• Exile up to one target card from a graveyard. You gain 2 life.\n" +
        "• Investigate. You lose 2 life. (Create a Clue token. It's an artifact with \"{2}, Sacrifice this token: Draw a card.\")"
    power = 3
    toughness = 2

    triggeredAbility {
        trigger = Triggers.self.enters()
        effect = ModalEffect.chooseOne(
            mode("Exile up to one target card from a graveyard. You gain 2 life.") {
                val graveyardCard = target(TargetFilter.CardInGraveyard, optional = true)
                effect = Effects.Exile(graveyardCard) then Effects.GainLife(2)
            },
            mode("Investigate. You lose 2 life.") {
                effect = Effects.Investigate() then Effects.LoseLife(2, EffectTarget.Controller)
            }
        )
        description = "When Commander Musiker enters, choose one — Exile up to one target card from a " +
            "graveyard and you gain 2 life; or investigate and you lose 2 life."
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "100"
        artist = "Runa I. Rosenberger"
        imageUri = "https://cards.scryfall.io/normal/front/7/e/7e60bd56-3da1-4211-b790-320471eef242.jpg?1793013565"
    }
}
