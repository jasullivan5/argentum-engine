package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.ActivateAbility
import com.wingedsheep.engine.core.ChooseNumberDecision
import com.wingedsheep.engine.core.NumberChosenResponse
import com.wingedsheep.engine.state.components.battlefield.CountersComponent
import com.wingedsheep.engine.state.components.player.ManaPoolComponent
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.fin.cards.ViviOrnitier
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.CounterType
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.model.EntityId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import com.wingedsheep.engine.core.Outcome

/**
 * Vivi Ornitier (FIN #248) — {1}{U}{R} Legendary Creature — Wizard 0/3.
 *
 * "Whenever you cast a noncreature spell, put a +1/+1 counter on Vivi Ornitier and it deals 1
 *  damage to each opponent."
 *
 * Casting a noncreature spell (Divination) grows Vivi by a +1/+1 counter and pings the opponent for
 * exactly 1 (the source being Vivi). The {0} mana ability adds X mana split freely between {U} and
 * {R} — a mixed split, not X of a single color.
 */
class ViviOrnitierScenarioTest : FunSpec({

    fun createDriver(): GameTestDriver {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all)
        return driver
    }

    fun plusOneCounters(driver: GameTestDriver, id: EntityId): Int =
        driver.state.getEntity(id)?.get<CountersComponent>()?.getCount(CounterType.PLUS_ONE_PLUS_ONE) ?: 0

    test("casting a noncreature spell grows Vivi and deals 1 to the opponent") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Island" to 40), startingLife = 20)
        val active = driver.activePlayer!!
        val opponent = driver.getOpponent(active)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        val vivi = driver.putCreatureOnBattlefield(active, "Vivi Ornitier")

        val divination = driver.putCardInHand(active, "Divination") // noncreature, no target
        driver.giveMana(active, Color.BLUE, 1)
        driver.giveColorlessMana(active, 2)

        driver.castSpell(active, divination).outcome shouldBe Outcome.Done
        driver.bothPass() // the cast trigger resolves (counter + ping), then Divination resolves

        plusOneCounters(driver, vivi) shouldBe 1
        driver.getLifeTotal(opponent) shouldBe 19
    }

    test("{0} ability adds X mana in any combination of {U} and {R}") {
        val driver = createDriver()
        driver.initMirrorMatch(deck = Deck.of("Island" to 40), startingLife = 20)
        val active = driver.activePlayer!!
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)

        val vivi = driver.putCreatureOnBattlefield(active, "Vivi Ornitier")
        driver.replaceState(driver.state.updateEntity(vivi) {
            it.with(CountersComponent().withAdded(CounterType.PLUS_ONE_PLUS_ONE, 5))
        })

        driver.submit(ActivateAbility(active, vivi, ViviOrnitier.activatedAbilities.first().id))
        val decision = driver.pendingDecision as ChooseNumberDecision
        decision.maxValue shouldBe 5

        // 2 blue, the remaining 3 red.
        driver.submitDecision(active, NumberChosenResponse(decision.id, 2)).error shouldBe null

        val pool = driver.state.getEntity(active)!!.get<ManaPoolComponent>()!!
        pool.blue shouldBe 2
        pool.red shouldBe 3
    }
})
