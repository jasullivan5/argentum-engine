package com.wingedsheep.mtg.sets.definitions.m21.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Patterns
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.references.Player

/**
 * Kinetic Augur
 * {3}{R}
 * Creature — Human Shaman
 * star/4
 * Trample
 * Kinetic Augur's power is equal to the number of instant and sorcery cards in your graveyard.
 * When this creature enters, discard up to two cards, then draw that many cards.
 *
 * The power CDA is the Enigma Drake `dynamicPower` shape; the enter trigger is the
 * "discard up to two, then draw that many" loot ([Patterns.Hand.discardUpToThenDraw]) — one choice
 * of zero to two cards, not a repeated discard/draw (2020-06-23 ruling).
 */
val KineticAugur = card("Kinetic Augur") {
    manaCost = "{3}{R}"
    colorIdentity = "R"
    typeLine = "Creature — Human Shaman"
    toughness = 4
    oracleText = "Trample (This creature can deal excess combat damage to the player or planeswalker it's attacking.)\n" +
        "Kinetic Augur's power is equal to the number of instant and sorcery cards in your graveyard.\n" +
        "When this creature enters, discard up to two cards, then draw that many cards."

    dynamicPower(
        DynamicAmounts.count(Player.You, Zone.GRAVEYARD, GameObjectFilter.InstantOrSorcery)
    )

    keywords(Keyword.TRAMPLE)

    triggeredAbility {
        trigger = Triggers.self.enters()
        effect = Patterns.Hand.discardUpToThenDraw(2)
        description = "When this creature enters, discard up to two cards, then draw that many cards."
    }

    metadata {
        rarity = Rarity.UNCOMMON
        collectorNumber = "154"
        artist = "Colin Boyer"
        imageUri = "https://cards.scryfall.io/normal/front/d/c/dc5e8221-fc2d-4d90-80f3-729606648c54.jpg?1783930687"

        ruling("2020-06-23", "The ability that defines Kinetic Augur's power works in all zones, not just on the battlefield.")
        ruling(
            "2020-06-23",
            "As Kinetic Augur's triggered ability resolves, you choose whether to discard zero, one, or two cards, " +
                "discard them, and draw that many. You can't discard one card, draw a card, then choose to discard again."
        )
    }
}
