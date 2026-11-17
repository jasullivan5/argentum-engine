package com.wingedsheep.engine.triggers

import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.state.components.identity.FaceDownComponent
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.Color
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.dsl.Effects
import com.wingedsheep.sdk.dsl.Triggers
import com.wingedsheep.sdk.dsl.card
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.scripting.GameObjectFilter
import com.wingedsheep.sdk.scripting.filters.unified.TargetFilter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Zone-change triggers filtered by color ("whenever another white creature you control enters" —
 * Court Street Denizen; "whenever a nonblack creature dies") reach
 * `TriggerMatcher.matchesCardPredicate` with `HasColor` / `NotColor`, which had no branch and
 * failed loudly the first time such a trigger was checked. A dies trigger reads the colors the
 * creature died with — after a color change, or none while face down — not its printed ones.
 */
class ColorFilteredTriggerTest : FunSpec({
    val whiteEntryWatcher = card("Color Test White Entry Watcher") {
        manaCost = "{0}"
        typeLine = "Enchantment"
        triggeredAbility {
            trigger = Triggers.a(GameObjectFilter.Creature.withColor(Color.WHITE).youControl()).enters()
            effect = Effects.GainLife(1)
        }
    }
    val nonblackDeathWatcher = card("Color Test Nonblack Death Watcher") {
        manaCost = "{0}"
        typeLine = "Enchantment"
        triggeredAbility {
            trigger = Triggers.a(GameObjectFilter.Creature.notColor(Color.BLACK)).dies()
            effect = Effects.GainLife(10)
        }
    }
    val whiteDeathWatcher = card("Color Test White Death Watcher") {
        manaCost = "{0}"
        typeLine = "Enchantment"
        triggeredAbility {
            trigger = Triggers.a(GameObjectFilter.Creature.withColor(Color.WHITE)).dies()
            effect = Effects.GainLife(10)
        }
    }
    val whiteBear = card("Color Test White Bear") {
        manaCost = "{W}"
        typeLine = "Creature — Bear"
        power = 2
        toughness = 2
    }
    val blackBear = card("Color Test Black Bear") {
        manaCost = "{B}"
        typeLine = "Creature — Bear"
        power = 2
        toughness = 2
    }
    val kill = card("Color Test Kill") {
        manaCost = "{0}"
        typeLine = "Instant"
        spell {
            val victim = target(TargetFilter.Creature)
            effect = Effects.Destroy(victim)
        }
    }

    val paintBlack = card("Color Test Paint Black") {
        manaCost = "{0}"
        typeLine = "Instant"
        spell {
            val creature = target(TargetFilter.Creature)
            effect = Effects.ChangeColor(creature, setOf(Color.BLACK))
        }
    }

    fun newDriver(watcher: String) = GameTestDriver().apply {
        registerCards(TestCards.all + listOf(whiteEntryWatcher, nonblackDeathWatcher, whiteDeathWatcher, whiteBear, blackBear, kill, paintBlack))
        initMirrorMatch(deck = Deck.of("Plains" to 40), startingLife = 20)
        passPriorityUntil(Step.PRECOMBAT_MAIN)
        putPermanentOnBattlefield(player1, watcher)
    }

    fun GameTestDriver.resolveAll() {
        bothPass()
        while (stackSize > 0) bothPass()
    }

    fun GameTestDriver.castBear(color: Color, bear: String) {
        giveMana(player1, color, 1)
        castSpell(player1, putCardInHand(player1, bear)).error shouldBe null
        resolveAll()
    }

    fun GameTestDriver.paintBlack(bear: String) {
        castSpell(player1, putCardInHand(player1, paintBlack.name), listOf(findPermanent(player1, bear)!!)).error shouldBe null
        resolveAll()
    }

    fun GameTestDriver.turnFaceDown(bear: String): EntityId {
        val id = findPermanent(player1, bear)!!
        replaceState(state.updateEntity(id) { it.with(FaceDownComponent) })
        return id
    }

    fun GameTestDriver.destroy(victim: EntityId) {
        castSpell(player1, putCardInHand(player1, kill.name), listOf(victim)).error shouldBe null
        resolveAll()
    }

    fun GameTestDriver.destroy(bear: String) = destroy(findPermanent(player1, bear)!!)

    test("a white creature entering fires the white-creature entry trigger") {
        val d = newDriver(whiteEntryWatcher.name)
        d.castBear(Color.WHITE, whiteBear.name)
        d.getLifeTotal(d.player1) shouldBe 21
    }

    test("a black creature entering does not fire the white-creature entry trigger") {
        val d = newDriver(whiteEntryWatcher.name)
        d.castBear(Color.BLACK, blackBear.name)
        d.getLifeTotal(d.player1) shouldBe 20
    }

    test("a nonblack creature dying fires the nonblack-creature death trigger") {
        val d = newDriver(nonblackDeathWatcher.name)
        d.castBear(Color.WHITE, whiteBear.name)
        d.destroy(whiteBear.name)
        d.getLifeTotal(d.player1) shouldBe 30
    }

    test("a black creature dying does not fire the nonblack-creature death trigger") {
        val d = newDriver(nonblackDeathWatcher.name)
        d.castBear(Color.BLACK, blackBear.name)
        d.destroy(blackBear.name)
        d.getLifeTotal(d.player1) shouldBe 20
    }

    test("a white creature turned black dies as a black creature, so the nonblack death trigger does not fire") {
        val d = newDriver(nonblackDeathWatcher.name)
        d.castBear(Color.WHITE, whiteBear.name)
        d.paintBlack(whiteBear.name)
        d.destroy(whiteBear.name)
        d.getLifeTotal(d.player1) shouldBe 20
    }

    test("a face-down white creature dies colorless, so the white death trigger does not fire") {
        val d = newDriver(whiteDeathWatcher.name)
        d.castBear(Color.WHITE, whiteBear.name)
        d.destroy(d.turnFaceDown(whiteBear.name))
        d.getLifeTotal(d.player1) shouldBe 20
    }
})
