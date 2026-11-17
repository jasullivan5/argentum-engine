package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.*
import com.wingedsheep.engine.state.components.stack.ChosenTarget
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.war.cards.PlanewideCelebration
import com.wingedsheep.sdk.core.*
import com.wingedsheep.sdk.dsl.*
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/** Source: WAR #172. A graveyard target retains its zone/object binding without an explicit fromZone. */
class PlanewideCelebrationScenarioTest : FunSpec({
    val exileResponse=card("Celebration Graveyard Response") {
        manaCost="{0}";typeLine="Instant"
        spell { val c=target(TargetFilter.CardInGraveyard);effect=Effects.Exile(c) }
    }
    fun setup()=GameTestDriver().also {
        it.registerCards(TestCards.all+listOf(PlanewideCelebration,exileResponse))
        it.initMirrorMatch(deck=Deck.of("Forest" to 40),skipMulligans=true,startingPlayer=0)
        it.passPriorityUntil(Step.PRECOMBAT_MAIN)
    }
    fun cast(d:GameTestDriver,target:ChosenTarget.Card):ExecutionResult {
        val spell=d.putCardInHand(d.player1,PlanewideCelebration.name)
        d.giveMana(d.player1,Color.GREEN,7)
        return d.submit(CastSpell(d.player1,spell,targets=listOf(target),chosenModes=listOf(1,3,3,3),
            modeTargetsOrdered=listOf(listOf(target),emptyList(),emptyList(),emptyList())))
    }
    fun finish(d:GameTestDriver) {
        repeat(30) {
            if(d.pendingDecision!=null)d.autoResolveDecision()
            else if(d.stackSize>0)d.bothPass().error shouldBe null else return
        }
        error("Unsettled celebration")
    }
    test("a targeted permanent card returns from our graveyard and all three life modes resolve") {
        val d=setup();val land=d.putCardInGraveyard(d.player1,"Forest")
        cast(d,ChosenTarget.Card(land,d.player1,Zone.GRAVEYARD)).error shouldBe null
        finish(d);d.getHand(d.player1).contains(land) shouldBe true;d.getLifeTotal(d.player1) shouldBe 32
    }
    test("a permanent in hand cannot substitute for a graveyard target") {
        val d=setup();val land=d.putCardInHand(d.player1,"Forest")
        (cast(d,ChosenTarget.Card(land,d.player1,Zone.HAND)).error!=null) shouldBe true
        d.getLifeTotal(d.player1) shouldBe 20
    }
    test("an opponent's graveyard cannot substitute for our graveyard") {
        val d=setup();val land=d.putCardInGraveyard(d.player2,"Forest")
        (cast(d,ChosenTarget.Card(land,d.player2,Zone.GRAVEYARD)).error!=null) shouldBe true
    }
    test("exiling the only target in response does not bounce it out of exile or resolve targetless modes") {
        val d=setup();val land=d.putCardInGraveyard(d.player1,"Forest")
        cast(d,ChosenTarget.Card(land,d.player1,Zone.GRAVEYARD)).error shouldBe null
        d.passPriority(d.player1)
        val response=d.putCardInHand(d.player2,exileResponse.name)
        d.castSpellWithTargets(d.player2,response,listOf(ChosenTarget.Card(land,d.player1,Zone.GRAVEYARD))).error shouldBe null
        finish(d)
        d.getHand(d.player1).contains(land) shouldBe false
        d.state.getExile(d.player1).contains(land) shouldBe true
        d.getLifeTotal(d.player1) shouldBe 20
    }
})
