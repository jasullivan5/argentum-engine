package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.*
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.components.stack.TriggeredAbilityOnStackComponent
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.dsl.*
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.scripting.GameObjectFilter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.Json

/**
 * CR 603.3b: each player, in APNAP order, puts their own simultaneous triggered abilities on the
 * stack in any order they choose. A player who turned automatic ordering off is asked for that
 * order, before any of the abilities chooses targets (CR 603.3d); everyone else keeps the order the
 * abilities triggered in, except that one checking this turn's history as it resolves goes last.
 */
class TriggerOrderingTest : FunSpec({
    // Rumor Gatherer's and Elvish Visionary's shapes: a scry that wants to resolve before the draw.
    val gatherer = card("Order Gatherer") {
        manaCost = "{1}"; typeLine = "Creature — Test"; power = 2; toughness = 1
        triggeredAbility {
            trigger = Triggers.another(GameObjectFilter.Creature.youControl()).enters()
            effect = Effects.Scry(1)
        }
    }
    val visionary = card("Order Visionary") {
        manaCost = "{1}"; typeLine = "Creature — Test"; power = 1; toughness = 1
        triggeredAbility {
            trigger = Triggers.self.enters()
            effect = Effects.DrawCards(1)
        }
    }
    val warden = card("Order Warden") {
        manaCost = "{1}"; typeLine = "Creature — Test"; power = 1; toughness = 1
        triggeredAbility {
            trigger = Triggers.another(GameObjectFilter.Creature).enters()
            effect = Effects.GainLife(1)
        }
    }
    val sentinel = card("Order Sentinel") {
        manaCost = "{1}"; typeLine = "Creature — Test"; power = 1; toughness = 1
        triggeredAbility {
            trigger = Triggers.another(GameObjectFilter.Creature).enters()
            effect = Effects.GainLife(2)
        }
    }
    val pinger = card("Order Pinger") {
        manaCost = "{1}"; typeLine = "Creature — Test"; power = 1; toughness = 1
        triggeredAbility {
            trigger = Triggers.another(GameObjectFilter.Creature.youControl()).enters()
            val t = target(Targets.Any)
            effect = Effects.DealDamage(1, t)
        }
    }
    // Old-Growth Educator's shape: an Infusion check made as the ability resolves.
    val educator = card("Order Educator") {
        manaCost = "{1}"; typeLine = "Creature — Test"; power = 4; toughness = 4
        triggeredAbility {
            trigger = Triggers.self.enters()
            effect = Effects.If(Conditions.YouGainedLifeThisTurn, Effects.DrawCards(1))
        }
    }
    val lumaret = card("Order Lumaret") {
        manaCost = "{1}"; typeLine = "Creature — Test"; power = 2; toughness = 2
        triggeredAbility {
            trigger = Triggers.a(GameObjectFilter.Creature.youControl()).enters()
            effect = Effects.GainLife(1)
        }
    }
    val bear = card("Order Bear") {
        manaCost = "{1}"; typeLine = "Creature — Test"; power = 2; toughness = 2
    }

    fun driver(): GameTestDriver {
        val d = GameTestDriver()
        d.registerCards(TestCards.all + listOf(gatherer, visionary, warden, sentinel, pinger, educator, lumaret, bear))
        d.initMirrorMatch(Deck.of("Plains" to 40), skipMulligans = true, startingPlayer = 0)
        d.passPriorityUntil(Step.PRECOMBAT_MAIN)
        return d
    }

    fun GameTestDriver.manual(players: List<EntityId>) {
        players.forEach { replaceState(state.withManualTriggerOrdering(it, true)) }
    }

    fun GameTestDriver.cast(name: String): ExecutionResult {
        giveColorlessMana(player1, 1)
        castSpell(player1, putCardInHand(player1, name)).error shouldBe null
        return bothPass().also { it.error shouldBe null }
    }

    /** The stack's triggered abilities by source name, top (first to resolve) first. */
    fun GameTestDriver.stackTopFirst(): List<String> = state.stack.asReversed().map {
        state.getEntity(it)!!.get<TriggeredAbilityOnStackComponent>()!!.sourceName
    }

    fun GameTestDriver.order(resolutionOrder: List<Int>): ExecutionResult {
        val decision = pendingDecision.shouldBeInstanceOf<OrderTriggersDecision>()
        return submitDecision(decision.playerId, TriggerOrderResponse(decision.id, resolutionOrder))
    }

    test("with automatic ordering, simultaneous triggers keep their detection order and nobody is asked") {
        val d = driver()
        d.putCreatureOnBattlefield(d.player1, gatherer.name)
        d.cast(visionary.name)
        d.pendingDecision shouldBe null
        // Visionary's own trigger was detected first, so its draw resolves before the scry.
        d.stackTopFirst() shouldContainExactly listOf(visionary.name, gatherer.name)
    }

    test("automatic ordering says so when the order could have mattered") {
        val d = driver()
        d.putCreatureOnBattlefield(d.player1, gatherer.name)
        val events = d.cast(visionary.name).events
        events.filterIsInstance<TriggersOrderedAutomaticallyEvent>() shouldContainExactly
            listOf(TriggersOrderedAutomaticallyEvent(d.player1, 2))
    }

    test("interchangeable copies, a single trigger, or a player ordering their own: nothing to say") {
        val copies = driver()
        repeat(2) { copies.putCreatureOnBattlefield(copies.player1, warden.name) }
        copies.cast(bear.name).events.none { it is TriggersOrderedAutomaticallyEvent } shouldBe true

        val single = driver()
        single.putCreatureOnBattlefield(single.player1, warden.name)
        single.cast(bear.name).events.none { it is TriggersOrderedAutomaticallyEvent } shouldBe true

        val manual = driver()
        manual.putCreatureOnBattlefield(manual.player1, gatherer.name)
        manual.manual(listOf(manual.player1))
        manual.cast(visionary.name).events.none { it is TriggersOrderedAutomaticallyEvent } shouldBe true
    }

    test("while one player orders their own triggers, the other still hears theirs were ordered for them") {
        val d = driver()
        d.putCreatureOnBattlefield(d.player1, gatherer.name)
        d.putCreatureOnBattlefield(d.player2, warden.name)
        d.putCreatureOnBattlefield(d.player2, sentinel.name)
        d.manual(listOf(d.player2))
        val events = d.cast(visionary.name).events
        d.pendingDecision.shouldBeInstanceOf<OrderTriggersDecision>().playerId shouldBe d.player2
        events.filterIsInstance<TriggersOrderedAutomaticallyEvent>() shouldContainExactly
            listOf(TriggersOrderedAutomaticallyEvent(d.player1, 2))
    }

    test("with automatic ordering, an ability checking this turn's history resolves after the others") {
        val d = driver()
        d.putCreatureOnBattlefield(d.player1, lumaret.name)
        d.cast(educator.name)
        d.pendingDecision shouldBe null
        // Whatever order detection found them in, the check waits for the lifegain.
        d.stackTopFirst() shouldContainExactly listOf(lumaret.name, educator.name)
    }

    test("asked for the order, the history check is offered last and marked") {
        val d = driver()
        d.putCreatureOnBattlefield(d.player1, lumaret.name)
        d.manual(listOf(d.player1))
        d.cast(educator.name)

        val decision = d.pendingDecision.shouldBeInstanceOf<OrderTriggersDecision>()
        decision.triggers.map { it.sourceName } shouldContainExactly listOf(lumaret.name, educator.name)
        decision.triggers.map { it.checksOnResolution } shouldContainExactly listOf(false, true)
    }

    test("with automatic ordering off, the controller is asked and the default order is offered first") {
        val d = driver()
        d.putCreatureOnBattlefield(d.player1, gatherer.name)
        d.manual(listOf(d.player1))
        d.cast(visionary.name)

        val decision = d.pendingDecision.shouldBeInstanceOf<OrderTriggersDecision>()
        decision.playerId shouldBe d.player1
        decision.context.phase shouldBe DecisionPhase.TRIGGER
        // Nothing is on the stack yet: the order comes before the abilities are put there.
        d.state.stack shouldBe emptyList()
        decision.triggers.map { it.sourceName } shouldContainExactly listOf(visionary.name, gatherer.name)
        decision.triggers.map { it.text } shouldContainExactly listOf("Draw a card", "Scry 1")
        decision.triggers[0].triggeringEntityId shouldBe null
        decision.triggers[1].triggeringEntityId shouldBe d.findPermanent(d.player1, visionary.name)

        d.order(listOf(0, 1)).error shouldBe null
        d.pendingDecision shouldBe null
        d.stackTopFirst() shouldContainExactly listOf(visionary.name, gatherer.name)
    }

    test("the chosen order is honoured: the scry can be put to resolve before the draw") {
        val d = driver()
        d.putCreatureOnBattlefield(d.player1, gatherer.name)
        d.manual(listOf(d.player1))
        d.cast(visionary.name)

        d.order(listOf(1, 0)).error shouldBe null
        d.pendingDecision shouldBe null
        d.stackTopFirst() shouldContainExactly listOf(gatherer.name, visionary.name)

        // The scry resolves first and looks at the card the draw is about to take.
        val top = d.state.getLibrary(d.player1).first()
        d.bothPass().error shouldBe null
        d.pendingDecision.shouldBeInstanceOf<SelectCardsDecision>().options shouldContainExactly listOf(top)
    }

    test("copies of one untargeted ability are interchangeable, so no question is asked") {
        val d = driver()
        repeat(2) { d.putCreatureOnBattlefield(d.player1, warden.name) }
        d.manual(listOf(d.player1))
        d.cast(bear.name)
        d.pendingDecision shouldBe null
        d.state.stack.size shouldBe 2
    }

    test("a single trigger is never ordered") {
        val d = driver()
        d.putCreatureOnBattlefield(d.player1, warden.name)
        d.manual(listOf(d.player1))
        d.cast(bear.name)
        d.pendingDecision shouldBe null
        d.state.stack.size shouldBe 1
    }

    test("copies of a targeted ability are asked about, before any target is chosen") {
        val d = driver()
        repeat(2) { d.putCreatureOnBattlefield(d.player1, pinger.name) }
        d.manual(listOf(d.player1))
        d.cast(bear.name)

        val decision = d.pendingDecision.shouldBeInstanceOf<OrderTriggersDecision>()
        decision.triggers.all { it.choosesTargets } shouldBe true
        d.order(listOf(1, 0)).error shouldBe null
        // Targets follow, one ability at a time, as each goes on the stack (CR 603.3d).
        repeat(2) {
            d.pendingDecision.shouldBeInstanceOf<ChooseTargetsDecision>()
            d.submitTargetSelection(d.player1, listOf(d.player2)).error shouldBe null
        }
        d.pendingDecision shouldBe null
        d.state.stack.size shouldBe 2
    }

    test("both players order their own triggers: the active player is asked first, then the other") {
        val d = driver()
        d.putCreatureOnBattlefield(d.player1, gatherer.name)
        d.putCreatureOnBattlefield(d.player1, warden.name)
        d.putCreatureOnBattlefield(d.player2, warden.name)
        d.putCreatureOnBattlefield(d.player2, sentinel.name)
        d.manual(listOf(d.player1, d.player2))
        d.cast(visionary.name)

        val first = d.pendingDecision.shouldBeInstanceOf<OrderTriggersDecision>()
        first.playerId shouldBe d.player1
        first.triggers.map { it.sourceName } shouldContainExactly listOf(visionary.name, warden.name, gatherer.name)
        // The scry first, then Visionary's draw, then the life.
        d.order(listOf(2, 0, 1)).error shouldBe null

        val second = d.pendingDecision.shouldBeInstanceOf<OrderTriggersDecision>()
        second.playerId shouldBe d.player2
        second.triggers.map { it.sourceName } shouldContainExactly listOf(sentinel.name, warden.name)
        d.order(listOf(1, 0)).error shouldBe null

        d.pendingDecision shouldBe null
        // APNAP: the non-active player's abilities go on the stack last, so they resolve first.
        d.stackTopFirst() shouldContainExactly
            listOf(warden.name, sentinel.name, gatherer.name, visionary.name, warden.name)
        d.state.stack.takeLast(2).map { d.state.getEntity(it)!!.get<TriggeredAbilityOnStackComponent>()!!.controllerId } shouldContainExactly
            listOf(d.player2, d.player2)
    }

    test("only the player who turned automatic ordering off is asked") {
        val d = driver()
        d.putCreatureOnBattlefield(d.player1, gatherer.name)
        d.putCreatureOnBattlefield(d.player2, warden.name)
        d.putCreatureOnBattlefield(d.player2, sentinel.name)
        d.manual(listOf(d.player2))
        d.cast(visionary.name)

        d.pendingDecision.shouldBeInstanceOf<OrderTriggersDecision>().playerId shouldBe d.player2
        d.order(listOf(1, 0)).error shouldBe null
        d.pendingDecision shouldBe null
        d.stackTopFirst() shouldContainExactly listOf(warden.name, sentinel.name, visionary.name, gatherer.name)
    }

    test("an answer that isn't a permutation of the abilities is rejected") {
        val d = driver()
        d.putCreatureOnBattlefield(d.player1, gatherer.name)
        d.manual(listOf(d.player1))
        d.cast(visionary.name)

        d.order(listOf(0, 0)).error shouldBe "Order each triggered ability exactly once"
        d.order(listOf(0)).error shouldBe "Order each triggered ability exactly once"
        d.pendingDecision.shouldBeInstanceOf<OrderTriggersDecision>()
        d.order(listOf(1, 0)).error shouldBe null
    }

    test("a game paused on the question survives serialization, setting and waiting wave included") {
        val d = driver()
        d.putCreatureOnBattlefield(d.player1, gatherer.name)
        d.manual(listOf(d.player1))
        d.cast(visionary.name)
        d.pendingDecision.shouldBeInstanceOf<OrderTriggersDecision>()

        val json = Json { serializersModule = engineSerializersModule; allowStructuredMapKeys = true; encodeDefaults = true }
        val restored = json.decodeFromString(GameState.serializer(), json.encodeToString(GameState.serializer(), d.state))
        restored.ordersOwnTriggers(d.player1) shouldBe true
        restored.ordersOwnTriggers(d.player2) shouldBe false
        d.replaceState(restored)

        d.order(listOf(1, 0)).error shouldBe null
        d.stackTopFirst() shouldContainExactly listOf(gatherer.name, visionary.name)
    }
})
