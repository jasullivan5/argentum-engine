package com.wingedsheep.mtg.sets.definitions.m21.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.ManaCost
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.dsl.Conditions
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.ModifyStats
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter

/**
 * Liliana's Devotee
 * {2}{B}
 * Creature — Human Warlock
 * 2/3
 * Zombies you control get +1/+0.
 * At the beginning of your end step, if a creature died this turn, you may pay {1}{B}. If you do,
 * create a 2/2 black Zombie creature token.
 *
 * The Death-Priest of Myrkul shape: a subtype lord plus an intervening-if (CR 603.4) end-step
 * trigger on the global "a creature died this turn" condition wrapping a single optional payment —
 * it triggers once however many creatures died, and you can't pay more than once (2020-06-23 rulings).
 */
val LilianasDevotee = card("Liliana's Devotee") {
    manaCost = "{2}{B}"
    colorIdentity = "B"
    typeLine = "Creature — Human Warlock"
    power = 2
    toughness = 3
    oracleText = "Zombies you control get +1/+0.\n" +
        "At the beginning of your end step, if a creature died this turn, you may pay {1}{B}. " +
        "If you do, create a 2/2 black Zombie creature token."

    staticAbility {
        ability = ModifyStats(
            powerBonus = 1,
            toughnessBonus = 0,
            filter = GroupFilter(GameObjectFilter.Creature.youControl().withSubtype("Zombie"))
        )
    }

    triggeredAbility {
        trigger = Triggers.you.beginningOf(Step.END)
        interveningIf = Conditions.CreatureDiedThisTurn
        effect = Effects.MayPay(
            ManaCost.parse("{1}{B}"),
            Effects.CreateToken(
                power = 2,
                toughness = 2,
                colors = setOf(Color.BLACK),
                creatureTypes = setOf("Zombie"),
                imageUri = "https://cards.scryfall.io/normal/front/3/f/3f170f84-6c36-43e2-91e6-3c7a136944b1.jpg?1783930590"
            )
        )
        description = "At the beginning of your end step, if a creature died this turn, you may pay {1}{B}. " +
            "If you do, create a 2/2 black Zombie creature token."
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "109"
        artist = "Colin Boyer"
        flavorText = "Liliana inspires horror in many and fanatical admiration in a few."
        imageUri = "https://cards.scryfall.io/normal/front/a/e/ae5d7f15-a86f-4eaa-8280-2e7f73c8ce3a.jpg?1783930704"

        ruling(
            "2020-06-23",
            "If a creature didn't die before your end step begins, the last ability of Liliana's Devotee doesn't " +
                "trigger at all. The creature may have died before Liliana's Devotee entered the battlefield, however."
        )
        ruling("2020-06-23", "The triggered ability triggers just once, no matter how many creatures died this turn.")
        ruling("2020-06-23", "While resolving the triggered ability, you can't pay more than {1}{B} to get more than one Zombie token.")
    }
}
