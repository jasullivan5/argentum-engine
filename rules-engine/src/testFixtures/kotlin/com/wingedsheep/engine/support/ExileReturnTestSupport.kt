package com.wingedsheep.engine.support

import com.wingedsheep.engine.core.*
import com.wingedsheep.engine.state.components.stack.ChosenTarget
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.model.EntityId
import io.kotest.matchers.shouldBe
import com.wingedsheep.engine.state.ObjectRef
import com.wingedsheep.engine.state.components.stack.TriggeredAbilityOnStackComponent
import kotlinx.serialization.json.*

/** Real registered cards, native actions and priority; state seeding is left to each scenario. */
fun exileReturnDriver() = GameTestDriver().apply {
    registerCards(TestCards.all)
    initMirrorMatch(deck=Deck.of("Plains" to 40),startingPlayer=0)
    passPriorityUntil(Step.PRECOMBAT_MAIN)
}

fun GameTestDriver.castExileControl(name: String, target: EntityId?=null, cardId: EntityId?=null): EntityId {
    giveMana(player1,Color.WHITE,10);giveMana(player1,Color.BLUE,10)
    val card=cardId ?: putCardInHand(player1,name)
    submit(CastSpell(player1,card,target?.let { listOf(ChosenTarget.Permanent(it)) } ?: emptyList(),
        paymentStrategy=PaymentStrategy.FromPool)).error shouldBe null
    return card
}

fun GameTestDriver.enterExileSource(name: String, target: EntityId): EntityId {
    val source=castExileControl(name)
    bothPass().error shouldBe null
    val choice=pendingDecision as ChooseTargetsDecision
    submitTargetSelection(choice.playerId,listOf(target)).error shouldBe null
    settleExileControl()
    return source
}

fun GameTestDriver.settleExileControl(targetForNewEntry: EntityId?=null) {
    repeat(40) {
        when(val decision=pendingDecision) {
            is ChooseTargetsDecision -> submitTargetSelection(decision.playerId,listOf(checkNotNull(targetForNewEntry))).error shouldBe null
            is YesNoDecision -> submitYesNo(decision.playerId,true).error shouldBe null
            null -> if(stackSize==0) return else bothPass().error shouldBe null
            else -> autoResolveDecision()
        }
    }
    error("Unsettled exile/return scenario")
}

/** Observed native checkpoints, not a second exile/return interpreter. */
fun GameTestDriver.recordExileCheckpoint(card: String, checkpoint: String, source: EntityId,
    target: EntityId, events: List<GameEvent> = emptyList()) {
    println("LEGACY_EXILE " + buildJsonObject {
        put("card",card);put("checkpoint",checkpoint)
        put("source",state.objectRef(source)?.let { Json.encodeToJsonElement(ObjectRef.serializer(),it) } ?: JsonNull)
        put("target",state.objectRef(target)?.let { Json.encodeToJsonElement(ObjectRef.serializer(),it) } ?: JsonNull)
        put("sourceZone",state.logicalZone(source)?.zoneType?.name)
        put("targetZone",state.logicalZone(target)?.zoneType?.name)
        put("priority",priorityPlayer?.toString());put("decision",pendingDecision?.javaClass?.simpleName)
        put("stack",JsonArray(state.stack.map { id -> buildJsonObject {
            put("id",id.toString())
            state.getEntity(id)?.get<TriggeredAbilityOnStackComponent>()?.let {
                put("source",it.sourceId.toString());put("sourceVisit",it.sourceBattlefieldTimestamp)
                put("effect",Json.encodeToJsonElement(com.wingedsheep.sdk.scripting.effects.Effect.serializer(),it.effect))
            }
        }}))
        put("zoneEvents",JsonArray(events.filterIsInstance<ZoneChangeEvent>().map {
            Json.encodeToJsonElement(ZoneChangeEvent.serializer(),it)
        }))
    })
}
