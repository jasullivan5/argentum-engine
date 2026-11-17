package com.wingedsheep.mtg.sets.definitions.eoe.cards

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.dsl.DynamicAmounts
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Rarity
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter

/**
 * Diplomatic Relations
 * {2}{G}
 * Instant
 * Target creature you control gets +1/+0 and gains vigilance until end of turn. It deals damage equal to its power to target creature an opponent controls.
 */
val DiplomaticRelations = card("Diplomatic Relations") {
    manaCost = "{2}{G}"
    colorIdentity = "G"
    typeLine = "Instant"
    oracleText = "Target creature you control gets +1/+0 and gains vigilance until end of turn. It deals damage equal to its power to target creature an opponent controls."

    spell {
        val myCreature = target(TargetFilter.CreatureYouControl)
        val theirCreature = target(TargetFilter.CreatureOpponentControls, optional = true)
        effect = Effects.ModifyStats(1, 0, myCreature) then
            Effects.GrantKeyword(Keyword.VIGILANCE, myCreature) then
            Effects.DealDamage(
                amount = DynamicAmounts.powerOf(myCreature),
                target = theirCreature,
                damageSource = myCreature
            )
    }

    metadata {
        rarity = Rarity.COMMON
        collectorNumber = "177"
        artist = "Néstor Ossandón Leal"
        flavorText = "\"We will not yield until the Eumidians relinquish our Kavaron Tomorrow.\"\n—Official Kav diplomatic statement"
        imageUri = "https://cards.scryfall.io/normal/front/e/0/e0a104c5-61fb-4733-97ab-a31a15a49443.jpg?1753096637"
    }
}
