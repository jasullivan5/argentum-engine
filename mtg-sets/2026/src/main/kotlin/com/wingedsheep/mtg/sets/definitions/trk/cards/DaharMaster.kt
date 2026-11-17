package com.wingedsheep.mtg.sets.definitions.trk.cards

import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.TargetObject

/**
 * Dahar Master
 * {3}{R}
 * Creature — Klingon Warrior
 * 4/3
 * When this creature enters, you may sacrifice another nonland permanent. When you do, this
 * creature deals 2 damage to any target.
 */
val DaharMaster = card("Dahar Master") {
    manaCost = "{3}{R}"
    colorIdentity = "R"
    typeLine = "Creature — Klingon Warrior"
    oracleText = "When this creature enters, you may sacrifice another nonland permanent. When you do, " +
        "this creature deals 2 damage to any target."
    power = 4
    toughness = 3

    triggeredAbility {
        trigger = Triggers.self.enters()
        effect = Effects.ReflexiveTrigger(
            action = Effects.Pipeline {
                val toSacrifice = selectTarget(
                    TargetObject(filter = TargetFilter(GameObjectFilter.NonlandPermanent.youControl()).other()),
                    nonTargeting = true,
                    prompt = "Choose another nonland permanent to sacrifice"
                )
                run(Effects.SacrificeTarget(toSacrifice.asTarget))
            },
            optional = true,
            descriptionOverride = "You may sacrifice another nonland permanent. When you do, this creature " +
                "deals 2 damage to any target."
        ) {
            val any = target(Targets.Any)
            effect = Effects.DealDamage(2, any)
        }
        description = "When this creature enters, you may sacrifice another nonland permanent. When you " +
            "do, this creature deals 2 damage to any target."
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "146"
        artist = "Grant Griffin"
        flavorText = "\"The *d'k tahg* is sharp, but it is your conviction that drives it into the heart of your enemy.\""
        imageUri = "https://cards.scryfall.io/normal/front/1/1/1122fe1c-97f7-409d-ad37-0fed46f9f963.jpg?1792590970"
    }
}
