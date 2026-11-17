package com.wingedsheep.engine.mechanics.combat

import com.wingedsheep.engine.core.*
import com.wingedsheep.engine.handlers.EffectContext
import com.wingedsheep.engine.handlers.effects.combat.RandomizedBlockerPilesExecutor
import com.wingedsheep.engine.handlers.actions.decision.DecisionValidators
import com.wingedsheep.engine.state.components.combat.*
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.GameRng
import com.wingedsheep.sdk.scripting.effects.RandomizedBlockerPilesEffect
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldNotBeNull

class RandomizedBlockerPilesTest : ScenarioTestBase() {
    init {
        fun board(tapped: Boolean = false) = scenario().withPlayers()
            .withCardOnBattlefield(1, "Grizzly Bears").withCardOnBattlefield(1, "Savannah Lions")
            .withCardOnBattlefield(2, "Wall of Wood", tapped = tapped)
            .withCardInLibrary(1, "Forest").withCardInLibrary(2, "Forest")
            .withActivePlayer(1).inPhase(Phase.COMBAT, Step.DECLARE_ATTACKERS).build()
        fun enable(game: TestGame) {
            val result = RandomizedBlockerPilesExecutor().execute(game.state, RandomizedBlockerPilesEffect(), EffectContext(sourceId = null, controllerId = game.player1Id))
            result.events.filterIsInstance<BlockerDeclarationPolicyChangedEvent>().size shouldBe 1
            game.state = result.state
            game.declareAttackers(mapOf("Grizzly Bears" to 2, "Savannah Lions" to 2)).error shouldBe null
            game.passUntilPhase(Phase.COMBAT, Step.DECLARE_BLOCKERS)
            game.execute(DeclareBlockers(game.player2Id, emptyMap())).error shouldBe null
        }
        fun submit(game: TestGame, piles: List<List<com.wingedsheep.sdk.model.EntityId>>) = game.execute(
            SubmitDecision(game.player2Id, PilesSplitResponse(game.state.pendingDecision!!.id, piles)))

        test("invalid membership preserves the unanswered question and RNG") {
            val game = board()
            enable(game)
            val wall = game.findPermanent("Wall of Wood")!!
            val before = game.state
            submit(game, listOf(listOf(wall), listOf(wall))).error.shouldNotBeNull()
            game.state shouldBe before
            submit(game, listOf(listOf(wall, wall), emptyList())).error.shouldNotBeNull()
            submit(game, listOf(listOf(game.findPermanent("Grizzly Bears")!!), emptyList())).error.shouldNotBeNull()
            submit(game, listOf(listOf(wall))).error.shouldNotBeNull()
        }
        test("random assignment is reproducible and does not mutate the input state") {
            val game = board()
            enable(game)
            game.state = game.state.copy(rng = GameRng.seeded(42))
            val before = game.state
            val wall = game.findPermanent("Wall of Wood")!!
            val first = submit(game, listOf(listOf(wall), emptyList()))
            first.error shouldBe null
            val after = game.state
            game.state = before
            submit(game, listOf(listOf(wall), emptyList())).error shouldBe null
            game.state shouldBe after
            before.getEntity(wall)?.get<BlockingComponent>() shouldBe null
            first.events.filterIsInstance<BlockersDeclaredEvent>().size shouldBe 1
            game.state.getEntity(wall)?.has<BlockedThisTurnComponent>() shouldBe true
        }
        test("chosen tapped creatures do not block") {
            val game = board(tapped = true)
            enable(game)
            val wall = game.findPermanent("Wall of Wood")!!
            submit(game, listOf(listOf(wall), emptyList())).error shouldBe null
            game.state.getEntity(wall)?.get<BlockingComponent>() shouldBe null
            game.state.getEntity(game.player2Id)?.has<BlockersDeclaredThisCombatComponent>() shouldBe true
        }
        test("membership limits permit additional piles only for authorized cards") {
            val a = com.wingedsheep.sdk.model.EntityId.of("a")
            val b = com.wingedsheep.sdk.model.EntityId.of("b")
            val decision = SplitPilesDecision("piles", a, "Piles", DecisionContext(phase = DecisionPhase.COMBAT),
                listOf(a, b), 3, allowUnassigned = true, maxPileMemberships = mapOf(a to 2))
            DecisionValidators.validate(decision, PilesSplitResponse("piles", listOf(listOf(a), listOf(a), emptyList()))) shouldBe null
            DecisionValidators.validate(decision, PilesSplitResponse("piles", listOf(listOf(a), listOf(a), listOf(a)))).shouldNotBeNull()
            DecisionValidators.validate(decision, PilesSplitResponse("piles", listOf(listOf(b), listOf(b), emptyList()))).shouldNotBeNull()
        }

        fun pileBoard(attackers: List<String>, blockers: List<String>): TestGame {
            val builder = scenario().withPlayers().withActivePlayer(1).withPriorityPlayer(2)
                .inPhase(Phase.COMBAT, Step.DECLARE_BLOCKERS)
            for (name in attackers) builder.withCardOnBattlefield(1, name)
            for (name in blockers) builder.withCardOnBattlefield(2, name)
            val game = builder.withCardInLibrary(1, "Forest").withCardInLibrary(2, "Forest").build()
            game.state = RandomizedBlockerPilesExecutor().execute(game.state, RandomizedBlockerPilesEffect(),
                EffectContext(sourceId = null, controllerId = game.player1Id)).state
            for (name in attackers) game.state = game.state.updateEntity(game.findPermanent(name)!!) { it.with(AttackingComponent(game.player2Id)) }
            return game
        }
        fun open(game: TestGame) {
            game.execute(DeclareBlockers(game.player2Id, emptyMap())).error shouldBe null
        }
        val menace = com.wingedsheep.sdk.dsl.card("Pile Menace") {
            typeLine = "Creature — Beast"; power = 3; toughness = 3
            keywords(com.wingedsheep.sdk.core.Keyword.MENACE)
        }
        val limited = com.wingedsheep.sdk.dsl.card("Pile Limited") {
            typeLine = "Creature — Beast"; power = 3; toughness = 3
            staticAbility { ability = com.wingedsheep.sdk.scripting.CantBeBlockedByMoreThan(1) }
        }
        val extra = com.wingedsheep.sdk.dsl.card("Pile Extra") {
            typeLine = "Creature — Beast"; power = 1; toughness = 5
            staticAbility { ability = com.wingedsheep.sdk.scripting.CanBlockAdditionalForCreatureGroup(
                1, com.wingedsheep.sdk.scripting.filters.unified.GroupFilter.source()) }
        }
        cardRegistry.register(menace)
        cardRegistry.register(limited)
        cardRegistry.register(extra)

        test("evasion filters out ground blockers but retains a legal reach blocker") {
            val game = pileBoard(listOf("Air Elemental"), listOf("Wall of Wood", "Giant Spider"))
            open(game)
            val wall = game.findPermanent("Wall of Wood")!!
            val spider = game.findPermanent("Giant Spider")!!
            submit(game, listOf(listOf(wall, spider))).error shouldBe null
            game.state.getEntity(wall)?.get<BlockingComponent>() shouldBe null
            game.state.getEntity(spider)?.get<BlockingComponent>()?.blockedAttackerIds shouldBe listOf(game.findPermanent("Air Elemental")!!)
        }
        test("menace needs two surviving eligible blockers") {
            val one = pileBoard(listOf("Pile Menace"), listOf("Wall of Wood"))
            open(one)
            val wall = one.findPermanent("Wall of Wood")!!
            submit(one, listOf(listOf(wall))).error shouldBe null
            one.state.getEntity(wall)?.get<BlockingComponent>() shouldBe null
            val two = pileBoard(listOf("Pile Menace"), listOf("Wall of Wood", "Giant Spider"))
            open(two)
            submit(two, listOf(listOf(two.findPermanent("Wall of Wood")!!, two.findPermanent("Giant Spider")!!))).error shouldBe null
            two.state.getEntity(two.findPermanent("Pile Menace")!!)?.get<BlockedComponent>()?.blockerIds?.size shouldBe 2
        }
        test("oversubscribed maximum blockers asks for a legal subset without rerolling") {
            val game = pileBoard(listOf("Pile Limited"), listOf("Wall of Wood", "Giant Spider"))
            open(game)
            val wall = game.findPermanent("Wall of Wood")!!
            val spider = game.findPermanent("Giant Spider")!!
            submit(game, listOf(listOf(wall, spider))).error shouldBe null
            val choice = game.state.pendingDecision as SplitPilesDecision
            choice.requiredAssignments shouldBe 1
            val rng = game.state.rng
            game.execute(SubmitDecision(game.player2Id, PilesSplitResponse(choice.id, listOf(listOf(wall))))).error shouldBe null
            game.state.rng shouldBe rng
            game.state.getEntity(game.findPermanent("Pile Limited")!!)?.get<BlockedComponent>()?.blockerIds?.size shouldBe 1
        }
        test("additional and unlimited blocking capacities permit distinct pile memberships") {
            val game = pileBoard(listOf("Grizzly Bears", "Savannah Lions"), listOf("Pile Extra", "Wall of Glare"))
            open(game)
            val question = game.state.pendingDecision as SplitPilesDecision
            val extraId = game.findPermanent("Pile Extra")!!
            val wall = game.findPermanent("Wall of Glare")!!
            question.maxPileMemberships[extraId] shouldBe 2
            question.maxPileMemberships[wall] shouldBe 2
            submit(game, listOf(listOf(extraId, wall), listOf(extraId, wall))).error shouldBe null
            game.state.getEntity(extraId)?.get<BlockingComponent>()?.blockedAttackerIds?.size shouldBe 2
            game.state.getEntity(wall)?.get<BlockingComponent>()?.blockedAttackerIds?.size shouldBe 2
        }
        test("blocking a band member expands the resulting block to the entire band") {
            val game = pileBoard(listOf("Grizzly Bears", "Savannah Lions"), listOf("Wall of Wood"))
            for (name in listOf("Grizzly Bears", "Savannah Lions")) {
                game.state = game.state.updateEntity(game.findPermanent(name)!!) { it.with(AttackingComponent(game.player2Id, "band")) }
            }
            open(game)
            val wall = game.findPermanent("Wall of Wood")!!
            submit(game, listOf(listOf(wall), emptyList())).error shouldBe null
            game.state.getEntity(wall)?.get<BlockingComponent>()?.blockedAttackerIds?.size shouldBe 2
        }
        test("durations expire at the corresponding boundary and this-turn policies survive extra combats") {
            val game = board()
            fun stateFor(duration: com.wingedsheep.sdk.scripting.Duration) = RandomizedBlockerPilesExecutor().execute(
                game.state, RandomizedBlockerPilesEffect(duration), EffectContext(sourceId = null, controllerId = game.player1Id)).state
            val turn = stateFor(com.wingedsheep.sdk.scripting.Duration.EndOfTurn)
            RandomizedBlockerPiles.isActive(services.combatManager.endCombat(turn).state) shouldBe true
            RandomizedBlockerPiles.isActive(services.turnManager.cleanupPhaseManager.cleanupEndOfTurn(turn)) shouldBe false
            val combat = stateFor(com.wingedsheep.sdk.scripting.Duration.EndOfCombat)
            RandomizedBlockerPiles.isActive(services.combatManager.endCombat(combat).state) shouldBe false
            val permanent = stateFor(com.wingedsheep.sdk.scripting.Duration.Permanent)
            RandomizedBlockerPiles.isActive(services.turnManager.cleanupPhaseManager.cleanupEndOfTurn(permanent)) shouldBe true
        }
        test("a restored pile suspension retains the same random result and memberships") {
            val game = board()
            enable(game)
            val json = kotlinx.serialization.json.Json { serializersModule = engineSerializersModule; allowStructuredMapKeys = true }
            val encoded = json.encodeToString(com.wingedsheep.engine.state.GameState.serializer(), game.state)
            val restored = json.decodeFromString(com.wingedsheep.engine.state.GameState.serializer(), encoded)
            restored shouldBe game.state
            val before = game.state
            val wall = game.findPermanent("Wall of Wood")!!
            submit(game, listOf(listOf(wall), emptyList())).error shouldBe null
            val expected = game.state
            game.state = restored
            submit(game, listOf(listOf(wall), emptyList())).error shouldBe null
            game.state shouldBe expected
            before.pendingDecision.shouldNotBeNull()
        }
        test("each defender receives only their own attacking slice and no intervening priority") {
            val game = pileBoard(listOf("Grizzly Bears", "Savannah Lions"), listOf("Wall of Wood", "Giant Spider"))
            val third = com.wingedsheep.sdk.model.EntityId.of("third")
            val spider = game.findPermanent("Giant Spider")!!
            game.state = game.state.copy(entities = game.state.entities + (third to game.state.getEntity(game.player2Id)!!),
                turnOrder = game.state.turnOrder + third)
                .updateEntity(spider) { it.with(com.wingedsheep.engine.state.components.identity.ControllerComponent(third)) }
                .updateEntity(game.findPermanent("Savannah Lions")!!) { it.with(AttackingComponent(third)) }
            open(game)
            val first = game.state.pendingDecision as SplitPilesDecision
            first.numberOfPiles shouldBe 1
            first.cards shouldBe listOf(game.findPermanent("Wall of Wood")!!)
            submit(game, listOf(emptyList())).error shouldBe null
            game.state.priorityPlayerId shouldBe third
            game.execute(DeclareBlockers(third, emptyMap())).error shouldBe null
            val second = game.state.pendingDecision as SplitPilesDecision
            second.cards shouldBe listOf(spider)
            game.execute(SubmitDecision(third, PilesSplitResponse(second.id, listOf(listOf(spider))))).error shouldBe null
            game.state.priorityPlayerId shouldBe game.player1Id
            game.state.getEntity(spider)?.get<BlockingComponent>()?.blockedAttackerIds shouldBe listOf(game.findPermanent("Savannah Lions")!!)
        }

        test("granted unlimited blocking capacity is honored both before and after random assignment") {
            val game = pileBoard(listOf("Grizzly Bears", "Savannah Lions"), listOf("Wall of Wood"))
            val wall = game.findPermanent("Wall of Wood")!!
            game.state = game.state.updateEntity(wall) { it.with(com.wingedsheep.engine.state.components.identity.FaceDownComponent) }
            game.state = game.state.copy(grantedStaticAbilities = listOf(com.wingedsheep.engine.event.GrantedStaticAbility(
                entityId = wall, ability = com.wingedsheep.sdk.scripting.CanBlockAnyNumber(), duration = com.wingedsheep.sdk.scripting.Duration.EndOfTurn)))
            open(game)
            (game.state.pendingDecision as SplitPilesDecision).maxPileMemberships[wall] shouldBe 2
            submit(game, listOf(listOf(wall), listOf(wall))).error shouldBe null
            game.state.getEntity(wall)?.get<BlockingComponent>()?.blockedAttackerIds?.size shouldBe 2
        }
        test("a large capped pile asks for one selected blocker rather than listing every outcome") {
            val game = pileBoard(listOf("Pile Limited"), List(30) { "Wall of Wood" })
            open(game)
            val walls = (game.state.pendingDecision as SplitPilesDecision).cards
            walls.size shouldBe 30
            submit(game, listOf(walls)).error shouldBe null
            val question = game.state.pendingDecision as SplitPilesDecision
            question.requiredAssignments shouldBe 1
            question.suggestedPiles!!.flatten().size shouldBe 1
            submit(game, listOf(listOf(walls.last()))).error shouldBe null
            game.state.getEntity(game.findPermanent("Pile Limited")!!)?.get<BlockedComponent>()?.blockerIds shouldBe listOf(walls.last())
        }

        test("a defender with no creatures declares no blocks without being asked") {
            val game = pileBoard(listOf("Grizzly Bears"), emptyList())
            open(game)
            game.state.pendingDecision shouldBe null
            game.state.getEntity(game.player2Id)?.has<BlockersDeclaredThisCombatComponent>() shouldBe true
        }

        test("many separately capped piles are trimmed per attacker without a subset search") {
            val attackers = List(20) { index ->
                com.wingedsheep.sdk.dsl.card("Pile Capped $index") {
                    typeLine = "Creature — Beast"; power = 3; toughness = 3
                    staticAbility { ability = com.wingedsheep.sdk.scripting.CantBeBlockedByMoreThan(1) }
                }.also { cardRegistry.register(it) }.name
            }
            val game = pileBoard(attackers, List(40) { "Wall of Wood" })
            open(game)
            val blockers = (game.state.pendingDecision as SplitPilesDecision).cards
            submit(game, blockers.chunked(2)).error shouldBe null
            val question = game.state.pendingDecision as SplitPilesDecision
            question.requiredAssignments shouldBe 20
            question.suggestedPiles!!.map { it.size } shouldBe List(20) { 1 }
            submit(game, question.suggestedPiles).error shouldBe null
            attackers.forEach { name ->
                game.state.getEntity(game.findPermanent(name)!!)?.get<BlockedComponent>()?.blockerIds?.size shouldBe 1
            }
        }

        test("a large pile without its required co-blocker is eliminated before the subset search") {
            val dependent = com.wingedsheep.sdk.dsl.card("Pile Dependent") {
                typeLine = "Creature — Beast"; power = 1; toughness = 3
                staticAbility { ability = com.wingedsheep.sdk.scripting.CantBlockUnlessCoBlocker(
                    coBlockerFilter = com.wingedsheep.sdk.scripting.GameObjectFilter.Creature.withSubtype("Elf")) }
            }
            cardRegistry.register(dependent)
            val game = pileBoard(listOf("Grizzly Bears"), List(30) { "Pile Dependent" })
            open(game)
            val blockers = (game.state.pendingDecision as SplitPilesDecision).cards
            blockers.size shouldBe 30
            submit(game, listOf(blockers)).error shouldBe null
            game.state.pendingDecision shouldBe null
            blockers.forEach { game.state.getEntity(it)?.get<BlockingComponent>() shouldBe null }
            game.state.getEntity(game.player2Id)?.has<BlockersDeclaredThisCombatComponent>() shouldBe true
        }

        test("post-assignment labels do not reveal a face-down attacker's identity") {
            val limit = com.wingedsheep.sdk.dsl.card("Pile Global Limit") {
                typeLine = "Enchantment"
                staticAbility { ability = com.wingedsheep.sdk.scripting.BlockerCountLimit(1) }
            }
            cardRegistry.register(limit)
            val game = pileBoard(listOf("Grizzly Bears"), listOf("Wall of Wood", "Giant Spider", "Pile Global Limit"))
            val attacker = game.findPermanent("Grizzly Bears")!!
            game.state = game.state.updateEntity(attacker) { it.with(com.wingedsheep.engine.state.components.identity.FaceDownComponent) }
            open(game)
            val options = (game.state.pendingDecision as SplitPilesDecision).cards
            submit(game, listOf(options)).error shouldBe null
            (game.state.pendingDecision as SplitPilesDecision).pileLabels shouldBe listOf("Face-down creature")
        }
    }
}
