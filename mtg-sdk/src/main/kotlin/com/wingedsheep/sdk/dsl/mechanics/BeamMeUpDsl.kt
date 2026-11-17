package com.wingedsheep.sdk.dsl

import com.wingedsheep.sdk.scripting.KeywordAbility

/**
 * Add Beam me up [cost] (Star Trek).
 *
 * "You may cast this card from your graveyard for [cost] if you also return a creature you control
 * to its owner's hand. Then exile this spell."
 *
 * Display-only at the DSL layer — the engine reads the [KeywordAbility.BeamMeUp] entry through
 * `BeamMeUp.options`, the single source for printed and granted (Scotty, Miracle Worker) offers:
 * `CastFromZoneEnumerator.enumerateBeamMeUp` surfaces one graveyard cast per offer at the spell's
 * normal timing, the cast charges the beam me up mana and owes the creature return as a cost
 * (creatures flagged `CANT_BE_BEAMED_UP` can't pay it), and `StackExitRider` exiles the spell on
 * every stack exit. Only instants and sorceries can be cast this way.
 */
fun CardBuilder.beamMeUp(cost: String) {
    keywordAbilityList.add(KeywordAbility.beamMeUp(cost))
}
