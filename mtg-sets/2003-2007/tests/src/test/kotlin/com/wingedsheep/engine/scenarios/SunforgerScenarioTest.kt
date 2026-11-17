package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.ActivateAbility
import com.wingedsheep.engine.core.SelectCardsDecision
import com.wingedsheep.engine.core.SpellCastEvent
import com.wingedsheep.engine.core.LibraryShuffledEvent
import com.wingedsheep.engine.state.components.battlefield.AttachedToComponent
import com.wingedsheep.engine.state.components.stack.ChosenTarget
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.rav.cards.Sunforger
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.model.EntityId
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Sunforger (RAV #272) — "{R}{W}, Unattach this Equipment: Search your library for a red or white
 * instant card with mana value 4 or less and cast that card without paying its mana cost. Then
 * shuffle."
 *
 * The new vocabulary is `CostAtom.Unattach` (`Costs.Unattach`), and its whole point is that it is a
 * *cost* with a real affordability gate rather than a free rider on the effect. What these tests
 * pin:
 *
 * - "You can't pay the cost of unattaching Sunforger unless Sunforger is attached to a creature"
 *   (the printed 2020-08-07 ruling) — the ability is not offered at all while it sits unattached,
 *   even with the mana in hand;
 * - paying it actually detaches, and the +4/+0 it was granting goes with it;
 * - the search pool honours the card's filter (a red or white instant of mana value 4 or less, not
 *   any instant and not a sorcery), and the found card is cast for free.
 */
class SunforgerScenarioTest : FunSpec({

    val forgeAbility = Sunforger.activatedAbilities[0].id
    val equipAbility = Sunforger.activatedAbilities[1].id

    fun driver(): GameTestDriver {
        val d = GameTestDriver()
        d.registerCards(TestCards.all + Sunforger)
        d.initMirrorMatch(deck = Deck.of("Mountain" to 40), skipMulligans = true, startingPlayer = 0)
        d.passPriorityUntil(Step.PRECOMBAT_MAIN)
        return d
    }

    /** Puts Sunforger and [creatureName] on player 1's battlefield and equips them. */
    fun GameTestDriver.equipTo(creatureName: String): Pair<EntityId, EntityId> {
        val forger = putPermanentOnBattlefield(player1, "Sunforger")
        val creature = putCreatureOnBattlefield(player1, creatureName)
        giveColorlessMana(player1, 3)
        submit(
            ActivateAbility(
                playerId = player1,
                sourceId = forger,
                abilityId = equipAbility,
                targets = listOf(ChosenTarget.Permanent(creature))
            )
        ).error shouldBe null
        bothPass()
        state.getEntity(forger)?.get<AttachedToComponent>()?.targetId shouldBe creature
        return forger to creature
    }

    test("the ability isn't offered while Sunforger is unattached, even with the mana available") {
        val d = driver()
        val forger = d.putPermanentOnBattlefield(d.player1, "Sunforger")
        d.giveMana(d.player1, Color.RED, 1)
        d.giveMana(d.player1, Color.WHITE, 1)

        withClue("nothing is attached, so the unattach cost can't be paid and the client greys it out") {
            d.legalActions(d.player1).single { legal ->
                val action = legal.action
                action is ActivateAbility && action.abilityId == forgeAbility
            }.affordable shouldBe false
        }
        withClue("and the engine refuses the activation outright") {
            (d.submit(ActivateAbility(d.player1, forger, forgeAbility)).error != null) shouldBe true
        }
    }

    test("paying the cost detaches Sunforger and casts the found instant for free") {
        val d = driver()
        val (forger, bear) = d.equipTo("Centaur Courser") // 3/3

        withClue("+4/+0 while attached") {
            d.state.projectedState.getPower(bear) shouldBe 7
        }

        // A legal find, plus two cards the filter must reject: a blue instant and a red sorcery.
        d.putCardOnTopOfLibrary(d.player1, "Counterspell")
        d.putCardOnTopOfLibrary(d.player1, "Doom Blade")
        val bolt = d.putCardOnTopOfLibrary(d.player1, "Lightning Bolt")

        d.giveMana(d.player1, Color.RED, 1)
        d.giveMana(d.player1, Color.WHITE, 1)
        val eventStart = d.events.size
        d.submit(ActivateAbility(d.player1, forger, forgeAbility)).error shouldBe null
        d.bothPass()

        val search = d.pendingDecision
        withClue("the search decision is raised: $search") { (search is SelectCardsDecision) shouldBe true }
        withClue("only the red-or-white instant of mana value 4 or less is on offer") {
            (search as SelectCardsDecision).options shouldBe listOf(bolt)
        }
        d.submitCardSelection(d.player1, listOf(bolt)).error shouldBe null

        // The source says cast, then shuffle. Choosing the found spell's targets is part of casting.
        d.events.drop(eventStart).filterIsInstance<LibraryShuffledEvent>() shouldBe emptyList()

        // Cast without paying its mana cost — the Bolt still needs a target.
        d.submitTargetSelection(d.player1, listOf(d.player2)).error shouldBe null
        val sequence = d.events.drop(eventStart)
        val castIndex = sequence.indexOfFirst { it is SpellCastEvent }
        val shuffleIndex = sequence.indexOfFirst { it is LibraryShuffledEvent }
        (castIndex >= 0 && shuffleIndex > castIndex) shouldBe true
        var guard = 0
        while (d.stackSize > 0 && guard++ < 10) d.bothPass()

        withClue("the free Bolt resolved") { d.getLifeTotal(d.player2) shouldBe 17 }
        withClue("the unattach cost was really paid") {
            d.state.getEntity(forger)?.get<AttachedToComponent>()?.targetId shouldBe null
        }
        withClue("so the +4/+0 is gone with it") {
            d.state.projectedState.getPower(bear) shouldBe 3
        }
        withClue("Sunforger itself never left the battlefield — unattaching moves no zones") {
            d.findPermanent(d.player1, "Sunforger") shouldBe forger
        }
    }

    test("a library with nothing the filter admits finds nothing and still unattaches") {
        val d = driver()
        val (forger, _) = d.equipTo("Centaur Courser")
        d.putCardOnTopOfLibrary(d.player1, "Counterspell")

        d.giveMana(d.player1, Color.RED, 1)
        d.giveMana(d.player1, Color.WHITE, 1)
        d.submit(ActivateAbility(d.player1, forger, forgeAbility)).error shouldBe null
        d.bothPass()

        var guard = 0
        while (d.pendingDecision != null && guard++ < 5) {
            d.submitCardSelection(d.player1, emptyList()).error shouldBe null
        }

        withClue("a search may always fail to find (CR 701.19c) — nothing was cast") {
            d.getLifeTotal(d.player2) shouldBe 20
        }
        withClue("the cost was still paid: costs are paid before the effect does anything") {
            d.state.getEntity(forger)?.get<AttachedToComponent>()?.targetId shouldBe null
        }
    }
})
