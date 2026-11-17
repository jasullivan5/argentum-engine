package com.wingedsheep.mtg.sets.definitions.m21.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Conditions
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.dsl.grantedTriggeredAbility
import com.wingedsheep.sdk.dsl.unaryMinus
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.Duration
import com.wingedsheep.sdk.scripting.effects.CardSource
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget

/**
 * Liliana, Waker of the Dead
 * {2}{B}{B}
 * Legendary Planeswalker — Liliana
 * Loyalty: 4
 *
 * +1: Each player discards a card. Each opponent who can't loses 3 life.
 * −3: Target creature gets -X/-X until end of turn, where X is the number of cards in your graveyard.
 * −7: You get an emblem with "At the beginning of combat on your turn, put target creature card
 * from a graveyard onto the battlefield under your control. It gains haste."
 *
 * The +1 follows its ruling exactly: the opponents with empty hands are recorded first (they're the
 * ones who can't discard), then each player in APNAP order chooses a card from their own hand
 * without any card leaving it, and all the chosen cards are discarded together in one move; only
 * then do the recorded opponents lose 3 life. An empty-handed controller just discards nothing.
 * The −3's X is locked in as it resolves. The emblem's haste is indefinite (ruling).
 */
val LilianaWakerOfTheDead = card("Liliana, Waker of the Dead") {
    manaCost = "{2}{B}{B}"
    colorIdentity = "B"
    typeLine = "Legendary Planeswalker — Liliana"
    startingLoyalty = 4
    oracleText = "+1: Each player discards a card. Each opponent who can't loses 3 life.\n" +
        "−3: Target creature gets -X/-X until end of turn, where X is the number of cards in your graveyard.\n" +
        "−7: You get an emblem with \"At the beginning of combat on your turn, put target creature card " +
        "from a graveyard onto the battlefield under your control. It gains haste.\""

    loyaltyAbility(+1) {
        effect = Effects.Pipeline(
            descriptionOverride = "Each player discards a card. Each opponent who can't loses 3 life."
        ) {
            val cant = forEachPlayerCollecting(Player.EachOpponent) {
                listOf(storePlayer(onlyIf = Conditions.EmptyHand))
            }.single()
            val (toDiscard) = forEachPlayerCollecting(Player.ActivePlayerFirst) {
                val hand = gather(CardSource.FromZone(Zone.HAND, Player.You))
                listOf(chooseExactly(1, hand, prompt = "Choose a card to discard"))
            }
            discard(toDiscard)
            run(Effects.ForEachPlayer(cant.asPlayers, Effects.LoseLife(3, EffectTarget.Controller)))
        }
    }

    loyaltyAbility(-3) {
        val creature = target(TargetFilter.Creature)
        val x = DynamicAmounts.cardsInYourGraveyard()
        effect = Effects.ModifyStats(-x, -x, creature)
    }

    loyaltyAbility(-7) {
        effect = Effects.CreateGlobalTriggeredAbility(
            ability = grantedTriggeredAbility {
                trigger = Triggers.you.beginningOf(Step.BEGIN_COMBAT)
                val reanimated = target(TargetFilter.CreatureInGraveyard)
                effect = Effects.Move(
                    reanimated,
                    Zone.BATTLEFIELD,
                    controllerOverride = EffectTarget.Controller,
                    fromZone = Zone.GRAVEYARD
                ) then Effects.GrantKeyword(Keyword.HASTE, reanimated, Duration.Permanent)
                description = "At the beginning of combat on your turn, put target creature card from a " +
                    "graveyard onto the battlefield under your control. It gains haste."
            },
            descriptionOverride = "At the beginning of combat on your turn, put target creature card from a " +
                "graveyard onto the battlefield under your control. It gains haste."
        )
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "108"
        artist = "Anna Steinbauer"
        imageUri = "https://cards.scryfall.io/normal/front/e/3/e329a3e2-6702-4758-8aac-c3017e77b619.jpg?1783930705"
        ruling(
            "2020-06-23",
            "As Liliana's first ability resolves, first the player whose turn it is chooses a card in hand " +
                "without revealing it, then each other player in turn order does the same. Then all the chosen " +
                "cards are discarded at the same time. Finally, each opponent who couldn't discard a card loses 3 life."
        )
        ruling(
            "2020-06-23",
            "A player who has a card in hand can't choose to lose 3 life instead of discarding a card."
        )
        ruling(
            "2020-06-23",
            "If you have no cards in hand, you just don't discard anything. You don't lose 3 life. " +
                "Other players still discard if able."
        )
        ruling(
            "2020-06-23",
            "The value of X is determined only as Liliana's second ability resolves. Once that happens, " +
                "the value of X won't change later in the turn even if the number of cards in your graveyard changes."
        )
        ruling("2020-06-23", "Creatures put onto the battlefield with Liliana's emblem gain haste indefinitely.")
    }
}
