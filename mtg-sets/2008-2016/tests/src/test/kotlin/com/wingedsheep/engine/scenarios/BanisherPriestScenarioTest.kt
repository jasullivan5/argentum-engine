package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.ChooseTargetsDecision
import com.wingedsheep.engine.core.Outcome
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.m14.cards.BanisherPriest
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.engine.core.*
import com.wingedsheep.engine.support.*
import com.wingedsheep.engine.state.components.battlefield.*
import com.wingedsheep.engine.state.components.identity.TokenComponent
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.core.CounterType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Banisher Priest (Magic 2014 #7) — {1}{W}{W} Creature — Human Cleric 2/2.
 *
 * "When this creature enters, exile target creature an opponent controls until this creature
 *  leaves the battlefield."
 */
class BanisherPriestScenarioTest : FunSpec({

    test("exiles an opponent's creature until the Priest leaves, then the creature returns") {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all + BanisherPriest)
        driver.initMirrorMatch(deck = Deck.of("Plains" to 40), startingLife = 20)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        val me = driver.activePlayer!!
        val opponent = driver.getOpponent(me)

        val theirs = driver.putCreatureOnBattlefield(opponent, "Grizzly Bears")
        val mine = driver.putCreatureOnBattlefield(me, "Grizzly Bears")

        val priestCard = driver.putCardInHand(me, "Banisher Priest")
        driver.giveMana(me, Color.WHITE, 3)
        driver.castSpell(me, priestCard).outcome shouldBe Outcome.Done
        driver.bothPass() // Priest resolves; enters trigger asks for a target

        val decision = driver.pendingDecision.shouldBeInstanceOf<ChooseTargetsDecision>()
        decision.legalTargets[0].orEmpty() shouldContain theirs
        decision.legalTargets[0].orEmpty() shouldNotContain mine
        driver.submitTargetSelection(me, listOf(theirs))
        driver.bothPass() // trigger resolves

        driver.findPermanent(opponent, "Grizzly Bears") shouldBe null
        driver.getExile(opponent) shouldContain theirs

        // Kill the Priest — the exiled creature comes back under its owner's control.
        val priest = driver.findPermanent(me, "Banisher Priest").shouldNotBeNull()
        val bolt = driver.putCardInHand(me, "Lightning Bolt")
        driver.giveMana(me, Color.RED, 1)
        driver.castSpell(me, bolt, targets = listOf(priest)).outcome shouldBe Outcome.Done
        var guard = 0
        while (driver.state.stack.isNotEmpty() && guard++ < 10) driver.bothPass()

        driver.findPermanent(me, "Banisher Priest") shouldBe null
        driver.findPermanent(opponent, "Grizzly Bears").shouldNotBeNull()
        driver.getExile(opponent).isEmpty() shouldBe true
    }

    test("duration returns in the departure action before a player response is possible") {
        val d=exileReturnDriver()
        val target=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val original=checkNotNull(d.state.objectRef(target))
        val source=d.enterExileSource("Banisher Priest",target)
        d.state.zoneReturns.single().source shouldBe d.state.objectRef(source)
        d.castExileControl("Boomerang",source)
        val departure=d.bothPass()
        departure.error shouldBe null
        val atPriority=d.getPermanents(d.player2).contains(target)
        val responseStack=d.stackSize
        // Announce a real instant at this priority checkpoint; the return cannot be waiting below it.
        d.castExileControl("Opt")
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
        val source=d.castExileControl("Banisher Priest");d.bothPass()
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
        val source=d.castExileControl("Banisher Priest");d.bothPass()
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
    test("two same-name Priests have independent durations") {
        val d=exileReturnDriver()
        val a=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val b=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val first=d.enterExileSource("Banisher Priest",a)
        val second=d.enterExileSource("Banisher Priest",b)
        d.state.zoneReturns.size shouldBe 2
        d.castExileControl("Boomerang",first);d.bothPass()
        d.getPermanents(d.player2).contains(a) shouldBe true
        d.getExile(d.player2).contains(b) shouldBe true
        d.state.zoneReturns.single().source shouldBe d.state.objectRef(second)
    }
    test("stale exile generation is not returned by the old duration") {
        val d=exileReturnDriver()
        val target=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val source=d.enterExileSource("Banisher Priest",target)
        d.replaceState(d.zones.moveToZone(d.state,target,Zone.HAND).state)
        d.replaceState(d.zones.moveToZone(d.state,target,Zone.EXILE).state)
        d.castExileControl("Boomerang",source);d.settleExileControl()
        d.getExile(d.player2).contains(target) shouldBe true
        d.state.zoneReturns.isEmpty() shouldBe true
    }
    test("own creatures and an invalidated target cannot establish the duration") {
        val d=exileReturnDriver()
        val target=d.putCreatureOnBattlefield(d.player2,"Hill Giant")
        val mine=d.putCreatureOnBattlefield(d.player1,"Hill Giant")
        d.castExileControl("Banisher Priest");d.bothPass()
        val choice=d.pendingDecision as ChooseTargetsDecision
        choice.legalTargets[0].orEmpty().contains(mine) shouldBe false
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
        val source=d.enterExileSource("Banisher Priest",target)
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
        val source=d.enterExileSource("Banisher Priest",token)
        d.castExileControl("Boomerang",source);d.settleExileControl()
        d.state.getBattlefield().contains(token) shouldBe false
        d.state.zoneReturns.isEmpty() shouldBe true
    }
})
