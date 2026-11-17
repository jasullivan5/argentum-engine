package com.wingedsheep.engine.handlers.effects.token

import com.wingedsheep.engine.core.EffectResult
import com.wingedsheep.engine.core.EntersWithChoiceOnBattlefieldContinuation
import com.wingedsheep.engine.core.Suspension
import com.wingedsheep.engine.core.ZoneChangeEvent
import com.wingedsheep.engine.handlers.EffectContext
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.scripting.effects.CreateAuraTokenEffect
import com.wingedsheep.sdk.scripting.effects.CreatePredefinedTokenEffect
import com.wingedsheep.sdk.scripting.effects.CreateRandomCreatureTokenWithManaValueEffect
import com.wingedsheep.sdk.scripting.effects.CreateTokenBatchEffect
import com.wingedsheep.sdk.scripting.effects.CreateTokenCopyOfChosenPermanentEffect
import com.wingedsheep.sdk.scripting.effects.CreateTokenCopyOfEquippedCreatureEffect
import com.wingedsheep.sdk.scripting.effects.CreateTokenCopyOfSourceEffect
import com.wingedsheep.sdk.scripting.effects.CreateTokenCopyOfTargetEffect
import com.wingedsheep.sdk.scripting.effects.CreateTokenEffect
import com.wingedsheep.sdk.scripting.effects.Effect
import com.wingedsheep.sdk.scripting.effects.InvestigateEffect

/** Durable instruction scope; carried by the existing pipeline context across entry questions. */
internal object TokenEntryGroups {
    private const val KEY = "__simultaneousTokenEntryGroup"
    fun group(context: EffectContext): String? = context.pipeline.chosenValues[KEY]
    fun withGroup(context: EffectContext, group: String): EffectContext = context.copy(
        pipeline = context.pipeline.copy(chosenValues = context.pipeline.chosenValues + (KEY to group)))

    /**
     * Whether [effect] is one token-creation instruction — the effect types [TokenExecutors]
     * scopes to a single entry group. A "for each …" loop over such a body is still one
     * instruction, so it shares one group across its iterations.
     */
    fun createsTokens(effect: Effect): Boolean = effect::class in TOKEN_EFFECT_TYPES

    private val TOKEN_EFFECT_TYPES = setOf(
        CreateTokenBatchEffect::class, CreateTokenEffect::class, CreatePredefinedTokenEffect::class,
        InvestigateEffect::class, CreateAuraTokenEffect::class, CreateTokenCopyOfSourceEffect::class,
        CreateTokenCopyOfEquippedCreatureEffect::class, CreateTokenCopyOfChosenPermanentEffect::class,
        CreateTokenCopyOfTargetEffect::class, CreateRandomCreatureTokenWithManaValueEffect::class,
    )

    fun stamp(result: EffectResult, group: String?): EffectResult {
        if (group == null) return result
        val frames = result.state.continuationStack.map { frame ->
            val suspension = frame as? Suspension
            val answer = suspension?.answer as? EntersWithChoiceOnBattlefieldContinuation
            if (answer == null || answer.simultaneousEntryGroup != null ||
                suspension.question.id != result.pendingDecision?.id) frame
            else suspension.copy(answer = answer.copy(simultaneousEntryGroup = group))
        }
        return result.copy(state = result.state.copy(continuationStack = frames), events = result.events.map {
            if (it is ZoneChangeEvent && it.toZone == Zone.BATTLEFIELD) it.copy(simultaneousEntryGroup = group)
            else it
        })
    }
}
