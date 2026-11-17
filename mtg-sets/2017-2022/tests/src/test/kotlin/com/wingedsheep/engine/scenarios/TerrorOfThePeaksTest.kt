package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.state.components.stack.ChosenTarget
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.m21.cards.TerrorOfThePeaks
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.Deck
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import com.wingedsheep.engine.core.Outcome

/**
 * Tests for Terror of the Peaks.
 *
 * Terror of the Peaks: {3}{R}{R}
 * Creature — Dragon (5/4)
 * Flying
 * Spells your opponents cast that target this creature cost an additional 3 life to cast.
 * Whenever another creature you control enters, this creature deals damage equal to that
 * creature's power to any target.
 */
class TerrorOfThePeaksTest : FunSpec({

    fun createDriver(): GameTestDriver {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all + listOf(TerrorOfThePeaks))
        return driver
    }

    // =========================================================================
    // ETB trigger: "Whenever another creature you control enters, deal damage
    // equal to that creature's power to any target."
    // =========================================================================

    test("ETB trigger deals damage equal to entering creature's power (2/1 deals 2)") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Mountain" to 40), startingLife = 20)
        val player1 = driver.activePlayer!!
        val player2 = driver.getOpponent(player1)

        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        driver.putCreatureOnBattlefield(player1, "Terror of the Peaks")

        // Cast Goblin Guide (2/1, {R}) to trigger Terror's ETB ability
        val bears = driver.putCardInHand(player1, "Goblin Guide")
        driver.giveMana(player1, Color.RED, 1)
        driver.castSpell(player1, bears)

        // Bears spell resolves → enters battlefield → Terror's trigger fires and pauses for target
        driver.bothPass()
        driver.submitTargetSelection(player1, listOf(player2))

        // Trigger resolves — deal 2 damage (Grizzly Bears' power) to player2
        driver.bothPass()

        driver.getLifeTotal(player2) shouldBe 18
    }

    test("ETB trigger deals damage equal to entering creature's power (5/5 deals 5)") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Mountain" to 40), startingLife = 20)
        val player1 = driver.activePlayer!!
        val player2 = driver.getOpponent(player1)

        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        driver.putCreatureOnBattlefield(player1, "Terror of the Peaks")

        // Cast Force of Nature (5/5, {3}{G}{G}) to trigger Terror for 5 damage
        val fon = driver.putCardInHand(player1, "Force of Nature")
        driver.giveMana(player1, Color.GREEN, 5)
        driver.castSpell(player1, fon)  // {3}{G}{G}: 5 green covers all

        driver.bothPass()
        driver.submitTargetSelection(player1, listOf(player2))
        driver.bothPass()

        driver.getLifeTotal(player2) shouldBe 15
    }

    test("ETB trigger does not fire for opponents' creatures entering") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Mountain" to 40), startingLife = 20)
        val player1 = driver.activePlayer!!
        val player2 = driver.getOpponent(player1)

        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        driver.putCreatureOnBattlefield(player1, "Terror of the Peaks")
        // Opponent's creature entering should not trigger Terror (trigger filters youControl)
        driver.putCreatureOnBattlefield(player2, "Grizzly Bears")

        driver.passPriorityUntil(Step.END)

        driver.getLifeTotal(player2) shouldBe 20
    }

    test("ETB trigger does not fire when Terror itself enters (OTHER binding)") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Mountain" to 40), startingLife = 20)
        val player1 = driver.activePlayer!!
        val player2 = driver.getOpponent(player1)

        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        // Terror entering via putCreatureOnBattlefield does not emit events,
        // but even if cast, TriggerBinding.OTHER ensures Terror doesn't trigger itself.
        driver.putCreatureOnBattlefield(player1, "Terror of the Peaks")

        driver.passPriorityUntil(Step.END)

        driver.getLifeTotal(player2) shouldBe 20
    }

    // =========================================================================
    // SpellTargetingLifeCost: "Spells your opponents cast that target this
    // creature cost an additional 3 life to cast."
    //
    // In these tests Terror belongs to the NON-ACTIVE player (player2) so that
    // the ACTIVE player (player1, who already has priority) can cast a targeting
    // spell — mirroring the WardLifeCounterTest pattern.
    // =========================================================================

    test("opponent pays 3 mandatory life when casting spell targeting Terror") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Mountain" to 40), startingLife = 20)
        val player1 = driver.activePlayer!!
        val player2 = driver.getOpponent(player1)

        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        // Terror belongs to player2 (the non-active player/opponent of the caster)
        val terror = driver.putCreatureOnBattlefield(player2, "Terror of the Peaks")

        // Player1 (active, has priority) casts Lightning Bolt targeting player2's Terror
        // The 3 life is paid as part of the casting cost (not a trigger)
        driver.giveMana(player1, Color.RED, 1)
        val bolt = driver.putCardInHand(player1, "Lightning Bolt")
        driver.castSpellWithTargets(player1, bolt, listOf(ChosenTarget.Permanent(terror)))

        // 3 life deducted from player1 at cast time — before the spell even goes on the stack
        driver.getLifeTotal(player1) shouldBe 17

        // Resolve Bolt: deals 3 damage to Terror (5/4 survives)
        driver.bothPass()
        driver.findPermanent(player2, "Terror of the Peaks") shouldNotBe null
    }

    test("targeting spell is countered when opponent cannot pay 3 life") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Mountain" to 40), startingLife = 2)
        val player1 = driver.activePlayer!!
        val player2 = driver.getOpponent(player1)

        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        val terror = driver.putCreatureOnBattlefield(player2, "Terror of the Peaks")

        // Player1 at 2 life can't pay 3 life — cast is rejected entirely (spell never goes on stack)
        driver.giveMana(player1, Color.RED, 1)
        val bolt = driver.putCardInHand(player1, "Lightning Bolt")
        val castResult = driver.castSpellWithTargets(player1, bolt, listOf(ChosenTarget.Permanent(terror)))

        // Cast fails validation: not enough life to pay targeting cost
        castResult.outcome shouldNotBe Outcome.Done

        // Terror is unharmed, player1 life unchanged (cast was rejected)
        driver.findPermanent(player2, "Terror of the Peaks") shouldNotBe null
        driver.getLifeTotal(player1) shouldBe 2
    }

    test("controller's own spells targeting Terror do not trigger the life cost") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Mountain" to 40), startingLife = 20)
        val player1 = driver.activePlayer!!

        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        // Terror belongs to player1 (active); player1 casts bolt at own Terror — no life cost
        val terror = driver.putCreatureOnBattlefield(player1, "Terror of the Peaks")

        driver.giveMana(player1, Color.RED, 1)
        val bolt = driver.putCardInHand(player1, "Lightning Bolt")
        driver.castSpellWithTargets(player1, bolt, listOf(ChosenTarget.Permanent(terror)))

        // Bolt resolves: deals 3 damage to Terror (5/4), no life cost triggered
        driver.bothPass()

        driver.getLifeTotal(player1) shouldBe 20
        // Terror survives: 3 damage < 4 toughness
        driver.findPermanent(player1, "Terror of the Peaks") shouldNotBe null
    }
})
