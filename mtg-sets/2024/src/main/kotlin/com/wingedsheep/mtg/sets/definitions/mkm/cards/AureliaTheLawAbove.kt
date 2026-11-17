package com.wingedsheep.mtg.sets.definitions.mkm.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.references.Player
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import com.wingedsheep.sdk.dsl.Triggers

/**
 * Aurelia, the Law Above
 * {3}{R}{W}
 * Legendary Creature — Angel
 * 4/4
 * Flying, vigilance, haste
 * Whenever a player attacks with three or more creatures, you draw a card.
 * Whenever a player attacks with five or more creatures, Aurelia deals 3 damage to each of your
 * opponents and you gain 3 life.
 *
 * Both abilities are `Triggers.anyPlayer.attacks(minAttackers = n)` — one firing per
 * declare-attackers, whoever declared, the Seasoned Consultant shape widened from "you" to "a
 * player". The per-attacker `Triggers.a().attacks(AttackerCountAtLeast(n))` shape is wrong here:
 * an unfiltered ANY-binding `AttackEvent` expands to one trigger *per attacking creature*
 * (CR 603.2c), so an attack with three creatures drew three cards.
 *
 * The count is read off the declaration, so per the MKM ruling removing an attacker in response
 * doesn't undo either trigger.
 */
val AureliaTheLawAbove = card("Aurelia, the Law Above") {
    manaCost = "{3}{R}{W}"
    colorIdentity = "RW"
    typeLine = "Legendary Creature — Angel"
    oracleText = "Flying, vigilance, haste\n" +
        "Whenever a player attacks with three or more creatures, you draw a card.\n" +
        "Whenever a player attacks with five or more creatures, Aurelia deals 3 damage to each " +
        "of your opponents and you gain 3 life."
    power = 4
    toughness = 4

    keywords(Keyword.FLYING, Keyword.VIGILANCE, Keyword.HASTE)

    triggeredAbility {
        trigger = Triggers.anyPlayer.attacks(minAttackers = 3)
        effect = Effects.DrawCards(1)
        description = "Whenever a player attacks with three or more creatures, you draw a card."
    }

    triggeredAbility {
        trigger = Triggers.anyPlayer.attacks(minAttackers = 5)
        effect = Effects.DealDamage(
            3,
            EffectTarget.PlayerRef(Player.EachOpponent),
            damageSource = EffectTarget.Self
        ) then
            Effects.GainLife(3)
        description = "Whenever a player attacks with five or more creatures, Aurelia deals 3 " +
            "damage to each of your opponents and you gain 3 life."
    }

    metadata {
        rarity = Rarity.RARE
        collectorNumber = "188"
        artist = "Lie Setiawan"
        imageUri = "https://cards.scryfall.io/normal/front/8/f/8f80c6e7-e9f9-4ca6-87f7-a52c96079e4a.jpg?1783912856"

        ruling(
            "2024-02-02",
            "For both triggered abilities, it doesn't matter what happens to the attacking " +
                "creatures in response. As long as a player attacked with at least the " +
                "appropriate number of creatures, the effects of Aurelia's triggered abilities " +
                "will still occur."
        )
    }
}
