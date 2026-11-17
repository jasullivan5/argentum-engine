package com.wingedsheep.mtg.sets.definitions.thb.cards

import com.wingedsheep.sdk.core.Subtype
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter

/**
 * Rise to Glory
 * {3}{W}{B}
 * Sorcery
 * Choose one or both —
 * • Return target creature card from your graveyard to the battlefield.
 * • Return target Aura card from your graveyard to the battlefield.
 *
 * The returned Aura isn't cast, so its controller chooses what it enchants as it enters; the
 * modes resolve in order, so it may enchant the creature the first mode just returned.
 */
val RiseToGlory = card("Rise to Glory") {
    manaCost = "{3}{W}{B}"
    colorIdentity = "WB"
    typeLine = "Sorcery"
    oracleText = "Choose one or both —\n" +
        "• Return target creature card from your graveyard to the battlefield.\n" +
        "• Return target Aura card from your graveyard to the battlefield."

    spell {
        modal(chooseCount = 2, minChooseCount = 1) {
            mode("Return target creature card from your graveyard to the battlefield") {
                val creature = target(TargetFilter(GameObjectFilter.Creature.ownedByYou(), zone = Zone.GRAVEYARD))
                effect = Effects.PutOntoBattlefieldFromGraveyard(creature)
            }
            mode("Return target Aura card from your graveyard to the battlefield") {
                val aura = target(TargetFilter(GameObjectFilter.Any.withSubtype(Subtype.AURA).ownedByYou(), zone = Zone.GRAVEYARD))
                effect = Effects.PutOntoBattlefieldFromGraveyard(aura)
            }
        }
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "225"
        artist = "Howard Lyon"
        flavorText = "A hero dies, and a legend arises."
        imageUri = "https://cards.scryfall.io/normal/front/c/f/cf6c11c8-64a1-46a4-979c-56a6d5b873d3.jpg?1783931519"
        ruling("2020-01-24", "If an Aura is put onto the battlefield without being cast, the Aura's controller-to-be chooses what it will enchant as it comes back onto the battlefield. An Aura put onto the battlefield this way doesn't target anything (so it could be attached to an opponent's permanent with hexproof, for example), but the Aura's enchant ability restricts what it can be attached to. If the Aura can't legally be attached to anything, it remains in its current zone.")
        ruling("2020-01-24", "Because modes are performed sequentially, an Aura returned to the battlefield with Rise to Glory can enchant a creature returned to the battlefield with Rise to Glory.")
    }
}
