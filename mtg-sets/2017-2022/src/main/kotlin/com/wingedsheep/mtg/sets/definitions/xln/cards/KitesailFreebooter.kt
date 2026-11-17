package com.wingedsheep.mtg.sets.definitions.xln.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Targets
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.effects.CardSource
import com.wingedsheep.sdk.scripting.effects.Chooser

/**
 * Kitesail Freebooter
 * {1}{B}
 * Creature — Human Pirate
 * 1/2
 * Flying
 * When this creature enters, target opponent reveals their hand. You choose a noncreature, nonland
 * card from it. Exile that card until this creature leaves the battlefield.
 *
 * The Duress reveal-and-choose pipeline feeding [Effects.MoveUntilSourceLeaves]: the return is a
 * duration tied to this creature's battlefield visit, not a leaves trigger, and the card goes back
 * to its owner's hand. If the Freebooter has already left by the time the trigger resolves, the
 * opponent still reveals but the move does nothing (2020-06-23 ruling).
 */
val KitesailFreebooter = card("Kitesail Freebooter") {
    manaCost = "{1}{B}"
    colorIdentity = "B"
    typeLine = "Creature — Human Pirate"
    power = 1
    toughness = 2
    oracleText = "Flying\n" +
        "When this creature enters, target opponent reveals their hand. You choose a noncreature, nonland card from it. " +
        "Exile that card until this creature leaves the battlefield."

    keywords(Keyword.FLYING)

    triggeredAbility {
        trigger = Triggers.self.enters()
        val opponent = target(Targets.Opponent)
        effect = Effects.Pipeline {
            run(Effects.RevealHand(opponent))
            val opponentHand = gather(CardSource.FromZone(Zone.HAND, opponent.asPlayer))
            val chosen = chooseExactly(
                1,
                from = opponentHand,
                chooser = Chooser.Controller,
                filter = GameObjectFilter.Noncreature and GameObjectFilter.Nonland,
                prompt = "Choose a noncreature, nonland card to exile until Kitesail Freebooter leaves the battlefield",
                selectedLabel = "Exile",
                alwaysPrompt = true,
                showAllCards = true,
            )
            run(Effects.MoveUntilSourceLeaves(chosen.asTarget, Zone.EXILE))
        }
        description = "When this creature enters, target opponent reveals their hand. You choose a noncreature, " +
            "nonland card from it. Exile that card until this creature leaves the battlefield."
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "110"
        artist = "Dan Murayama Scott"
        imageUri = "https://cards.scryfall.io/normal/front/f/6/f62fd592-4910-417d-a500-e7029f3d119f.jpg?1783935759"

        ruling(
            "2020-06-23",
            "If Kitesail Freebooter leaves the battlefield before its enters-the-battlefield ability resolves, " +
                "the opponent will reveal their hand, but no card will be exiled."
        )
    }
}
