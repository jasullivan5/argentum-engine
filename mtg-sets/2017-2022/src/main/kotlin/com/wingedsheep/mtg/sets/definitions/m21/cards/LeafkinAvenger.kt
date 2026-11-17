package com.wingedsheep.mtg.sets.definitions.m21.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.references.Player

/**
 * Leafkin Avenger
 * {2}{R}{G}
 * Creature — Elemental Druid
 * 4/3
 * {T}: Add {G} for each creature with power 4 or greater you control.
 * {7}{R}: This creature deals damage equal to its power to target player or planeswalker.
 *
 * The mana ability is Gaea's Cradle's count narrowed to creatures with power 4 or greater (read
 * off projected power, so pumps count). The damage reads the source's power at resolution, falling
 * back to last-known information if it has left the battlefield (2020-06-23 ruling).
 */
val LeafkinAvenger = card("Leafkin Avenger") {
    manaCost = "{2}{R}{G}"
    colorIdentity = "RG"
    typeLine = "Creature — Elemental Druid"
    power = 4
    toughness = 3
    oracleText = "{T}: Add {G} for each creature with power 4 or greater you control.\n" +
        "{7}{R}: This creature deals damage equal to its power to target player or planeswalker."

    activatedAbility {
        cost = Costs.Tap
        effect = Effects.AddMana(
            Color.GREEN,
            DynamicAmounts.battlefield(Player.You, GameObjectFilter.Creature.powerAtLeast(4)).count(),
        )
        manaAbility = true
        description = "{T}: Add {G} for each creature with power 4 or greater you control."
    }

    activatedAbility {
        cost = Costs.Mana("{7}{R}")
        val victim = target(Targets.PlayerOrPlaneswalker)
        effect = Effects.DealDamage(DynamicAmounts.sourcePower(), victim)
        description = "{7}{R}: This creature deals damage equal to its power to target player or planeswalker."
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "220"
        artist = "Filip Burburan"
        flavorText = "\"My roots are ash, my heart a smoldering ember. What I could not protect, I will avenge.\""
        imageUri = "https://cards.scryfall.io/normal/front/7/b/7bd3a903-23e0-4b5a-9c7e-390d5ced8371.jpg?1783930662"

        ruling(
            "2020-06-23",
            "If Leafkin Avenger leaves the battlefield while its last ability is on the stack, use its power as it " +
                "last existed on the battlefield to determine how much damage it deals."
        )
    }
}
