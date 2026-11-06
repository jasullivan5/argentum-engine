package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.PermanentsSacrificedEvent
import com.wingedsheep.engine.core.ZoneChangeEvent
import com.wingedsheep.engine.state.components.identity.ControllerComponent
import com.wingedsheep.engine.state.components.stack.TriggeredAbilityOnStackComponent
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.model.EntityId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SparkElementalScenarioTest : FunSpec({
    fun driver() = GameTestDriver().apply {
        registerCards(TestCards.all)
        initMirrorMatch(Deck.of("Island" to 40), startingPlayer = 0)
        passPriorityUntil(Step.PRECOMBAT_MAIN)
    }

    fun GameTestDriver.trigger() = requireNotNull(state.getEntity(state.stack.single())
        ?.get<TriggeredAbilityOnStackComponent>())

    fun GameTestDriver.respond(name: String, color: Color, mana: Int, target: EntityId) {
        val spell = putCardInHand(player1, name)
        giveMana(player1, color, mana)
        castSpell(player1, spell, listOf(target)).error shouldBe null
        bothPass().error shouldBe null
    }

    for (replace in listOf(false, true)) {
        test("the original Spark Elemental is sacrificed with graveyard replacement=$replace") {
            val d = driver()
            val source = d.putCreatureOnBattlefield(d.player1, "Spark Elemental")
            if (replace) d.putPermanentOnBattlefield(d.player2, "Rest in Peace")
            val original = requireNotNull(d.state.objectRef(source))
            d.passPriorityUntil(Step.END)
            d.trigger().objectReferences.source shouldBe original
            val result = d.bothPass()
            result.error shouldBe null
            result.events.filterIsInstance<PermanentsSacrificedEvent>().single().permanentIds shouldBe listOf(source)
            val move = result.events.filterIsInstance<ZoneChangeEvent>().single()
            move.oldObject shouldBe original
            move.newObject shouldBe d.state.objectRef(source)
            (move.newObject != original) shouldBe true
            move.wasSacrificed shouldBe true
            move.requestedDestination shouldBe Zone.GRAVEYARD
            move.toZone shouldBe if (replace) Zone.EXILE else Zone.GRAVEYARD
        }
    }

    test("Momentary Blink returns a new object that survives the original trigger") {
        val d = driver()
        val source = d.putCreatureOnBattlefield(d.player1, "Spark Elemental")
        val original = requireNotNull(d.state.objectRef(source))
        d.passPriorityUntil(Step.END)
        d.trigger().objectReferences.source shouldBe original
        d.respond("Momentary Blink", Color.WHITE, 2, source)
        val returned = requireNotNull(d.state.objectRef(source))
        returned.entityId shouldBe original.entityId
        (returned.generation != original.generation) shouldBe true
        d.trigger().objectReferences.source shouldBe original

        val resolved = d.bothPass()
        resolved.error shouldBe null
        resolved.events.filterIsInstance<PermanentsSacrificedEvent>() shouldBe emptyList()
        resolved.events.filterIsInstance<ZoneChangeEvent>() shouldBe emptyList()
        d.stackSize shouldBe 0
        d.state.objectRef(source) shouldBe returned
        (source in d.state.getBattlefield()) shouldBe true
    }

    test("an absent source is not replaced by another Spark Elemental") {
        val d = driver()
        val source = d.putCreatureOnBattlefield(d.player1, "Spark Elemental")
        d.passPriorityUntil(Step.END)
        d.respond("Unsummon", Color.BLUE, 1, source)
        val other = d.putCreatureOnBattlefield(d.player1, "Spark Elemental")
        val otherRef = d.state.objectRef(other)
        val resolved = d.bothPass()
        resolved.error shouldBe null
        resolved.events.filterIsInstance<PermanentsSacrificedEvent>() shouldBe emptyList()
        resolved.events.filterIsInstance<ZoneChangeEvent>() shouldBe emptyList()
        (source in d.getHand(d.player1)) shouldBe true
        d.state.objectRef(other) shouldBe otherRef
        (other in d.state.getBattlefield()) shouldBe true
    }

    test("changing the source controller in response preserves identity but prevents sacrifice") {
        val d = driver()
        val source = d.putCreatureOnBattlefield(d.player1, "Spark Elemental")
        val original = d.state.objectRef(source)
        d.passPriorityUntil(Step.END)
        d.addComponent(source, ControllerComponent(d.player2))
        d.state.projectedState.getController(source) shouldBe d.player2
        d.state.objectRef(source) shouldBe original
        val resolved = d.bothPass()
        resolved.error shouldBe null
        resolved.events.filterIsInstance<PermanentsSacrificedEvent>() shouldBe emptyList()
        resolved.events.filterIsInstance<ZoneChangeEvent>() shouldBe emptyList()
        d.state.objectRef(source) shouldBe original
    }

    test("the trigger controller can sacrifice the original permanent owned by another player") {
        val d = driver()
        val source = d.putCreatureOnBattlefield(d.player2, "Spark Elemental")
        val original = d.state.objectRef(source)
        d.addComponent(source, ControllerComponent(d.player1))
        d.passPriorityUntil(Step.END)
        d.trigger().controllerId shouldBe d.player1
        val resolved = d.bothPass()
        resolved.error shouldBe null
        resolved.events.filterIsInstance<PermanentsSacrificedEvent>().single().playerId shouldBe d.player1
        val move = resolved.events.filterIsInstance<ZoneChangeEvent>().single()
        move.oldObject shouldBe original
        move.lastKnown?.controllerId shouldBe d.player1
        move.ownerId shouldBe d.player2
        move.toZone shouldBe Zone.GRAVEYARD
        (source in d.getGraveyard(d.player2)) shouldBe true
    }
})
