package com.wingedsheep.mtg.sets.definitions.trk.cards

import com.wingedsheep.sdk.core.CounterType
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Patterns
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.TimingRule
import com.wingedsheep.sdk.scripting.effects.SearchDestination
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter

/**
 * Warp Core
 * {2}
 * Artifact
 * When this artifact enters, you may search your library for a basic land card, reveal it, put it
 * into your hand, then shuffle.
 * {1}, {T}: Put two charge counters on target Spacecraft. Activate only as a sorcery.
 */
val WarpCore = card("Warp Core") {
    manaCost = "{2}"
    colorIdentity = ""
    typeLine = "Artifact"
    oracleText = "When this artifact enters, you may search your library for a basic land card, reveal it, " +
        "put it into your hand, then shuffle.\n" +
        "{1}, {T}: Put two charge counters on target Spacecraft. Activate only as a sorcery."

    triggeredAbility {
        trigger = Triggers.self.enters()
        optional = true
        effect = Patterns.Library.searchLibrary(
            filter = GameObjectFilter.BasicLand,
            destination = SearchDestination.HAND,
            reveal = true
        )
    }

    activatedAbility {
        cost = Costs.Composite(Costs.Mana("{1}"), Costs.Tap)
        val spacecraft = target(TargetFilter(GameObjectFilter.Permanent.withSubtype("Spacecraft")))
        effect = Effects.AddCounters(CounterType.CHARGE, 2, spacecraft)
        timing = TimingRule.SorcerySpeed
        description = "{1}, {T}: Put two charge counters on target Spacecraft. Activate only as a sorcery."
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "274"
        artist = "Leon Tukker"
        flavorText = "Every warp core has its own glow and thrum, each as unique as a human heartbeat."
        imageUri = "https://cards.scryfall.io/normal/front/d/e/de8e2bde-b368-4d10-8409-86f5da6997f6.jpg?1792590999"
    }
}
