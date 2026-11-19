package com.wingedsheep.mtg.sets.definitions.rav.cards

import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.ModifyStats
import com.wingedsheep.sdk.scripting.effects.CardSource
import com.wingedsheep.sdk.scripting.effects.EmitLibrarySearchedEventEffect
import com.wingedsheep.sdk.scripting.references.Player

/**
 * Sunforger — Ravnica: City of Guilds #272
 * {3} · Artifact — Equipment · Rare
 *
 * Equipped creature gets +4/+0.
 * {R}{W}, Unattach this Equipment: Search your library for a red or white instant card with mana
 * value 4 or less and cast that card without paying its mana cost. Then shuffle.
 * Equip {3}
 *
 * Modelling notes:
 * - **New SDK vocabulary: `CostAtom.Unattach`** (`Costs.Unattach`). The *effect*
 *   `UnattachEquipmentEffect` already existed for "unattach it" riders; the *cost* did not, and it
 *   is not a lookalike of any existing atom — a sacrifice moves zones, a tap can be restored, and
 *   this does neither. Its affordability gate is the card's own 2020-08-07 ruling: "You can't pay
 *   the cost of unattaching Sunforger unless Sunforger is attached to a creature", so the ability
 *   is not even offered while it sits unattached. Payment runs through the same
 *   `ZoneMovementUtils.unattachEmittingEvent` chokepoint as the effect, so a "becomes unattached"
 *   trigger cannot tell the two apart.
 * - The activated ability is at **instant speed** — the whole point of the card, and what
 *   `activatedAbility` gives by default. Unattaching mid-combat is legal and is how the card is
 *   played; nothing about the cost implies the sorcery timing that `equipAbility` carries.
 * - The search is the ordinary Gather → Select → cast pipeline rather than
 *   `Patterns.Library.searchLibrary`, because that helper's `SearchDestination` vocabulary has no
 *   "cast it" case — hand / battlefield / graveyard / top of library are all it can reach.
 *   `ChooseUpTo(1)` rather than `ChooseExactly(1)` is the "search … and cast" reading every such
 *   card gets: a search may always fail to find (CR 701.19c).
 * - Cast before shuffling, preserving the printed sequence even when announcing the found
 *   spell pauses for target selection. The native pipeline continuation resumes the shuffle
 *   after that cast; it does not wait for the newly cast spell to resolve.
 * - Casting "without paying its mana cost" carries the printed rulings for free: no alternative
 *   costs, mandatory additional costs still owed, and X = 0 — all of them properties of
 *   [CastFromCollectionWithoutPayingCostEffect] rather than of this card.
 */
val Sunforger = card("Sunforger") {
    manaCost = "{3}"
    colorIdentity = "RW"
    typeLine = "Artifact — Equipment"
    oracleText = "Equipped creature gets +4/+0.\n" +
        "{R}{W}, Unattach this Equipment: Search your library for a red or white instant card " +
        "with mana value 4 or less and cast that card without paying its mana cost. Then shuffle.\n" +
        "Equip {3}"

    staticAbility {
        ability = ModifyStats(4, 0)
    }

    activatedAbility {
        cost = Costs.Composite(Costs.Mana("{R}{W}"), Costs.Unattach)
        effect = Effects.Pipeline {
            val sunforgerSearch = gather(
                CardSource.FromZone(
                    Zone.LIBRARY,
                    Player.You,
                    GameObjectFilter.Instant
                        .withAnyColor(Color.RED, Color.WHITE)
                        .manaValueAtMost(4)
                ),
                search = true
            )
            val sunforgerFound = chooseUpTo(
                1,
                from = sunforgerSearch,
                prompt = "Search for a red or white instant card with mana value 4 or less to " +
                    "cast without paying its mana cost"
            )
            run(Effects.CastFromCollectionWithoutPayingCost(from = sunforgerFound))
            run(Effects.ShuffleLibrary())
            run(EmitLibrarySearchedEventEffect)
        }
    }

    equipAbility("{3}")

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "272"
        artist = "Darrell Riche"
        imageUri = "https://cards.scryfall.io/normal/front/c/8/c8d5e027-aa74-444b-a945-1bafa9bdec4c.jpg?1783943594"

        ruling(
            "2020-08-07",
            "You can't pay the cost of unattaching Sunforger unless Sunforger is attached to a creature."
        )
        ruling(
            "2020-08-07",
            "If you cast a spell \"without paying its mana cost,\" you can't choose to cast it for " +
                "any alternative costs. You can, however, pay additional costs. If the card has " +
                "any mandatory additional costs, those must be paid to cast the spell."
        )
        ruling(
            "2020-08-07",
            "If a spell has {X} in its mana cost, you must choose 0 as the value of X when casting " +
                "it without paying its mana cost."
        )
    }
}.let { it.copy(oracleId = "d1421070-a3cc-4af3-bf91-0f97372f4161",
    metadata = it.metadata.copy(scryfallId = "c8d5e027-aa74-444b-a945-1bafa9bdec4c", releaseDate = "2005-10-07")) }
