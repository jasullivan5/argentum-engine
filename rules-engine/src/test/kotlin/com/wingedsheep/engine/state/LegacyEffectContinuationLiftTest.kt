package com.wingedsheep.engine.state

import com.wingedsheep.engine.core.ContinuationFrame
import com.wingedsheep.engine.core.CursorStep
import com.wingedsheep.engine.core.EffectProgram
import com.wingedsheep.engine.core.ProgramCursor
import com.wingedsheep.engine.core.engineSerializersModule
import com.wingedsheep.engine.handlers.ContinuationHandler
import com.wingedsheep.engine.handlers.EffectContext
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.scripting.effects.CompositeEffect
import com.wingedsheep.sdk.scripting.effects.Effect
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

/**
 * Games persisted while a composite's remaining steps were an `EffectContinuation` ("run these
 * next") must still resume. [LegacyEffectContinuationLift] turns each into an [EffectProgram] over
 * `CompositeEffect(remainingEffects)` with the cursor at `Start`, so resuming runs the
 * remaining effects from the first, and leaves current JSON untouched.
 */
class LegacyEffectContinuationLiftTest : FunSpec({

    val json = Json {
        serializersModule = engineSerializersModule
        allowStructuredMapKeys = true
        encodeDefaults = false
    }

    fun encodeEffects(effects: List<Effect>): JsonElement =
        json.encodeToJsonElement(ListSerializer(Effect.serializer()), effects)

    fun oldFrame(effects: List<Effect>, context: EffectContext) = JsonObject(mapOf(
        "type" to JsonPrimitive("com.wingedsheep.engine.core.EffectContinuation"),
        "remainingEffects" to encodeEffects(effects),
        "effectContext" to json.encodeToJsonElement(EffectContext.serializer(), context),
    ))

    fun setup(): GameTestDriver = GameTestDriver().apply {
        registerCards(TestCards.all)
        initMirrorMatch(Deck.of("Forest" to 40), startingPlayer = 0)
        passPriorityUntil(Step.PRECOMBAT_MAIN)
    }

    /** [state] encoded, with its continuation stack replaced by [frames] (raw JSON). */
    fun withStack(state: GameState, vararg frames: JsonElement): JsonObject {
        val encoded = json.parseToJsonElement(json.encodeToString(state)).jsonObject
        return JsonObject(encoded + ("continuationStack" to JsonArray(frames.toList())))
    }

    test("an old frame decodes to a program before the first of its remaining effects") {
        val d = setup()
        val player = d.activePlayer!!
        val context = EffectContext(sourceId = null, controllerId = player)
        val remaining = listOf(Effects.GainLife(2), Effects.GainLife(3))

        val decoded = json.decodeFromString<GameState>(withStack(d.state, oldFrame(remaining, context)).toString())

        decoded.continuationStack shouldBe listOf<ContinuationFrame>(
            EffectProgram(
                root = CompositeEffect(remaining),
                origin = null,
                cursor = ProgramCursor(listOf(CursorStep.Start)),
                effectContext = context,
            )
        )
    }

    test("a converted frame resumes its remaining effects from the first, in order") {
        val d = setup()
        val player = d.activePlayer!!
        val lifeBefore = d.getLifeTotal(player)
        val context = EffectContext(sourceId = null, controllerId = player)
        val old = withStack(d.state, oldFrame(listOf(Effects.GainLife(2), Effects.GainLife(3)), context))

        val decoded = json.decodeFromString<GameState>(old.toString())
        val drained = ContinuationHandler(d.services).drainAutomatic(decoded, emptyList())

        drained.state.continuationStack.shouldBeEmpty()
        drained.state.getEntity(player)!!
            .get<com.wingedsheep.engine.state.components.identity.LifeTotalComponent>()!!.life shouldBe lifeBefore + 5
    }

    test("an old frame nested inside another object is converted too") {
        val context = EffectContext(sourceId = null, controllerId = com.wingedsheep.sdk.model.EntityId("player-1"))
        val frame = oldFrame(listOf(Effects.GainLife(1)), context)
        val holder = JsonObject(mapOf("type" to JsonPrimitive("Holder"), "inner" to JsonArray(listOf(frame))))

        val lifted = LegacyEffectContinuationLift.lift(holder).jsonObject
        val converted = (lifted.getValue("inner") as JsonArray).single()
        json.decodeFromJsonElement(ContinuationFrame.serializer(), converted) shouldBe EffectProgram(
            CompositeEffect(listOf(Effects.GainLife(1))), null, ProgramCursor.START, context
        )
    }

    test("the pre-suspension format's frame ends up converted too") {
        // legacy-suspensions/nested-may predates suspensions (root `pendingDecision`); its
        // `EffectContinuation` goes through LegacyGameStateSerializer and then this lift.
        val text = checkNotNull(javaClass.getResource("/legacy-suspensions/nested-may/state.json")).readText()
        text.contains("com.wingedsheep.engine.core.EffectContinuation") shouldBe true
        val decoded = json.decodeFromString<GameState>(text)
        val program = decoded.continuationStack.filterIsInstance<EffectProgram>().single()
        program.root shouldBe CompositeEffect(listOf(Effects.GainLife(3)))
        program.origin shouldBe null
        program.cursor shouldBe ProgramCursor.START
    }

    test("a current-shape document passes through the lift untouched") {
        val d = setup()
        val context = EffectContext(sourceId = null, controllerId = d.activePlayer!!)
        val current = d.state.pushContinuation(EffectProgram.queued(listOf(Effects.GainLife(1)), context, origin = com.wingedsheep.engine.core.ProgramOrigin.BuiltBy("Test", null)))
        val encoded = json.parseToJsonElement(json.encodeToString(current))
        LegacyEffectContinuationLift.lift(encoded) shouldBeSameInstanceAs encoded
        json.decodeFromString<GameState>(encoded.toString()) shouldBe current
    }
})
