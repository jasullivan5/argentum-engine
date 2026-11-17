package com.wingedsheep.ai.engine

import com.wingedsheep.engine.core.CastSpell
import com.wingedsheep.engine.legalactions.ModalEnumerationMode
import com.wingedsheep.engine.legalactions.ModalLegalEnumeration
import com.wingedsheep.engine.registry.CardRegistry
import com.wingedsheep.engine.state.components.stack.ChosenTarget
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.ecl.cards.AshlingsCommand
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.Deck
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * A choose-N modal spell ("choose two —") is offered as one action whose modes the AI picks
 * itself. It used to take the first N available modes in printed order, each player slot aimed by
 * the removal ranking — from the 2026-10-31 AI-vs-humans review.
 */
class ModalChooseNAiTest : FunSpec({

    val cards = TestCards.all + listOf(AshlingsCommand)
    val registry = CardRegistry().apply { register(cards) }

    // Game 6d7c6893 turn 18: on 3 life, with a lone non-Elemental 1/1 against two 5/5s, Ashling's
    // Command was cast as "target player draws two cards" + "2 damage to each creature target
    // player controls", both at the opponent: two cards for them, 2 damage their 5/5s shrug off.
    test("Ashling's Command never draws or makes Treasure for the opponent") {
        // The AI sits second, as it did in the game, so the opponent is the first player offered.
        val d = GameTestDriver().apply {
            registerCards(cards)
            initMirrorMatch(deck = Deck.of("Island" to 40), skipMulligans = true, startingPlayer = 1)
            passPriorityUntil(Step.PRECOMBAT_MAIN)
        }
        val ai = d.player2
        val opponent = d.player1
        d.state.activePlayerId shouldBe ai
        d.setLifeTotal(ai, 3)
        d.setLifeTotal(opponent, 13)
        d.putCreatureOnBattlefield(ai, "Grizzly Bears")
        repeat(2) { d.putCreatureOnBattlefield(opponent, "Force of Nature") }
        repeat(4) { d.putLandOnBattlefield(ai, "Island") }
        repeat(3) { d.putLandOnBattlefield(ai, "Mountain") }
        val command = d.putCardInHand(ai, "Ashling's Command")
        val cast = d.legalActions(ai).single { (it.action as? CastSpell)?.cardId == command }

        val chosen = AIPlayer.create(registry, ai, AiProfile.LIVE).chooseFrom(d.state, listOf(cast)).action
        val spell = chosen.shouldBeInstanceOf<CastSpell>()

        // Mode 0 (copy an Elemental you control) has no target. Draw is the mode that matters.
        spell.chosenModes shouldContain 1
        val aimedAt = spell.chosenModes.zip(spell.modeTargetsOrdered).associate { (mode, targets) ->
            mode to targets.single().shouldBeInstanceOf<ChosenTarget.Player>().playerId
        }
        aimedAt[1] shouldBe ai
        // Treasure is ours to make; the damage, if dealt, is to their 5/5s, not our Bears.
        aimedAt[3]?.let { it shouldBe ai }
        aimedAt[2]?.let { it shouldBe opponent }
    }

    context("the mode selections compared") {
        fun modal(available: List<Boolean>, choose: Int, min: Int = choose, repeat: Boolean = false) =
            ModalLegalEnumeration(
                chooseCount = choose,
                minChooseCount = min,
                allowRepeat = repeat,
                modes = available.mapIndexed { i, ok -> ModalEnumerationMode(i, "mode $i", ok) },
                unavailableIndices = available.withIndex().filterNot { it.value }.map { it.index },
            )
        fun indices(m: ModalLegalEnumeration, cap: Int = 99) =
            TargetSelection.modeSelections(m, cap).map { modes -> modes.map { it.index } }

        test("choose two of four is every pair of the available modes") {
            indices(modal(listOf(false, true, true, true), choose = 2)) shouldBe
                listOf(listOf(1, 2), listOf(1, 3), listOf(2, 3))
        }
        test("a mode that may be chosen more than once pairs with itself") {
            indices(modal(listOf(true, true), choose = 2, repeat = true)) shouldBe
                listOf(listOf(0, 0), listOf(0, 1), listOf(1, 1))
        }
        test("choose one or more takes the widest selections first, up to the cap") {
            indices(modal(listOf(true, true, true), choose = 3, min = 1), cap = 4) shouldBe
                listOf(listOf(0, 1, 2), listOf(0, 1), listOf(0, 2), listOf(1, 2))
        }
    }
})
