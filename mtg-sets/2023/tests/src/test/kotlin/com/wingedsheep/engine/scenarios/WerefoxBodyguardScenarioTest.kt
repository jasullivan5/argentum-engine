package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.*
import com.wingedsheep.engine.support.*
import com.wingedsheep.engine.state.components.battlefield.*
import com.wingedsheep.engine.state.components.identity.TokenComponent
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.core.CounterType
import com.wingedsheep.mtg.sets.definitions.woe.cards.WerefoxBodyguard
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/** WOE #39: optional target, but an automatic duration return during source sacrifice. */
class WerefoxBodyguardScenarioTest : FunSpec({
    test("up to one permits no target while another Fox and the source itself remain ineligible") {
        val d=exileReturnDriver()
        val friendly=d.putCreatureOnBattlefield(d.player1,"Hill Giant")
        val opposing=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val fox=d.putCreatureOnBattlefield(d.player2,"Werefox Bodyguard")
        val land=d.putLandOnBattlefield(d.player1,"Forest")
        val source=d.castExileControl("Werefox Bodyguard");d.bothPass().error shouldBe null
        val choice=d.pendingDecision as ChooseTargetsDecision
        choice.targetRequirements.single().minTargets shouldBe 0
        val legal=choice.legalTargets[0].orEmpty()
        legal.containsAll(listOf(friendly,opposing)) shouldBe true
        legal.any { it in listOf(fox,source,land) } shouldBe false
        d.submitTargetSelection(d.player1,emptyList()).error shouldBe null
        d.bothPass().error shouldBe null
        d.pendingDecision shouldBe null
        d.state.zoneReturns.isEmpty() shouldBe true
        d.getPermanents(d.player1).contains(friendly) shouldBe true
    }
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
    test("duration returns in the departure action before a player response is possible") {
        val d=exileReturnDriver()
        val target=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val original=checkNotNull(d.state.objectRef(target))
        val source=d.enterExileSource("Werefox Bodyguard",target)
        d.state.zoneReturns.single().source shouldBe d.state.objectRef(source)
        val response=d.putCardInHand(d.player1,"Opt")
        d.castExileControl("Boomerang",source)
        val departure=d.bothPass()
        departure.error shouldBe null
        val atPriority=d.getPermanents(d.player2).contains(target)
        val responseStack=d.stackSize
        // Announce a real instant at this priority checkpoint; the return cannot be waiting below it.
        d.castExileControl("Opt",cardId=response)
        atPriority shouldBe true
        responseStack shouldBe 0
        d.state.isCurrentObject(original) shouldBe false
        d.getController(target) shouldBe d.player2
        val changes=departure.events.filterIsInstance<ZoneChangeEvent>()
        val left=changes.indexOfFirst { it.entityId==source && it.fromZone==Zone.BATTLEFIELD }
        val returned=changes.indexOfFirst { it.entityId==target && it.toZone==Zone.BATTLEFIELD && it.transitionCause==ZoneTransitionCause.DURATION_RETURN }
        (left>=0 && returned>left) shouldBe true
        d.state.zoneReturns.isEmpty() shouldBe true
    }
    test("source departure before exile resolves prevents the initial movement") {
        val d=exileReturnDriver()
        val target=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val source=d.castExileControl("Werefox Bodyguard");d.bothPass()
        val choice=d.pendingDecision as ChooseTargetsDecision
        d.submitTargetSelection(choice.playerId,listOf(target))
        d.castExileControl("Boomerang",source);d.settleExileControl()
        d.getPermanents(d.player2).contains(target) shouldBe true
        d.getExile(d.player2).contains(target) shouldBe false
        d.state.zoneReturns.isEmpty() shouldBe true
    }
    test("renewed source cannot establish the duration of its former visit") {
        val d=exileReturnDriver()
        val a=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val b=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val source=d.castExileControl("Werefox Bodyguard");d.bothPass()
        val choice=d.pendingDecision as ChooseTargetsDecision
        d.submitTargetSelection(choice.playerId,listOf(a))
        val before=checkNotNull(d.state.objectRef(source))
        d.castExileControl("Cloudshift",source);d.settleExileControl(b)
        d.state.isCurrentObject(before) shouldBe false
        d.getPermanents(d.player2).contains(a) shouldBe true
        d.getExile(d.player2).contains(b) shouldBe true
        d.state.zoneReturns.single().source shouldBe d.state.objectRef(source)
        d.castExileControl("Boomerang",source);d.settleExileControl()
        d.getPermanents(d.player2).contains(b) shouldBe true
    }
    test("two same-name Bodyguards have independent durations") {
        val d=exileReturnDriver()
        val a=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val b=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val first=d.enterExileSource("Werefox Bodyguard",a)
        val second=d.enterExileSource("Werefox Bodyguard",b)
        d.state.zoneReturns.size shouldBe 2
        d.castExileControl("Boomerang",first);d.bothPass()
        d.getPermanents(d.player2).contains(a) shouldBe true
        d.getExile(d.player2).contains(b) shouldBe true
        d.state.zoneReturns.single().source shouldBe d.state.objectRef(second)
    }
    test("stale exile generation is not returned by the old duration") {
        val d=exileReturnDriver()
        val target=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val source=d.enterExileSource("Werefox Bodyguard",target)
        d.replaceState(d.zones.moveToZone(d.state,target,Zone.HAND).state)
        d.replaceState(d.zones.moveToZone(d.state,target,Zone.EXILE).state)
        d.castExileControl("Boomerang",source);d.settleExileControl()
        d.getExile(d.player2).contains(target) shouldBe true
        d.state.zoneReturns.isEmpty() shouldBe true
    }
    test("a friendly creature is legal but an invalidated target cannot establish the duration") {
        val d=exileReturnDriver()
        val target=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val mine=d.putCreatureOnBattlefield(d.player1,"Hill Giant")
        d.castExileControl("Werefox Bodyguard");d.bothPass()
        val choice=d.pendingDecision as ChooseTargetsDecision
        choice.legalTargets[0].orEmpty().contains(mine) shouldBe true
        d.submitTargetSelection(choice.playerId,listOf(target))
        d.castExileControl("Boomerang",target);d.settleExileControl()
        d.getHand(d.player2).contains(target) shouldBe true
        d.state.zoneReturns.isEmpty() shouldBe true
    }
    test("duration return does not preserve the departed object's Aura counters or damage") {
        val d=exileReturnDriver()
        val target=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        d.castExileControl("Holy Strength",target);d.settleExileControl()
        d.replaceState(d.state.updateEntity(target) { it.with(CountersComponent(mapOf(CounterType.PLUS_ONE_PLUS_ONE to 2))).with(DamageComponent(1)) })
        val source=d.enterExileSource("Werefox Bodyguard",target)
        d.castExileControl("Boomerang",source);d.bothPass()
        d.state.projectedState.getPower(target) shouldBe 3
        d.state.getEntity(target)?.get<CountersComponent>()?.counters.orEmpty().isEmpty() shouldBe true
        (d.state.getEntity(target)?.get<DamageComponent>()?.amount ?: 0) shouldBe 0
        d.getGraveyard(d.player1).any { d.state.getEntity(it)?.get<com.wingedsheep.engine.state.components.identity.CardComponent>()?.name=="Holy Strength" } shouldBe true
    }
    test("exiling a token never schedules its recreation as an original card") {
        val d=exileReturnDriver()
        val token=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        d.replaceState(d.state.updateEntity(token) { it.with(TokenComponent) })
        val source=d.enterExileSource("Werefox Bodyguard",token)
        d.castExileControl("Boomerang",source);d.settleExileControl()
        d.state.getBattlefield().contains(token) shouldBe false
        d.state.zoneReturns.isEmpty() shouldBe true
    }
})
