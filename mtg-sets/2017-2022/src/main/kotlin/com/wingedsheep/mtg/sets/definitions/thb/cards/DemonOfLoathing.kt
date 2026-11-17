package com.wingedsheep.mtg.sets.definitions.thb.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.events.Recipient
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Demon of Loathing
 * {5}{B}{B}
 * Creature — Demon
 * 7/7
 * Flying, trample
 * Whenever this creature deals combat damage to a player, that player sacrifices a creature of their choice.
 */
val DemonOfLoathing = card("Demon of Loathing") {
    manaCost = "{5}{B}{B}"
    colorIdentity = "B"
    typeLine = "Creature — Demon"
    oracleText = "Flying, trample\nWhenever this creature deals combat damage to a player, that player sacrifices a creature of their choice."
    power = 7
    toughness = 7
    keywords(Keyword.FLYING, Keyword.TRAMPLE)

    triggeredAbility {
        trigger = Triggers.self.dealsCombatDamage(Recipient.AnyPlayer)
        effect = Effects.Sacrifice(GameObjectFilter.Creature, 1, EffectTarget.PlayerRef(Player.TriggeringPlayer))
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "292"
        artist = "Tomasz Jedruszek"
        flavorText = "Hatred is a potent ally, but an insatiable master."
        imageUri = "https://cards.scryfall.io/normal/front/5/7/57564f6f-7c99-4325-8a0e-22342e73767b.jpg?1783931489"
        ruling("2020-01-24", "If a creature is dealt lethal damage at the same time that Demon of Loathing deals combat damage to its controller, that player can't sacrifice that creature.")
    }
}
