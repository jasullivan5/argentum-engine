package com.wingedsheep.mtg.sets.definitions.trk.cards

import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.TimingRule
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter

/**
 * Picard, Leading by Example
 * {1}{W}
 * Legendary Creature — Human Officer
 * 2/2
 * {1}, {T}: Another target creature gets +X/+X until end of turn, where X is Picard's power.
 * Activate only as a sorcery.
 */
val PicardLeadingByExample = card("Picard, Leading by Example") {
    manaCost = "{1}{W}"
    colorIdentity = "W"
    typeLine = "Legendary Creature — Human Officer"
    oracleText = "{1}, {T}: Another target creature gets +X/+X until end of turn, where X is Picard's power. " +
        "Activate only as a sorcery."
    power = 2
    toughness = 2

    activatedAbility {
        cost = Costs.Composite(Costs.Mana("{1}"), Costs.Tap)
        val t = target(TargetFilter.Creature.other())
        effect = Effects.ModifyStats(DynamicAmounts.sourcePower(), DynamicAmounts.sourcePower(), t)
        timing = TimingRule.SorcerySpeed
        description = "{1}, {T}: Another target creature gets +X/+X until end of turn, where X is Picard's power. " +
            "Activate only as a sorcery."
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "29"
        artist = "Julie Dillon"
        flavorText = "\"I have the utmost confidence in your ability to perform the impossible.\""
        imageUri = "https://cards.scryfall.io/normal/front/f/9/f9fa20c5-f241-4da2-9c99-c9b650095d74.jpg?1785981358"
    }
}
