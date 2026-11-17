package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.mkm.cards.AureliaTheLawAbove
import com.wingedsheep.sdk.core.ManaCost
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.core.Subtype
import com.wingedsheep.sdk.model.CardDefinition
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.model.EntityId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Scenario tests for Aurelia, the Law Above.
 *
 * Aurelia, the Law Above ({3}{R}{W}): Legendary Creature — Angel 4/4
 *   "Flying, vigilance, haste
 *    Whenever a player attacks with three or more creatures, you draw a card.
 *    Whenever a player attacks with five or more creatures, Aurelia deals 3 damage to each of your
 *    opponents and you gain 3 life."
 *
 * Both abilities fire **once per attack declaration**, not once per attacking creature — the
 * regression that made a three-creature attack draw three cards. Aurelia sits on the defending
 * side throughout, since "a player" includes an opponent's attack. Each case reads the totals at
 * declare blockers, after the triggers have resolved and before any combat damage muddies them.
 */
class AureliaTheLawAboveScenarioTest : FunSpec({

    val Bear = CardDefinition.creature(
        name = "Law Above Bear",
        manaCost = ManaCost.parse("{1}{G}"),
        subtypes = setOf(Subtype("Bear")),
        power = 2,
        toughness = 2,
        oracleText = ""
    )

    /** The attacking player, the defender who controls Aurelia, and the defender's hand size before the attack. */
    data class Attack(val attacker: EntityId, val defender: EntityId, val defenderHandBefore: Int)

    /**
     * The active player attacks the defender — who controls Aurelia — with [attackerCount] bears,
     * stopping once every attack trigger has resolved.
     */
    fun attackWithBears(driver: GameTestDriver, attackerCount: Int): Attack {
        driver.registerCards(TestCards.all + listOf(Bear, AureliaTheLawAbove))
        driver.initMirrorMatch(deck = Deck.of("Forest" to 40), startingLife = 20)
        val attacker = driver.activePlayer!!
        val defender = driver.getOpponent(attacker)

        driver.putCreatureOnBattlefield(defender, "Aurelia, the Law Above")
        val bears = (1..attackerCount).map { driver.putCreatureOnBattlefield(attacker, "Law Above Bear") }
        bears.forEach { driver.removeSummoningSickness(it) }

        driver.passPriorityUntil(Step.DECLARE_ATTACKERS)
        val handBefore = driver.getHandSize(defender)
        driver.declareAttackers(attacker, bears, defender)
        driver.passPriorityUntil(Step.DECLARE_BLOCKERS)
        return Attack(attacker, defender, handBefore)
    }

    test("an opponent attacking with three creatures draws exactly one card") {
        val driver = GameTestDriver()
        val attack = attackWithBears(driver, 3)

        // One firing for the declaration, not one per attacker.
        driver.getHandSize(attack.defender) shouldBe attack.defenderHandBefore + 1
        // The five-attacker ability never fired.
        driver.getLifeTotal(attack.attacker) shouldBe 20
        driver.getLifeTotal(attack.defender) shouldBe 20
    }

    test("two attackers don't trigger it") {
        val driver = GameTestDriver()
        val attack = attackWithBears(driver, 2)

        driver.getHandSize(attack.defender) shouldBe attack.defenderHandBefore
        driver.getLifeTotal(attack.attacker) shouldBe 20
        driver.getLifeTotal(attack.defender) shouldBe 20
    }

    test("five attackers draw one card and deal three damage once") {
        val driver = GameTestDriver()
        val attack = attackWithBears(driver, 5)

        // Each ability fires once: one card, 3 damage to the attacking player, 3 life gained.
        driver.getHandSize(attack.defender) shouldBe attack.defenderHandBefore + 1
        driver.getLifeTotal(attack.attacker) shouldBe 20 - 3
        driver.getLifeTotal(attack.defender) shouldBe 20 + 3
    }
})
