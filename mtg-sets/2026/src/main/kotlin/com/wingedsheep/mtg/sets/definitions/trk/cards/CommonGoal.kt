package com.wingedsheep.mtg.sets.definitions.trk.cards

import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.CardSource
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Common Goal
 * {1}{G}
 * Sorcery
 * One or two target creatures you control each deal damage equal to their power to target creature
 * an opponent controls.
 *
 * Same shape as Terrific Team-Up without the pump: the opponent's creature is declared first so it
 * stays addressable across the loop, then each still-legal creature you control deals damage equal
 * to its own power, as its own damage source.
 */
val CommonGoal = card("Common Goal") {
    manaCost = "{1}{G}"
    colorIdentity = "G"
    typeLine = "Sorcery"
    oracleText = "One or two target creatures you control each deal damage equal to their power to " +
        "target creature an opponent controls."

    spell {
        val creatureOpponentControls = target(TargetFilter.CreatureOpponentControls)
        targets(TargetFilter(GameObjectFilter.Creature.youControl()), count = 2, minCount = 1)

        effect = Effects.Pipeline {
            val allTargets = gather(CardSource.ChosenTargets)
            val team = filter(allTargets, GameObjectFilter.Creature.youControl())
            run(Effects.ForEachInCollection(
                collection = team,
                effect = Effects.DealDamage(
                    amount = DynamicAmounts.powerOf(EffectTarget.IterationEntity),
                    target = creatureOpponentControls,
                    damageSource = EffectTarget.IterationEntity,
                ),
            ))
        }
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "190"
        artist = "Ioannis Fiore"
        flavorText = "\"So be it, human. We shall work together, if only so I can be the one to kill you.\""
        imageUri = "https://cards.scryfall.io/normal/front/4/f/4fddc950-fbc5-4cf7-b694-dcd54ffccb31.jpg?1785981568"
    }
}
