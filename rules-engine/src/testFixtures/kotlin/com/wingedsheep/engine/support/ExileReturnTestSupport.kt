package com.wingedsheep.engine.support

import com.wingedsheep.engine.core.*
import com.wingedsheep.engine.state.components.stack.ChosenTarget
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.model.EntityId
import io.kotest.matchers.shouldBe

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
