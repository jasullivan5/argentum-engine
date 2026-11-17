package com.wingedsheep.mtg.sets.definitions.fin.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.CounterType
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.ActivationRestriction
import com.wingedsheep.sdk.scripting.TimingRule
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.scripting.GameObjectFilter

/**
 * Vivi Ornitier
 * {1}{U}{R}
 * Legendary Creature — Wizard
 * 0/3
 * {0}: Add X mana in any combination of {U} and/or {R}, where X is Vivi Ornitier's power.
 *      Activate only during your turn and only once each turn.
 * Whenever you cast a noncreature spell, put a +1/+1 counter on Vivi Ornitier and it deals 1
 * damage to each opponent.
 *
 * The mana ability is a once-per-turn, your-turn-only [ActivationRestriction] producing
 * [DynamicAmounts.sourcePower] mana that the player splits freely between {U} and {R}
 * ([Effects.AddDynamicMana] — not AddManaOfChoice, which would force all X into one color). The noncreature
 * trigger grows Vivi (its power feeds back into the mana ability) and pings each opponent — Vivi is
 * the damage source so "deals 1 damage" comes from it.
 */
val ViviOrnitier = card("Vivi Ornitier") {
    manaCost = "{1}{U}{R}"
    colorIdentity = "UR"
    typeLine = "Legendary Creature — Wizard"
    power = 0
    toughness = 3
    oracleText = "{0}: Add X mana in any combination of {U} and/or {R}, where X is Vivi Ornitier's " +
        "power. Activate only during your turn and only once each turn.\n" +
        "Whenever you cast a noncreature spell, put a +1/+1 counter on Vivi Ornitier and it deals 1 " +
        "damage to each opponent."

    activatedAbility {
        cost = Costs.Mana("{0}")
        effect = Effects.AddDynamicMana(DynamicAmounts.sourcePower(), setOf(Color.BLUE, Color.RED))
        manaAbility = true
        timing = TimingRule.ManaAbility
        restrictions = listOf(
            ActivationRestriction.OnlyDuringYourTurn,
            ActivationRestriction.OncePerTurn,
        )
    }

    triggeredAbility {
        trigger = Triggers.you.casts(GameObjectFilter.Noncreature)
        effect = Effects.AddCounters(CounterType.PLUS_ONE_PLUS_ONE, 1, EffectTarget.Self) then
            Effects.DealDamage(1, EffectTarget.PlayerRef(Player.EachOpponent), damageSource = EffectTarget.Self)
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "248"
        artist = "Toni Infante"
        imageUri = "https://cards.scryfall.io/normal/front/e/c/ecc1027a-8c07-44a0-bdde-fa2844cff694.jpg?1762773080"
    }
}
