package com.wingedsheep.mtg.sets.definitions.m21.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Keral Keep Disciples
 * {2}{R}{R}
 * Creature — Human Monk
 * 4/3
 * Whenever you activate a loyalty ability of a Chandra planeswalker, this creature deals 1 damage
 * to each opponent.
 *
 * The Sarkhan's Whelp trigger narrowed to loyalty abilities: the activated ability's source must be
 * a Chandra planeswalker on the battlefield, and the activation must be a loyalty ability (CR 606).
 * The trigger goes on the stack above the loyalty ability, so it resolves first.
 */
val KeralKeepDisciples = card("Keral Keep Disciples") {
    manaCost = "{2}{R}{R}"
    colorIdentity = "R"
    typeLine = "Creature — Human Monk"
    power = 4
    toughness = 3
    oracleText = "Whenever you activate a loyalty ability of a Chandra planeswalker, this creature deals 1 damage to each opponent."

    triggeredAbility {
        trigger = Triggers.you.activatesAbility(
            of = GameObjectFilter.Planeswalker.withSubtype("Chandra").onBattlefield(),
            loyalty = true,
        )
        effect = Effects.DealDamage(1, EffectTarget.PlayerRef(Player.EachOpponent))
        description = "Whenever you activate a loyalty ability of a Chandra planeswalker, this creature deals 1 damage to each opponent."
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "334"
        artist = "Leesha Hannigan"
        flavorText = "\"You're not setting foot in here. Not on our watch!\""
        imageUri = "https://cards.scryfall.io/normal/front/7/b/7b862e46-f5a2-4fce-98b5-2c5aa49ec648.jpg?1783930617"

        ruling("2020-06-23", "The ability of Keral Keep Disciples resolves before the ability that caused it to trigger.")
    }
}
