package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.*
import com.wingedsheep.engine.handlers.EffectContext
import com.wingedsheep.engine.handlers.effects.linkedexile.LinkedExileLookup
import com.wingedsheep.engine.state.components.battlefield.BattlefieldEntryTimestampComponent
import com.wingedsheep.engine.state.components.identity.TokenComponent
import com.wingedsheep.engine.support.*
import com.wingedsheep.sdk.core.Zone
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/** LRW #34, Oracle and 2007-10-01 ruling: independent triggers, not an exile duration. */
class OblivionRingScenarioTest : FunSpec({
    test("ordinary exile and later independent return renew the target under its owner") {
        val d=exileReturnDriver()
        val target=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val before=checkNotNull(d.state.objectRef(target))
        val source=d.enterExileSource("Oblivion Ring",target)
        d.getExile(d.player2).contains(target) shouldBe true
        LinkedExileLookup.exiledCards(d.state,source) shouldBe listOf(target)
        d.castExileControl("Boomerang",source)
        d.bothPass().error shouldBe null
        // The printed return really is an independent trigger: it has not resolved yet.
        d.getExile(d.player2).contains(target) shouldBe true
        d.stackSize shouldBe 1
        d.bothPass().error shouldBe null
        d.getPermanents(d.player2).contains(target) shouldBe true
        d.getController(target) shouldBe d.player2
        d.state.isCurrentObject(before) shouldBe false
    }
    test("departure and empty return before entry resolution do not cancel the legal late exile") {
        val d=exileReturnDriver()
        val target=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val before=checkNotNull(d.state.objectRef(target))
        val source=d.castExileControl("Oblivion Ring")
        d.bothPass().error shouldBe null
        val choice=d.pendingDecision as ChooseTargetsDecision
        d.submitTargetSelection(choice.playerId,listOf(target)).error shouldBe null
        d.castExileControl("Boomerang",source)
        d.bothPass().error shouldBe null
        d.stackSize shouldBe 2
        d.bothPass().error shouldBe null
        d.stackSize shouldBe 1
        d.getPermanents(d.player2).contains(target) shouldBe true
        val result=d.bothPass()
        result.error shouldBe null
        d.getExile(d.player2).contains(target) shouldBe true
        d.state.isCurrentObject(before) shouldBe false
        d.stackSize shouldBe 0
        d.state.zoneReturns.isEmpty() shouldBe true
        d.state.departedLinkedExile.values.flatten().contains(target) shouldBe true
        result.events.filterIsInstance<ZoneChangeEvent>().single { it.entityId==target }.transitionCause shouldBe ZoneTransitionCause.PRIMARY
    }
    test("a renewed source keeps its new pile separate from the old visit's late exile") {
        val d=exileReturnDriver()
        val a=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val b=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val source=d.castExileControl("Oblivion Ring")
        d.bothPass().error shouldBe null
        val choice=d.pendingDecision as ChooseTargetsDecision
        d.submitTargetSelection(choice.playerId,listOf(a)).error shouldBe null
        val old=checkNotNull(d.state.objectRef(source))
        val visit=d.state.getEntity(source)!!.get<BattlefieldEntryTimestampComponent>()!!.timestamp
        d.castExileControl("Flicker of Fate",source)
        d.settleExileControl(b)
        d.state.isCurrentObject(old) shouldBe false
        d.getExile(d.player2).containsAll(listOf(a,b)) shouldBe true
        LinkedExileLookup.exiledCards(d.state,source) shouldBe listOf(b)
        LinkedExileLookup.exiledCards(d.state,EffectContext(sourceId=source,controllerId=d.player1,sourceBattlefieldTimestamp=visit)) shouldBe listOf(a)
        d.castExileControl("Boomerang",source);d.settleExileControl()
        d.getPermanents(d.player2).contains(b) shouldBe true
        d.getExile(d.player2).contains(a) shouldBe true
    }
    test("same-name sources have independent linked piles") {
        val d=exileReturnDriver()
        val a=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val b=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val first=d.enterExileSource("Oblivion Ring",a)
        val second=d.enterExileSource("Oblivion Ring",b)
        d.castExileControl("Boomerang",first);d.settleExileControl()
        d.getPermanents(d.player2).contains(a) shouldBe true
        LinkedExileLookup.exiledCards(d.state,second) shouldBe listOf(b)
        d.castExileControl("Boomerang",second);d.settleExileControl()
        d.getPermanents(d.player2).contains(b) shouldBe true
    }
    test("target departure and renewal before resolution invalidate the old target object") {
        val d=exileReturnDriver()
        val target=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        d.castExileControl("Oblivion Ring");d.bothPass()
        val choice=d.pendingDecision as ChooseTargetsDecision
        d.submitTargetSelection(choice.playerId,listOf(target))
        d.castExileControl("Boomerang",target);d.bothPass()
        // Native zone service seeds a return of the same card with a different generation.
        d.replaceState(d.zones.moveToZone(d.state,target,Zone.BATTLEFIELD).state)
        d.settleExileControl()
        d.getPermanents(d.player2).contains(target) shouldBe true
        d.getExile(d.player2).contains(target) shouldBe false
    }
    test("entry targeting excludes lands and the source itself") {
        val d=exileReturnDriver()
        val target=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val land=d.putLandOnBattlefield(d.player1,"Forest")
        val source=d.castExileControl("Oblivion Ring");d.bothPass()
        val legal=(d.pendingDecision as ChooseTargetsDecision).legalTargets[0].orEmpty()
        legal.contains(target) shouldBe true
        legal.contains(land) shouldBe false
        legal.contains(source) shouldBe false
    }
    test("a card leaving exile and entering it again has lost the old link") {
        val d=exileReturnDriver()
        val target=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val source=d.enterExileSource("Oblivion Ring",target)
        d.replaceState(d.zones.moveToZone(d.state,target,Zone.HAND).state)
        d.replaceState(d.zones.moveToZone(d.state,target,Zone.EXILE).state)
        d.castExileControl("Boomerang",source);d.settleExileControl()
        d.getExile(d.player2).contains(target) shouldBe true
        d.getPermanents(d.player2).contains(target) shouldBe false
    }
    test("an exiled token is not returned as an original card") {
        val d=exileReturnDriver()
        val token=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        d.replaceState(d.state.updateEntity(token) { it.with(TokenComponent) })
        val source=d.enterExileSource("Oblivion Ring",token)
        d.castExileControl("Boomerang",source);d.settleExileControl()
        d.state.getBattlefield().contains(token) shouldBe false
    }
})
