package com.wingedsheep.mtg.sets.definitions.thb.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.CounterType
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Nadir Kraken
 * {1}{U}{U}
 * Creature — Kraken
 * 2/3
 * Whenever you draw a card, you may pay {1}. If you do, put a +1/+1 counter on this creature and
 * create a 1/1 blue Tentacle creature token.
 */
val NadirKraken = card("Nadir Kraken") {
    manaCost = "{1}{U}{U}"
    colorIdentity = "U"
    typeLine = "Creature — Kraken"
    oracleText = "Whenever you draw a card, you may pay {1}. If you do, put a +1/+1 counter on this creature and create a 1/1 blue Tentacle creature token."
    power = 2
    toughness = 3

    triggeredAbility {
        trigger = Triggers.you.draws()
        effect = Effects.MayPay(
            cost = Effects.PayMana("{1}"),
            then = Effects.AddCounters(counterType = CounterType.PLUS_ONE_PLUS_ONE, count = 1, target = EffectTarget.Self) then
                Effects.CreateToken(
                    power = 1,
                    toughness = 1,
                    colors = setOf(Color.BLUE),
                    creatureTypes = setOf("Tentacle"),
                    imageUri = "https://cards.scryfall.io/normal/front/7/b/7b9f04f7-587e-4774-8aaa-09e8520efacb.jpg?1783931454"
                )
        )
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "55"
        artist = "Dan Murayama Scott"
        imageUri = "https://cards.scryfall.io/normal/front/7/8/7817e039-e509-4b6f-b5a3-deb3769bbdc8.jpg?1783931582"
        ruling("2020-01-24", "While resolving Nadir Kraken's ability, you can't pay {1} multiple times to create multiple Tentacles or to give it multiple +1/+1 counters.")
        ruling("2020-01-24", "You choose whether to pay {1} after drawing the card and seeing what it is. If you draw multiple cards, you see them all before deciding how many of the resulting triggers you'll pay {1} for.")
        ruling("2020-01-24", "If Nadir Kraken leaves the battlefield after its ability has triggered, you can still pay {1} and get a Tentacle, even though you won't put a +1/+1 counter on Nadir Kraken.")
    }
}
