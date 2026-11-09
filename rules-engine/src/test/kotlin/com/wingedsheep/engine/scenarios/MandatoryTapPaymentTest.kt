package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.ActivateAbility
import com.wingedsheep.engine.core.ExecutionResult
import com.wingedsheep.engine.core.Outcome
import com.wingedsheep.engine.core.Rejection
import com.wingedsheep.engine.core.TappedEvent
import com.wingedsheep.engine.core.tap
import com.wingedsheep.engine.handlers.CostHandler
import com.wingedsheep.engine.mechanics.mana.ManaPool
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.components.battlefield.TappedComponent
import com.wingedsheep.engine.state.components.player.ManaPoolComponent
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.engine.core.PaymentStrategy
import com.wingedsheep.sdk.dsl.Costs
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.EntityId
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Source {T} is a mandatory payment, not an instruction that may do nothing (CR 107.5,
 * 118.3 and 601.2h via 602.2b, verified against the retained November 6, 2026 rules).
 * Engine-level controls use several production definitions, not card-specific payment logic.
 */
class MandatoryTapPaymentTest : ScenarioTestBase() {
    private data class Subject(val card: String, val ability: Int, val mana: Int, val sacrifice: Boolean)

    private val subjects = listOf(
        Subject("Mind Stone", 0, 0, false),
        Subject("Evolving Wilds", 0, 0, true),
        Subject("Mind Stone", 1, 1, true),
        // Held out from the application's original payment examination.
        Subject("Hedron Archive", 0, 0, false),
        Subject("Hedron Archive", 1, 2, true),
    )

    private val nested = card("Nested mandatory tap control") {
        typeLine = "Artifact"
        activatedAbility {
            cost = Costs.Composite(Costs.Mana("{1}"), Costs.Composite(Costs.Tap, Costs.SacrificeSelf))
            effect = Effects.DrawCards(1)
        }
    }

    init {
        cardRegistry.register(nested)
        for (subject in subjects) {
            for ((strategyName, strategy) in listOf("AutoPay" to PaymentStrategy.AutoPay,
                "Explicit" to PaymentStrategy.Explicit(emptyList()))) {
                test("${subject.card} ability ${subject.ability}: tapped $strategyName refuses atomically") {
                    val game = scenario().withPlayers().withCardOnBattlefield(1, subject.card, tapped = true).build()
                    val source = game.findPermanent(subject.card)!!
                    game.state = game.state.updateEntity(game.player1Id) { it.with(ManaPoolComponent(colorless = subject.mana)) }
                    val action = activation(game, subject, source, strategy)
                    val result = actionProcessor.process(game.state, action).result
                    trace(game.state, action, result)
                    result.outcome.shouldBeInstanceOf<Outcome.Rejected>().reason.shouldBeInstanceOf<Rejection.IllegalAction>()
                    result.state shouldBeSameInstanceAs game.state
                    result.events.shouldBeEmpty()
                }

                test("${subject.card} ability ${subject.ability}: untapped $strategyName pays every component") {
                    val game = scenario().withPlayers().withCardOnBattlefield(1, subject.card).build()
                    val source = game.findPermanent(subject.card)!!
                    game.state = game.state.updateEntity(game.player1Id) { it.with(ManaPoolComponent(colorless = subject.mana)) }
                    val action = activation(game, subject, source, strategy)
                    val result = actionProcessor.process(game.state, action).result
                    trace(game.state, action, result)
                    result.outcome.shouldBeInstanceOf<Outcome.Done>()
                    result.events.filterIsInstance<TappedEvent>().count { it.entityId == source } shouldBe 1
                    if (subject.sacrifice) {
                        (source in result.state.getGraveyard(game.player1Id)) shouldBe true
                        (source in result.state.getBattlefield()) shouldBe false
                        result.state.stack.size shouldBe 1
                    } else {
                        result.state.getEntity(source)!!.has<TappedComponent>() shouldBe true
                        result.state.stack.shouldBeEmpty()
                        result.state.getEntity(game.player1Id)!!.get<ManaPoolComponent>()!!.colorless shouldBe
                            if (subject.card == "Mind Stone") 1 else 2
                    }
                    if (subject.mana > 0) result.state.getEntity(game.player1Id)!!.get<ManaPoolComponent>()!!.colorless shouldBe 0
                }
            }
        }

        for (tapped in listOf(false, true)) test("explicit selected Forest and composite source tap: tapped=$tapped") {
            val game = scenario().withPlayers().withCardOnBattlefield(1, "Mind Stone", tapped = tapped)
                .withCardOnBattlefield(1, "Forest").build()
            val source = game.findPermanent("Mind Stone")!!
            val forest = game.findPermanent("Forest")!!
            val action = activation(game, subjects[2], source, PaymentStrategy.Explicit(listOf(forest)))
            val result = actionProcessor.process(game.state, action).result
            trace(game.state, action, result)
            if (tapped) {
                result.outcome.shouldBeInstanceOf<Outcome.Rejected>().reason.shouldBeInstanceOf<Rejection.IllegalAction>()
                result.state shouldBeSameInstanceAs game.state
                result.events.shouldBeEmpty()
            } else {
                result.outcome.shouldBeInstanceOf<Outcome.Done>()
                result.events.filterIsInstance<TappedEvent>().map { it.entityId }.toSet() shouldBe setOf(forest, source)
                (source in result.state.getGraveyard(game.player1Id)) shouldBe true
                result.state.getEntity(game.player1Id)!!.get<ManaPoolComponent>()!!.total shouldBe 0
            }
        }

        test("a source cannot pay its draw mana by also paying its own mandatory tap") {
            val game = scenario().withPlayers().withCardOnBattlefield(1, "Mind Stone").build()
            val source = game.findPermanent("Mind Stone")!!
            val action = activation(game, subjects[2], source, PaymentStrategy.Explicit(listOf(source)))
            val result = actionProcessor.process(game.state, action).result
            result.outcome.shouldBeInstanceOf<Outcome.Rejected>()
            result.state shouldBeSameInstanceAs game.state
            result.events.shouldBeEmpty()
        }

        test("a nested composite still refuses its tapped source before spending floating mana") {
            val game = scenario().withPlayers().withCardOnBattlefield(1, nested.name, tapped = true).build()
            game.state = game.state.updateEntity(game.player1Id) { it.with(ManaPoolComponent(colorless = 1)) }
            val action = ActivateAbility(game.player1Id, game.findPermanent(nested.name)!!,
                nested.script.activatedAbilities.single().id, paymentStrategy = PaymentStrategy.Explicit(emptyList()))
            val result = actionProcessor.process(game.state, action).result
            result.outcome.shouldBeInstanceOf<Outcome.Rejected>().reason.shouldBeInstanceOf<Rejection.IllegalAction>()
            result.state shouldBeSameInstanceAs game.state
            result.events.shouldBeEmpty()
        }

        test("a later impossible tap rolls back earlier mana production and the first tap") {
            val impossible = card("Repeated source tap control") {
                typeLine = "Artifact"
                activatedAbility {
                    cost = Costs.Composite(Costs.Mana("{1}"), Costs.Tap, Costs.Tap, Costs.SacrificeSelf)
                    effect = Effects.DrawCards(1)
                }
            }
            cardRegistry.register(impossible)
            val game = scenario().withPlayers().withCardOnBattlefield(1, impossible.name)
                .withCardOnBattlefield(1, "Forest").build()
            val source = game.findPermanent(impossible.name)!!
            val action = ActivateAbility(game.player1Id, source, impossible.script.activatedAbilities.single().id,
                paymentStrategy = PaymentStrategy.Explicit(listOf(game.findPermanent("Forest")!!)))
            val result = actionProcessor.process(game.state, action).result
            trace(game.state, action, result)
            result.outcome.shouldBeInstanceOf<Outcome.Rejected>()
            result.state shouldBeSameInstanceAs game.state
            result.events.shouldBeEmpty()
        }

        test("direct cost payment refuses an already tapped source but the effect tap atom remains a no-op") {
            val game = scenario().withPlayers().withCardOnBattlefield(1, "Mind Stone", tapped = true).build()
            val source = game.findPermanent("Mind Stone")!!
            val payment = CostHandler(zones).payAbilityCost(game.state, Costs.Tap, source, game.player1Id, ManaPool())
            payment.success shouldBe false
            payment.events.shouldBeEmpty()
            val effectTap = tap(game.state, source)
            effectTap.state shouldBeSameInstanceAs game.state
            effectTap.tapped shouldBe null
        }
    }

    private fun activation(game: TestGame, subject: Subject, source: EntityId, strategy: PaymentStrategy) =
        ActivateAbility(game.player1Id, source, cardRegistry.getCard(subject.card)!!.script.activatedAbilities[subject.ability].id,
            paymentStrategy = strategy)

    private fun trace(before: GameState, action: ActivateAbility, result: ExecutionResult) {
        println("TAP_PAYMENT_TRACE " + buildJsonObject {
            put("action", action.toString())
            put("outcome", result.outcome.toString())
            put("error", result.error)
            put("sameStateInstance", before === result.state)
            put("beforeMana", before.getEntity(action.playerId)?.get<ManaPoolComponent>().toString())
            put("afterMana", result.state.getEntity(action.playerId)?.get<ManaPoolComponent>().toString())
            put("beforeBattlefield", before.getBattlefield().toString())
            put("afterBattlefield", result.state.getBattlefield().toString())
            put("beforeGraveyard", before.getGraveyard(action.playerId).toString())
            put("afterGraveyard", result.state.getGraveyard(action.playerId).toString())
            put("beforeStack", before.stack.toString())
            put("afterStack", result.state.stack.toString())
            put("events", result.events.toString())
        })
    }
}
