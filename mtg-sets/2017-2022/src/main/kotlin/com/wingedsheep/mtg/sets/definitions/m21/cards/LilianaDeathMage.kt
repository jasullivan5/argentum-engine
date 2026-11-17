package com.wingedsheep.mtg.sets.definitions.m21.cards

import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.dsl.times
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Liliana, Death Mage
 * {4}{B}{B}
 * Legendary Planeswalker — Liliana
 * Loyalty: 4
 *
 * +1: Return up to one target creature card from your graveyard to your hand.
 * −3: Destroy target creature. Its controller loses 2 life.
 * −7: Target opponent loses 2 life for each creature card in their graveyard.
 *
 * The −3's controller still loses 2 life when the creature survives (indestructible); an illegal
 * target fizzles the whole ability (ruling).
 */
val LilianaDeathMage = card("Liliana, Death Mage") {
    manaCost = "{4}{B}{B}"
    colorIdentity = "B"
    typeLine = "Legendary Planeswalker — Liliana"
    startingLoyalty = 4
    oracleText = "+1: Return up to one target creature card from your graveyard to your hand.\n" +
        "−3: Destroy target creature. Its controller loses 2 life.\n" +
        "−7: Target opponent loses 2 life for each creature card in their graveyard."

    loyaltyAbility(+1) {
        val creatureCard = target(
            TargetFilter(GameObjectFilter.Creature.ownedByYou(), zone = Zone.GRAVEYARD),
            optional = true,
        )
        effect = Effects.Move(creatureCard, Zone.HAND)
    }

    loyaltyAbility(-3) {
        val creature = target(TargetFilter.Creature)
        effect = Effects.Destroy(creature) then Effects.LoseLife(2, EffectTarget.TargetController)
    }

    loyaltyAbility(-7) {
        val opponent = target(Targets.Opponent)
        effect = Effects.LoseLife(
            DynamicAmounts.creatureCardsInYourGraveyard(Player.TargetOpponent) * 2,
            opponent
        )
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "328"
        artist = "Kieran Yanner"
        imageUri = "https://cards.scryfall.io/normal/front/9/2/921e3dc3-f8eb-4498-a6a8-b424952230cd.jpg?1783930620"
        ruling(
            "2020-06-23",
            "If the target creature is an illegal target by the time Liliana's second ability tries to resolve, " +
                "the ability won't resolve. No player loses 2 life. If the target is legal but not destroyed " +
                "(most likely because it has indestructible), its controller does lose 2 life."
        )
    }
}
