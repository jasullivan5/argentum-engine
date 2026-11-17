package com.wingedsheep.mtg.sets.definitions.m21.cards

import com.wingedsheep.sdk.core.CounterType
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.dsl.grantedTriggeredAbility
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GrantTriggeredAbility
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.targets.TargetObject

/**
 * Light of Promise
 * {2}{W}
 * Enchantment — Aura
 * Enchant creature
 * Enchanted creature has "Whenever you gain life, put that many +1/+1 counters on this creature."
 *
 * The aura grants the Custody Battle way: the quoted ability belongs to the enchanted creature, so
 * "you" is that creature's controller and "this creature" is the creature itself. "That many" is
 * the amount of the triggering life-gain event ([DynamicAmounts.triggerLifeGained]); each
 * life-gain event triggers it separately (2020-06-23 rulings).
 */
val LightOfPromise = card("Light of Promise") {
    manaCost = "{2}{W}"
    colorIdentity = "W"
    typeLine = "Enchantment — Aura"
    oracleText = "Enchant creature\n" +
        "Enchanted creature has \"Whenever you gain life, put that many +1/+1 counters on this creature.\""

    auraTarget = TargetObject(filter = TargetFilter.Creature)

    staticAbility {
        ability = GrantTriggeredAbility(
            grantedTriggeredAbility {
                trigger = Triggers.you.gainsLife()
                effect = Effects.AddDynamicCounters(
                    CounterType.PLUS_ONE_PLUS_ONE,
                    DynamicAmounts.triggerLifeGained(),
                    EffectTarget.Self,
                )
                description = "Whenever you gain life, put that many +1/+1 counters on this creature."
            }
        )
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "25"
        artist = "Cristi Balanescu"
        flavorText = "The light washed over him, and in the wind he heard the roars of all the pridemates who had come before."
        imageUri = "https://cards.scryfall.io/normal/front/d/5/d5f80411-0a95-4e0a-b7a8-af23ddf385cc.jpg?1783930738"

        ruling(
            "2020-06-23",
            "If enchanted creature is dealt lethal damage at the same time that you gain life, it won't receive a " +
                "counter from its ability in time to save it."
        )
        ruling(
            "2020-06-23",
            "Each creature with lifelink dealing combat damage causes a separate life-gaining event. For example, if " +
                "two creatures you control with lifelink deal combat damage at the same time, the ability will trigger " +
                "twice. However, if a single creature you control with lifelink deals combat damage to multiple " +
                "creatures, players, and/or planeswalkers at the same time (perhaps because it has trample or was " +
                "blocked by more than one creature), the ability will trigger only once."
        )
        ruling(
            "2020-06-23",
            "If you gain an amount of life \"for each\" of something, that life is gained as one event and the " +
                "ability triggers only once."
        )
    }
}
