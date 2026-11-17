package com.wingedsheep.mtg.sets.definitions.tdm.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Filters
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.dsl.Patterns
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.effects.SearchDestination

/**
 * Sagu Wildling // Roost Seek
 * {4}{G} // {G}
 * Creature — Dragon // Sorcery — Omen
 * 3/3
 *
 * Sagu Wildling:
 *   Flying
 *   When this creature enters, you gain 3 life.
 *
 * Roost Seek — {G}, Sorcery — Omen:
 *   Search your library for a basic land card, reveal it, put it into your hand, then shuffle.
 *   (Also shuffle this card.)
 *
 * Omen: as Roost Seek resolves, the card is shuffled into its owner's library instead of going
 * to the graveyard — the "(Also shuffle this card.)" reminder. Declared with `omen`, not
 * `adventure`: an Adventure would exile the card and let Sagu Wildling be cast from exile.
 */
val SaguWildling = card("Sagu Wildling") {
    manaCost = "{4}{G}"
    colorIdentity = "G"
    typeLine = "Creature — Dragon"
    power = 3
    toughness = 3
    oracleText = "Flying\n" +
        "When this creature enters, you gain 3 life."

    keywords(Keyword.FLYING)

    // When this creature enters, you gain 3 life.
    triggeredAbility {
        trigger = Triggers.self.enters()
        effect = Effects.GainLife(3)
        description = "you gain 3 life."
    }

    // Roost Seek — Omen. Search your library for a basic land card, reveal it, put it into your
    // hand, then shuffle.
    omen("Roost Seek") {
        manaCost = "{G}"
        typeLine = "Sorcery — Omen"
        oracleText = "Search your library for a basic land card, reveal it, put it into your hand, " +
            "then shuffle. (Also shuffle this card.)"
        spell {
            effect = Patterns.Library.searchLibrary(
                filter = Filters.BasicLand,
                count = 1,
                destination = SearchDestination.HAND,
                shuffleAfter = true,
                reveal = true
            )
        }
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "157"
        artist = "Gaboleps"
        imageUri = "https://cards.scryfall.io/normal/front/d/8/d8b43b00-f4d1-436c-bf3f-6d414cd4ce38.jpg?1743204593"
    }
}
