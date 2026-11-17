package com.wingedsheep.mtg.sets.definitions.thb.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.ModifyStats
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.predicates.ControllerPredicate
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.targets.TargetObject

/**
 * Warbriar Blessing
 * {1}{G}
 * Enchantment — Aura
 * Enchant creature you control
 * When this Aura enters, enchanted creature fights up to one target creature you don't control.
 * Enchanted creature gets +0/+2.
 *
 * If the Aura has left the battlefield by the time its trigger resolves, the creature it last
 * enchanted still fights — [EffectTarget.EnchantedCreature] falls back to the Aura's last-known host.
 */
val WarbriarBlessing = card("Warbriar Blessing") {
    manaCost = "{1}{G}"
    colorIdentity = "G"
    typeLine = "Enchantment — Aura"
    oracleText = "Enchant creature you control\n" +
        "When this Aura enters, enchanted creature fights up to one target creature you don't control. " +
        "(Each deals damage equal to its power to the other.)\n" +
        "Enchanted creature gets +0/+2."

    auraTarget = TargetObject(filter = TargetFilter.CreatureYouControl)

    triggeredAbility {
        trigger = Triggers.self.enters()
        val theirs = target(
            TargetFilter(
                GameObjectFilter.Creature.copy(
                    controllerPredicate = ControllerPredicate.Not(ControllerPredicate.ControlledByYou)
                )
            ),
            optional = true
        )
        effect = Effects.Fight(EffectTarget.EnchantedCreature, theirs)
    }

    staticAbility {
        ability = ModifyStats(0, 2)
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "204"
        artist = "Michele Parisi"
        imageUri = "https://cards.scryfall.io/normal/front/1/1/11d64cab-2ed1-46d9-8034-0c2ba3f2e1ed.jpg?1783931526"
        ruling("2020-01-24", "If the target creature is an illegal target when Warbriar Blessing's triggered ability tries to resolve, the ability doesn't resolve. If the enchanted creature is no longer on the battlefield, the target creature won't deal or be dealt damage.")
        ruling("2020-01-24", "If Warbriar Blessing leaves the battlefield before its enters-the-battlefield triggered ability resolves, the creature it last enchanted before it left will be the one to fight.")
    }
}
