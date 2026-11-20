package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.*
import com.wingedsheep.engine.support.*
import com.wingedsheep.mtg.sets.definitions.woe.cards.WerefoxBodyguard
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/** WOE #39: optional target, but an automatic duration return during source sacrifice. */
class WerefoxBodyguardScenarioTest : FunSpec({
    test("source requirement - paying the source sacrifice cost ends exile before priority") {
        val d=exileReturnDriver()
        val victim=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val source=d.enterExileSource("Werefox Bodyguard",victim)
        val response=d.putCardInHand(d.player1,"Opt")
        val activated=d.submit(ActivateAbility(d.player1,source,WerefoxBodyguard.script.activatedAbilities.single().id,
            paymentStrategy=PaymentStrategy.FromPool))
        activated.error shouldBe null
        d.recordExileCheckpoint("Werefox Bodyguard","source-sacrificed-for-cost",source,victim,activated.events)
        val returnedBeforeResponse=d.getPermanents(d.player2).contains(victim)
        val stackBeforeResponse=d.stackSize
        d.castExileControl("Opt",cardId=response)
        d.recordExileCheckpoint("Werefox Bodyguard","response-announced",source,victim)
        returnedBeforeResponse shouldBe true
        stackBeforeResponse shouldBe 1
        activated.events.filterIsInstance<ZoneChangeEvent>().any {
            it.entityId==victim && it.transitionCause==ZoneTransitionCause.DURATION_RETURN
        } shouldBe true
    }
})
