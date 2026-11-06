package com.wingedsheep.engine.handlers.effects.zones

import com.wingedsheep.engine.core.*
import com.wingedsheep.engine.handlers.EffectContext
import com.wingedsheep.engine.handlers.effects.EffectExecutor
import com.wingedsheep.engine.handlers.effects.ZoneTransitionService
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.scripting.effects.SacrificeSelfEffect
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import kotlin.reflect.KClass

/**
 * Executor for SacrificeSelfEffect.
 *
 * Sacrifices the source permanent (the card that has this effect).
 */
class SacrificeSelfExecutor(private val zones: ZoneTransitionService) : EffectExecutor<SacrificeSelfEffect> {

    override val effectType: KClass<SacrificeSelfEffect> = SacrificeSelfEffect::class

    override fun execute(
        state: GameState,
        effect: SacrificeSelfEffect,
        context: EffectContext
    ): EffectResult {
        val sourceId = context.resolveTarget(EffectTarget.Self, state)
            ?: return EffectResult.success(state)
        val controllerId = context.controllerId
        val battlefieldZone = state.logicalZone(sourceId)
            ?.takeIf { it.zoneType == Zone.BATTLEFIELD && sourceId in state.getZone(it) }
            ?: return EffectResult.success(state)
        if (state.projectedState.getController(sourceId) != controllerId) {
            return EffectResult.success(state)
        }

        val sourceName = state.getEntity(sourceId)?.get<CardComponent>()?.name ?: "Unknown"

        var newState = ZoneTransitionService.trackPermanentSacrifice(state, listOf(sourceId), controllerId)

        val transitionResult = zones.moveToZone(
            newState, sourceId, Zone.GRAVEYARD, fromZoneKey = battlefieldZone
        )

        val events = mutableListOf<GameEvent>()
        events.add(PermanentsSacrificedEvent(controllerId, listOf(sourceId), listOf(sourceName)))
        events.addAll(transitionResult.events)

        return EffectResult.success(transitionResult.state, events)
    }
}
