package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.ChooseTargetsDecision
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.isd.InnistradSet
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.Deck
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import com.wingedsheep.engine.core.YesNoDecision
import com.wingedsheep.engine.handlers.EffectContext
import com.wingedsheep.engine.handlers.effects.linkedexile.LinkedExileLookup
import com.wingedsheep.engine.state.components.battlefield.*
import com.wingedsheep.engine.state.components.identity.TokenComponent
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.core.CounterType
import com.wingedsheep.engine.support.*

/**
 * Fiend Hunter — {1}{W}{W} Creature — Human Cleric 1/3
 * When this creature enters, you may exile another target creature.
 * When this creature leaves the battlefield, return the exiled card under its owner's control.
 */
class FiendHunterScenarioTest : FunSpec({

    test("multiple legal creatures require one selected target before choosing the exile effect") {
        val d=exileReturnDriver()
        val mine=d.putCreatureOnBattlefield(d.player1,"Hill Giant")
        val theirs=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val source=d.castExileControl("Fiend Hunter");d.bothPass().error shouldBe null
        val choice=d.pendingDecision as ChooseTargetsDecision
        choice.legalTargets[0].orEmpty().containsAll(listOf(mine,theirs)) shouldBe true
        choice.legalTargets[0].orEmpty().contains(source) shouldBe false
        d.submitTargetSelection(d.player1,listOf(mine)).error shouldBe null
        d.bothPass().error shouldBe null
        (d.pendingDecision is YesNoDecision) shouldBe true
        d.submitYesNo(d.player1,true).error shouldBe null
        d.getExile(d.player1).contains(mine) shouldBe true
        d.getPermanents(d.player2).contains(theirs) shouldBe true
    }

    test("without another legal creature the entry trigger cannot be announced") {
        val d=exileReturnDriver()
        d.castExileControl("Fiend Hunter");d.bothPass().error shouldBe null
        d.pendingDecision shouldBe null
        d.stackSize shouldBe 0
    }
    test("owner return renews a stolen creature without old attachments counters damage or modifiers") {
        val d=exileReturnDriver()
        val target=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        d.castExileControl("Control Magic",target);d.settleExileControl()
        d.state.projectedState.getController(target) shouldBe d.player1
        d.castExileControl("Holy Strength",target);d.settleExileControl()
        d.replaceState(d.state.updateEntity(target) { it.with(CountersComponent(mapOf(CounterType.PLUS_ONE_PLUS_ONE to 2))).with(DamageComponent(1)) })
        d.giveMana(d.player1,Color.GREEN,1)
        d.castExileControl("Giant Growth",target);d.settleExileControl()
        val old=checkNotNull(d.state.objectRef(target))
        val source=d.enterExileSource("Fiend Hunter",target)
        d.castExileControl("Boomerang",source);d.settleExileControl()
        d.getController(target) shouldBe d.player2
        d.state.isCurrentObject(old) shouldBe false
        d.state.projectedState.getPower(target) shouldBe 3
        d.state.getEntity(target)?.get<CountersComponent>()?.counters.orEmpty().isEmpty() shouldBe true
        (d.state.getEntity(target)?.get<DamageComponent>()?.amount ?: 0) shouldBe 0
        for(aura in listOf("Holy Strength","Control Magic"))
            d.getGraveyard(d.player1).any { d.getCardName(it)==aura } shouldBe true
    }

    test("source requirement - a legal target is mandatory even when intending to decline") {
        val d=exileReturnDriver()
        val victim=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val source=d.castExileControl("Fiend Hunter");d.bothPass()
        val decision=d.pendingDecision as ChooseTargetsDecision
        d.recordExileCheckpoint("Fiend Hunter","target-announcement",source,victim)
        val omitted=d.submitTargetSelection(d.player1,emptyList())
        d.recordExileCheckpoint("Fiend Hunter","attempted-targetless-announcement",source,victim,omitted.events)
        (omitted.error!=null) shouldBe true
        decision.targetRequirements.single().minTargets shouldBe 1
    }
    test("source requirement - selection precedes the resolution-time choice to decline") {
        val d=exileReturnDriver()
        val victim=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val source=d.castExileControl("Fiend Hunter");d.bothPass()
        d.submitTargetSelection(d.player1,listOf(victim)).error shouldBe null
        d.pendingDecision shouldBe null
        val resolution=d.bothPass()
        d.recordExileCheckpoint("Fiend Hunter","entry-resolution-choice",source,victim,resolution.events)
        (d.pendingDecision is YesNoDecision) shouldBe true
        d.getPermanents(d.player2).contains(victim) shouldBe true
        d.submitYesNo(d.player1,false).error shouldBe null
        d.getPermanents(d.player2).contains(victim) shouldBe true
        d.getExile(d.player2).contains(victim) shouldBe false
    }
    test("source requirement - departure and empty return do not cancel accepted late exile") {
        val d=exileReturnDriver()
        val victim=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val source=d.castExileControl("Fiend Hunter");d.bothPass()
        d.submitTargetSelection(d.player1,listOf(victim)).error shouldBe null
        d.recordExileCheckpoint("Fiend Hunter","entry-pending",source,victim)
        d.castExileControl("Boomerang",source)
        val departed=d.bothPass();departed.error shouldBe null
        d.recordExileCheckpoint("Fiend Hunter","departed",source,victim,departed.events)
        d.stackSize shouldBe 2
        val empty=d.bothPass();empty.error shouldBe null
        d.recordExileCheckpoint("Fiend Hunter","empty-return-resolved",source,victim,empty.events)
        d.stackSize shouldBe 1
        val late=d.bothPass();late.error shouldBe null
        val accepted=d.submitYesNo(d.player1,true);accepted.error shouldBe null
        d.recordExileCheckpoint("Fiend Hunter","late-entry-resolved",source,victim,late.events+accepted.events)
        d.getExile(d.player2).contains(victim) shouldBe true
        d.stackSize shouldBe 0
        d.state.zoneReturns.isEmpty() shouldBe true
    }

    fun driver(): GameTestDriver {
        val d = GameTestDriver()
        d.registerCards(TestCards.all + InnistradSet.cards)
        return d
    }

    test("exiles a creature on enter and returns it when Fiend Hunter dies") {
        val d = driver()
        d.initMirrorMatch(deck = Deck.of("Plains" to 30), startingLife = 20)
        val p1 = d.activePlayer!!
        val p2 = d.getOpponent(p1)
        d.passPriorityUntil(Step.PRECOMBAT_MAIN)

        val victim = d.putCreatureOnBattlefield(p2, "Centaur Courser")

        val hunter = d.putCardInHand(p1, "Fiend Hunter")
        d.giveMana(p1, Color.WHITE, 3)
        d.castSpell(p1, hunter)
        d.bothPass() // resolve the creature; ETB trigger goes on the stack and asks for a target

        (d.pendingDecision is ChooseTargetsDecision) shouldBe true
        d.submitTargetSelection(p1, listOf(victim))
        d.bothPass() // resolve the ETB exile trigger to its optional effect
        d.submitYesNo(p1, true).error shouldBe null

        // Victim is exiled (under its owner p2's control zone), off the battlefield.
        d.getExile(p2) shouldContain victim
        d.state.getBattlefield(p2) shouldNotContain victim

        // Kill Fiend Hunter with a burn spell (3 damage to a 1/3).
        val bolt = d.putCardInHand(p1, "Lightning Bolt")
        d.giveMana(p1, Color.RED, 1)
        d.castSpell(p1, bolt, targets = listOf(hunter))
        d.bothPass() // resolve bolt -> Fiend Hunter dies -> LTB return trigger goes on stack
        d.bothPass() // resolve the LTB return trigger

        // Victim returns to the battlefield under its owner's control; exile is empty.
        d.state.getBattlefield(p2) shouldContain victim
        d.getExile(p2) shouldNotContain victim
    }

    test("may decline the exile when a legal target exists") {
        val d = driver()
        d.initMirrorMatch(deck = Deck.of("Plains" to 30), startingLife = 20)
        val p1 = d.activePlayer!!
        val p2 = d.getOpponent(p1)
        d.passPriorityUntil(Step.PRECOMBAT_MAIN)

        val bystander = d.putCreatureOnBattlefield(p2, "Centaur Courser")

        val hunter = d.putCardInHand(p1, "Fiend Hunter")
        d.giveMana(p1, Color.WHITE, 3)
        d.castSpell(p1, hunter)
        d.bothPass()

        (d.pendingDecision is ChooseTargetsDecision) shouldBe true
        // The target is required even when the resolution-time choice will be No.
        d.submitTargetSelection(p1, listOf(bystander)).error shouldBe null
        d.bothPass()
        d.submitYesNo(p1, false).error shouldBe null

        // Nothing was exiled; the bystander stays on the battlefield.
        d.getExile(p2) shouldNotContain bystander
        d.state.getBattlefield(p2) shouldContain bystander
    }
    test("a renewed source keeps its new pile separate from the old visit's late exile") {
        val d=exileReturnDriver()
        val a=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val b=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val source=d.castExileControl("Fiend Hunter")
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
        val first=d.enterExileSource("Fiend Hunter",a)
        val second=d.enterExileSource("Fiend Hunter",b)
        d.castExileControl("Boomerang",first);d.settleExileControl()
        d.getPermanents(d.player2).contains(a) shouldBe true
        LinkedExileLookup.exiledCards(d.state,second) shouldBe listOf(b)
        d.castExileControl("Boomerang",second);d.settleExileControl()
        d.getPermanents(d.player2).contains(b) shouldBe true
    }
    test("target departure and renewal before resolution invalidate the old target object") {
        val d=exileReturnDriver()
        val target=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        d.castExileControl("Fiend Hunter");d.bothPass()
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
        val source=d.castExileControl("Fiend Hunter");d.bothPass()
        val legal=(d.pendingDecision as ChooseTargetsDecision).legalTargets[0].orEmpty()
        legal.contains(target) shouldBe true
        legal.contains(land) shouldBe false
        legal.contains(source) shouldBe false
    }
    test("a card leaving exile and entering it again has lost the old link") {
        val d=exileReturnDriver()
        val target=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val source=d.enterExileSource("Fiend Hunter",target)
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
        val source=d.enterExileSource("Fiend Hunter",token)
        d.castExileControl("Boomerang",source);d.settleExileControl()
        d.state.getBattlefield().contains(token) shouldBe false
    }
})
