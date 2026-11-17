package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.*
import com.wingedsheep.engine.state.components.battlefield.*
import com.wingedsheep.engine.state.components.stack.ChosenTarget
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.khm.cards.TyriteSanctum
import com.wingedsheep.sdk.core.*
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Deck
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TyriteSanctumScenarioTest : FunSpec({
    val legend=card("Sanctum Adoption Legend") {typeLine="Legendary Creature — Human";power=2;toughness=2}
    val noncreatureGod=card("Sanctum Adoption God") {typeLine="Kindred Enchantment — God"}
    fun setup()=GameTestDriver().also {
        it.registerCards(TestCards.all+listOf(TyriteSanctum,legend,noncreatureGod))
        it.initMirrorMatch(deck=Deck.of("Forest" to 40),skipMulligans=true,startingPlayer=0)
        it.passPriorityUntil(Step.PRECOMBAT_MAIN)
    }
    test("adding God preserves the original creature type and lasts beyond the turn") {
        val d=setup();val land=d.putPermanentOnBattlefield(d.player1,TyriteSanctum.name)
        val target=d.putCreatureOnBattlefield(d.player2,legend.name);d.giveMana(d.player1,Color.GREEN,2)
        d.submit(ActivateAbility(d.player1,land,TyriteSanctum.activatedAbilities[1].id,targets=listOf(ChosenTarget.Permanent(target)))).error shouldBe null
        d.bothPass().error shouldBe null
        d.state.projectedState.getSubtypes(target).containsAll(listOf("God","Human")) shouldBe true
        d.state.getEntity(target)!!.get<CountersComponent>()!!.getCount(CounterType.PLUS_ONE_PLUS_ONE) shouldBe 1
        d.passPriorityUntil(Step.UPKEEP)
        d.state.projectedState.getSubtypes(target).containsAll(listOf("God","Human")) shouldBe true
    }
    test("the last ability targets a noncreature God and sacrifices the land as a cost") {
        val d=setup();val land=d.putPermanentOnBattlefield(d.player1,TyriteSanctum.name)
        val target=d.putPermanentOnBattlefield(d.player2,noncreatureGod.name);d.giveMana(d.player1,Color.GREEN,4)
        d.submit(ActivateAbility(d.player1,land,TyriteSanctum.activatedAbilities[2].id,targets=listOf(ChosenTarget.Permanent(target)))).error shouldBe null
        d.state.getGraveyard(d.player1).contains(land) shouldBe true
        d.state.projectedState.hasKeyword(target,Keyword.INDESTRUCTIBLE) shouldBe false
        d.bothPass().error shouldBe null
        d.state.getEntity(target)!!.get<CountersComponent>()!!.getCount(CounterType.INDESTRUCTIBLE) shouldBe 1
        d.state.projectedState.hasKeyword(target,Keyword.INDESTRUCTIBLE) shouldBe true
    }
})
