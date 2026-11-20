package com.wingedsheep.mtg.sets.definitions.lrw.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter

/**
 * Oblivion Ring
 * {2}{W}
 * Enchantment
 * When this enchantment enters, exile another target nonland permanent.
 * When this enchantment leaves the battlefield, return the exiled card to the battlefield under its
 * owner's control.
 *
 * Two *separate* triggers, not one "exile until ~ leaves" — that's the whole point of the 2007-10-01
 * ruling: if the Ring leaves before its ETB trigger resolves, the leaves trigger fires first and
 * finds nothing, and the ETB then exiles its target permanently. Modelling this as ordinary linked exile
 * plus an independent linked return keeps that ordering honest even after this source visit has ended.
 *
 * "another target nonland permanent" is [TargetFilter.OtherNonlandPermanent] — any controller's, but
 * never the Ring itself.
 */
val OblivionRing = card("Oblivion Ring") {
    manaCost = "{2}{W}"
    colorIdentity = "W"
    typeLine = "Enchantment"
    oracleText = "When this enchantment enters, exile another target nonland permanent.\n" +
        "When this enchantment leaves the battlefield, return the exiled card to the battlefield under its owner's control."

    triggeredAbility {
        trigger = Triggers.self.enters()
        val exiled = target(TargetFilter.OtherNonlandPermanent)
        effect = Effects.ExileLinkedToSource(exiled)
    }

    triggeredAbility {
        trigger = Triggers.self.leaves()
        effect = Effects.ReturnLinkedExileUnderOwnersControl()
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "34"
        artist = "Wayne England"
        flavorText = "A circle of sugar and a word of forbiddance."
        imageUri = "https://cards.scryfall.io/normal/front/1/c/1c7fffe8-709c-4cb4-bbad-e4a0c35b616a.jpg?1783942910"
        ruling("2007-10-01", "If Oblivion Ring leaves the battlefield before its first ability has resolved, its second ability will trigger and do nothing. Then its first ability will resolve and exile the targeted nonland permanent forever.")
        ruling("2012-07-01", "Auras attached to the exiled permanent will be put into their owners' graveyards. Equipment attached to the exiled permanent will become unattached and remain on the battlefield. Any counters on the exiled permanent will cease to exist.")
    }
}
