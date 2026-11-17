package com.wingedsheep.mtg.sets.definitions.trk.cards

import com.wingedsheep.sdk.core.AbilityFlag
import com.wingedsheep.sdk.core.ManaCost
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GrantKeyword
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.targets.TargetObject

/**
 * Marooned
 * {U}
 * Enchantment — Aura
 * Enchant artifact or creature
 * When this Aura enters, you may pay {3}. If you do, tap enchanted permanent.
 * Enchanted permanent doesn't untap during its controller's untap step and can't be beamed up.
 *
 * Stuck in Summoner's Sanctum's lock with an optional tap. Both host restrictions are projected
 * flags on the attached permanent (the default `attachedCreature()` scope covers any attached
 * permanent): [AbilityFlag.DOESNT_UNTAP], and [AbilityFlag.CANT_BE_BEAMED_UP], which keeps the
 * host out of every beam me up offer's return pool.
 */
val Marooned = card("Marooned") {
    manaCost = "{U}"
    colorIdentity = "U"
    typeLine = "Enchantment — Aura"
    oracleText = "Enchant artifact or creature\n" +
        "When this Aura enters, you may pay {3}. If you do, tap enchanted permanent.\n" +
        "Enchanted permanent doesn't untap during its controller's untap step and can't be beamed up."

    auraTarget = TargetObject(filter = TargetFilter.CreatureOrArtifact)

    triggeredAbility {
        trigger = Triggers.self.enters()
        effect = Effects.MayPay(ManaCost.parse("{3}"), then = Effects.Tap(EffectTarget.EnchantedPermanent))
    }

    staticAbility {
        ability = GrantKeyword(AbilityFlag.DOESNT_UNTAP.name)
    }

    staticAbility {
        ability = GrantKeyword(AbilityFlag.CANT_BE_BEAMED_UP.name)
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "72"
        artist = "Joel Chaim Holtzman"
        flavorText = "\"Okay, new plan!\""
        imageUri = "https://cards.scryfall.io/normal/front/0/8/0820a78e-e392-423c-9d62-ddfda5cb3f03.jpg?1785981422"
    }
}
