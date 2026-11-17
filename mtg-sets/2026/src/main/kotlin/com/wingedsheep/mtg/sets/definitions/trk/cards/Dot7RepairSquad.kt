package com.wingedsheep.mtg.sets.definitions.trk.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.ActivationRestriction
import com.wingedsheep.sdk.scripting.TimingRule
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter

/**
 * DOT-7 Repair Squad
 * {3}{W}
 * Artifact Creature — Robot
 * 3/3
 * Flying
 * {1}{W}: Untap target creature. Activate only as a sorcery and only once each turn.
 */
val Dot7RepairSquad = card("DOT-7 Repair Squad") {
    manaCost = "{3}{W}"
    colorIdentity = "W"
    typeLine = "Artifact Creature — Robot"
    oracleText = "Flying\n{1}{W}: Untap target creature. Activate only as a sorcery and only once each turn."
    power = 3
    toughness = 3
    keywords(Keyword.FLYING)

    activatedAbility {
        cost = Costs.Mana("{1}{W}")
        val t = target(TargetFilter.Creature)
        effect = Effects.Untap(t)
        timing = TimingRule.SorcerySpeed
        restrictions = listOf(ActivationRestriction.OncePerTurn)
        description = "{1}{W}: Untap target creature. Activate only as a sorcery and only once each turn."
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "12"
        artist = "Colin Searle"
        flavorText = "The DOT-7s are designed to make repairs in any conditions too dangerous for humanoids."
        imageUri = "https://cards.scryfall.io/normal/front/f/c/fca1528c-84cc-42d2-99ae-9f10f0e28743.jpg?1786005749"
    }
}
