package com.wingedsheep.mtg.sets.definitions.trk.cards

import com.wingedsheep.sdk.dsl.Conditions
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.GraveyardCardsHaveCastKeyword
import com.wingedsheep.sdk.scripting.GraveyardCastKeyword

/**
 * Scotty, Miracle Worker
 * {1}{U}{U}
 * Legendary Creature — Human Artificer
 * 2/3
 * During your main phase, each card in your graveyard has beam me up. The beam me up cost is equal
 * to that spell's mana cost. Scotty can't be beamed up to cast a spell this way.
 *
 * A main-phase-gated whole-graveyard beam me up grant; a null cost is "equal to that spell's mana
 * cost", and the engine offers it only for instants and sorceries (the reminder text's "Only
 * instant and sorcery spells can be cast this way"). The return filter is matched with Scotty as
 * the source, so it excludes him from offers this grant makes only — he can still be returned to
 * pay a card's printed beam me up.
 */
val ScottyMiracleWorker = card("Scotty, Miracle Worker") {
    manaCost = "{1}{U}{U}"
    colorIdentity = "U"
    typeLine = "Legendary Creature — Human Artificer"
    power = 2
    toughness = 3
    oracleText = "During your main phase, each card in your graveyard has beam me up. The beam me up cost is " +
        "equal to that spell's mana cost. Scotty can't be beamed up to cast a spell this way. (Only instant " +
        "and sorcery spells can be cast this way. You may cast a card from your graveyard for its beam me up " +
        "cost if you also return another creature you control to its owner's hand. Then exile that spell.)"

    staticAbility {
        condition = Conditions.IsYourMainPhase
        ability = GraveyardCardsHaveCastKeyword(
            GraveyardCastKeyword.BeamMeUp(returnFilter = GameObjectFilter.Creature.notSourceItself()),
            GameObjectFilter.Any,
        )
    }

    metadata {
        rarity = Rarity.MYTHIC
        collectorNumber = "79"
        artist = "Sam Kennedy"
        imageUri = "https://cards.scryfall.io/normal/front/4/2/42f77f9e-5e56-45ac-aa93-36353c92bca9.jpg?1792758783"
    }
}
