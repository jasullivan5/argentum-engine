package com.wingedsheep.mtg.sets.definitions.m21.cards

import com.wingedsheep.sdk.core.CounterType
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.dsl.Conditions
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.TargetObject

/**
 * Liliana's Scrounger
 * {2}{B}
 * Creature — Human Wizard
 * 3/2
 * At the beginning of each end step, if a creature died this turn, you may put a loyalty counter
 * on a Liliana planeswalker you control.
 *
 * Intervening-if on any creature's death this turn (it may have died before the Scrounger entered),
 * triggering once however many died. The Liliana is chosen — not targeted — as the ability resolves
 * (rulings).
 */
val LilianasScrounger = card("Liliana's Scrounger") {
    manaCost = "{2}{B}"
    colorIdentity = "B"
    typeLine = "Creature — Human Wizard"
    power = 3
    toughness = 2
    oracleText = "At the beginning of each end step, if a creature died this turn, you may put a loyalty " +
        "counter on a Liliana planeswalker you control."

    triggeredAbility {
        trigger = Triggers.anyPlayer.beginningOf(Step.END)
        interveningIf = Conditions.CreatureDiedThisTurn
        optional = true
        effect = Effects.Pipeline {
            val liliana = selectTarget(
                TargetObject(filter = TargetFilter(GameObjectFilter.Planeswalker.withSubtype("Liliana").youControl())),
                nonTargeting = true,
                prompt = "Choose a Liliana planeswalker you control to get a loyalty counter"
            )
            run(Effects.AddCountersToCollection(liliana, CounterType.LOYALTY, 1))
        }
        description = "At the beginning of each end step, if a creature died this turn, you may put a " +
            "loyalty counter on a Liliana planeswalker you control."
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "330"
        artist = "Martina Fačková"
        flavorText = "\"This arm's got plenty of use left in it. Who'd throw such a nice thing away?\""
        imageUri = "https://cards.scryfall.io/normal/front/5/a/5a64625f-937a-449e-b1fc-cd0f25b033f6.jpg?1783930619"
        ruling(
            "2020-06-23",
            "If a creature didn't die before your end step begins, the ability of Liliana's Scrounger doesn't " +
                "trigger at all. The creature may have died before Liliana's Scrounger entered the battlefield, however."
        )
        ruling("2020-06-23", "The triggered ability triggers just once, no matter how many creatures died this turn.")
        ruling(
            "2020-06-23",
            "If you control more than one Liliana planeswalker, you choose which one receives a loyalty counter " +
                "as the ability of Liliana's Scrounger resolves."
        )
    }
}
