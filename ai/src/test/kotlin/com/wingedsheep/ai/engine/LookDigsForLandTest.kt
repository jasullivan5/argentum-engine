package com.wingedsheep.ai.engine

import com.wingedsheep.ai.engine.knowledge.IntentCatalog
import com.wingedsheep.engine.core.CardsSelectedResponse
import com.wingedsheep.engine.core.DecisionContext
import com.wingedsheep.engine.core.DecisionPhase
import com.wingedsheep.engine.core.SelectCardsDecision
import com.wingedsheep.engine.state.ZoneKey
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.mtg.sets.MtgSetCatalog
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.model.EntityId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * [AiProfile.lookDigsForLand], from the 2026-10-31 AI-vs-humans review: game `e8c9a92d` turn 6,
 * Eye of Jace's upkeep surveil 1 with two lands in play, none in hand and six spells waiting. The
 * AI kept Theoretical Necromancer on top; the third land came on turn 10.
 *
 * The library is the logged one in shape: 31 cards, about half of them lands.
 */
class LookDigsForLandTest : FunSpec({

    val fra = MtgSetCatalog.requireByCode("FRA")
    val allCards = (fra.cards + fra.basicLands).distinctBy { it.name }

    val deck = Deck.of(
        "Swamp" to 8, "Island" to 6, "Room of Refuge" to 1,
        "Recursive Recruitment" to 1, "Mindseeker Oculus" to 2, "Twinned Vision" to 1,
        "Surveillance Phantasm" to 2, "Murmuring Volume" to 2, "Traxos, Academy Guardian" to 1,
        "Infinite Coursework" to 1, "Rank Rat" to 2, "Screeching Soulbreaker" to 1,
        "Apex Witchstalker" to 1, "Theoretical Necromancer" to 1,
    )

    /** The AI's turn with [lands] out, [hand] in hand and the rest of [deck] as the library. */
    fun game(lands: List<String>, hand: List<String>): Pair<GameTestDriver, EntityId> {
        val d = GameTestDriver().apply {
            registerCards(allCards)
            initMirrorMatch(deck)
            passPriorityUntil(Step.PRECOMBAT_MAIN)
        }
        val me = d.activePlayer!!
        var s = d.state
        for (id in s.getZone(me, Zone.HAND)) {
            s = s.moveToZone(id, ZoneKey(me, Zone.HAND), ZoneKey(me, Zone.LIBRARY))
        }
        d.replaceState(s)
        lands.forEach { d.putLandOnBattlefield(me, it) }
        d.putPermanentOnBattlefield(me, "Eye of Jace")
        hand.forEach { d.putCardInHand(me, it) }
        return d to me
    }

    fun surveil(d: GameTestDriver, me: EntityId, top: EntityId, flag: Boolean): List<EntityId> {
        val decision = SelectCardsDecision(
            id = "test",
            playerId = me,
            prompt = "Surveil 1: choose cards to put into your graveyard",
            context = DecisionContext(phase = DecisionPhase.RESOLUTION),
            options = listOf(top),
            minSelections = 0,
            maxSelections = 1,
            selectedLabel = "Put in graveyard",
            remainderLabel = "Put on top",
        )
        val responder = DecisionResponder(
            GameSimulator(d.cardRegistry),
            AIPlayer.defaultEvaluator(),
            intents = IntentCatalog.of(d.cardRegistry),
            castabilityAwareCardSelection = true,
            selectionCountsByValue = true,
            lookDigsForLand = flag,
        )
        return (responder.respond(d.state, decision, me) as CardsSelectedResponse).selectedCards
    }

    val loggedHand = listOf(
        "Way of the Mind Sculptor", "Theorix Charm", "Medic's Kitesail",
        "Void Extrapolator", "Theorix Metamage", "Theorix Charm",
    )

    test("e8c9 T6: two lands, none in hand, six spells — the surveiled three-drop goes to dig for a land") {
        val (d, me) = game(listOf("Island", "Swamp"), loggedHand)
        val necromancer = d.putCardOnTopOfLibrary(me, "Theoretical Necromancer")

        surveil(d, me, necromancer, flag = false) shouldBe emptyList()
        surveil(d, me, necromancer, flag = true) shouldBe listOf(necromancer)
    }

    test("the same land-short hand keeps a surveiled land on top") {
        val (d, me) = game(listOf("Island", "Swamp"), loggedHand)
        val swamp = d.putCardOnTopOfLibrary(me, "Swamp")

        surveil(d, me, swamp, flag = true) shouldBe emptyList()
    }

    test("flooded — six lands out, two in hand — the spell stays and the land goes") {
        val lands = listOf("Island", "Island", "Island", "Swamp", "Swamp", "Swamp")
        val (d, me) = game(lands, listOf("Swamp", "Island", "Medic's Kitesail"))
        val necromancer = d.putCardOnTopOfLibrary(me, "Theoretical Necromancer")
        surveil(d, me, necromancer, flag = true) shouldBe emptyList()

        val swamp = d.putCardOnTopOfLibrary(me, "Swamp")
        surveil(d, me, swamp, flag = true) shouldBe listOf(swamp)
    }
})
