package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.mtg.sets.definitions.fra.cards.GhaltaTheUnstoppable
import com.wingedsheep.mtg.sets.definitions.mrd.cards.RaiseTheAlarm
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.Deck
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

/**
 * Ghalta the Unstoppable {8}{G}
 *
 * "This spell costs {X} less to cast, where X is the greatest power among creatures you control."
 *
 * Pins the bug report that creature tokens were skipped when finding the greatest power: the
 * reduction looked each permanent up in the card registry, where tokens have no entry.
 */
class GhaltaTheUnstoppableScenarioTest : FunSpec({

    fun createDriver(): GameTestDriver {
        val driver = GameTestDriver()
        driver.registerCards(TestCards.all + listOf(GhaltaTheUnstoppable, RaiseTheAlarm))
        driver.initMirrorMatch(deck = Deck.of("Forest" to 40), skipMulligans = true, startingPlayer = 0)
        driver.passPriorityUntil(Step.PRECOMBAT_MAIN)
        return driver
    }

    test("a creature token's power counts toward the reduction") {
        val driver = createDriver()
        val me = driver.player1

        driver.giveMana(me, Color.WHITE, 2)
        val alarm = driver.putCardInHand(me, "Raise the Alarm")
        driver.castSpell(me, alarm).error shouldBe null
        driver.bothPass()

        // Only two 1/1 Soldier tokens: greatest power 1, so Ghalta costs {7}{G} — eight mana
        // is enough, where the unreduced {8}{G} would not be.
        val ghalta = driver.putCardInHand(me, "Ghalta the Unstoppable")
        driver.giveMana(me, Color.GREEN, 8)
        driver.castSpell(me, ghalta).error shouldBe null
        driver.bothPass()

        driver.findPermanent(me, "Ghalta the Unstoppable").shouldNotBeNull()
    }
})
