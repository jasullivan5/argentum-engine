package com.wingedsheep.mtg.sets.definitions.m21.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Conditions
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter

/**
 * Lofty Denial
 * {1}{U}
 * Instant
 * Counter target spell unless its controller pays {1}. If you control a creature with flying,
 * counter that spell unless its controller pays {4} instead.
 *
 * Whether you control a flier is checked as the spell resolves; one flier or several, the tax is
 * {4} (ruling).
 */
val LoftyDenial = card("Lofty Denial") {
    manaCost = "{1}{U}"
    colorIdentity = "U"
    typeLine = "Instant"
    oracleText = "Counter target spell unless its controller pays {1}. If you control a creature with " +
        "flying, counter that spell unless its controller pays {4} instead."

    spell {
        target(TargetFilter.SpellOnStack)
        effect = Effects.If(
            condition = Conditions.YouControl(GameObjectFilter.Creature.withKeyword(Keyword.FLYING)),
            then = Effects.CounterUnlessPays("{4}"),
            otherwise = Effects.CounterUnlessPays("{1}")
        )
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "56"
        artist = "Manuel Castañón"
        flavorText = "The mage scowled as yet another bolt splashed harmlessly against seemingly empty air."
        imageUri = "https://cards.scryfall.io/normal/front/6/4/64832674-beb1-446e-b2f7-8a5e271139a5.jpg?1783930725"
        ruling("2020-06-23", "If you control more than one creature with flying, Lofty Denial still requires only {4}.")
    }
}
