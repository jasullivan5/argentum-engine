package com.wingedsheep.engine.mechanics.dexterity

import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.model.GameRng
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.ints.shouldBeGreaterThan as intShouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan as intShouldBeLessThan
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Engine coverage for the dexterity flip's physics ([FlipPhysics]): what counts as touching, and how
 * the player's flick shapes where the card lands and whether it turns over. Chaos Orb's own text
 * is the rule here — the Comprehensive Rules say nothing about dexterity.
 */
class FlipPhysicsTest : FunSpec({

    val host = EntityId("host")
    val aura = EntityId("aura")

    fun card(id: EntityId, x: Double, y: Double, w: Double = 63.0, h: Double = 88.0) =
        FlipTableCard(entityId = id, controllerId = EntityId("p2"), x = x, y = y, width = w, height = h, tapped = false, eligible = true)

    fun table(vararg cards: FlipTableCard) = FlipTable(
        width = 700.0, height = 600.0,
        bands = emptyList(), cards = cards.toList(),
        handX = 350.0, handY = 670.0,
    )

    context("touching") {
        val target = card(host, x = 300.0, y = 200.0)

        test("a card resting squarely on another touches it") {
            FlipPhysics.overlaps(FlipLanding(300.0, 200.0, 0.0, halfTurns = 2), target).shouldBeTrue()
        }

        test("edges that just meet still touch") {
            // 63 mm wide each, centres 63 mm apart: the long edges lie on each other.
            FlipPhysics.overlaps(FlipLanding(363.0, 200.0, 0.0, halfTurns = 2), target).shouldBeTrue()
            FlipPhysics.overlaps(FlipLanding(363.5, 200.0, 0.0, halfTurns = 2), target).shouldBeFalse()
        }

        test("a turned card reaches further than an upright one") {
            // Upright, its half-width is 31.5 mm; turned 90° it's 44 mm — so 72 mm away only the turned one touches.
            FlipPhysics.overlaps(FlipLanding(372.0, 200.0, 0.0, halfTurns = 2), target).shouldBeFalse()
            FlipPhysics.overlaps(FlipLanding(372.0, 200.0, 90.0, halfTurns = 2), target).shouldBeTrue()
        }

        test("a diagonal landing whose corner clears the card is a miss, by the separating-axis test") {
            // Bounding boxes overlap, but at 45° the corner points away from the card.
            FlipPhysics.overlaps(FlipLanding(300.0 + 31.5 + 45.0, 200.0 - 44.0 - 45.0, 45.0, halfTurns = 2), target).shouldBeFalse()
        }

        test("an Aura tucked under its host is touchable only where it shows") {
            // The strip behind the host: the aura's back 22 mm.
            val tucked = card(aura, x = 300.0, y = 178.0).copy(hitTop = 134.0, hitBottom = 156.0)
            // Resting on the host's lower half covers the aura's hidden part, not its strip.
            FlipPhysics.overlaps(FlipLanding(300.0, 260.0, 0.0, halfTurns = 2), tucked).shouldBeFalse()
            FlipPhysics.overlaps(FlipLanding(300.0, 120.0, 0.0, halfTurns = 2), tucked).shouldBeTrue()
        }

        test("touched lists every card the landing overlaps") {
            val left = card(EntityId("left"), x = 200.0, y = 200.0)
            val right = card(EntityId("right"), x = 273.0, y = 200.0)
            val far = card(EntityId("far"), x = 600.0, y = 500.0)
            FlipPhysics.touched(table(left, right, far), FlipLanding(236.5, 200.0, 0.0, halfTurns = 2)) shouldBe
                listOf(EntityId("left"), EntityId("right"))
        }
    }

    context("the flick") {
        val t = table()
        val ideal = FlipPhysics.IDEAL_SPIN
        fun flick(x: Double, y: Double, spin: Double = ideal, seed: Long = 1L) =
            FlipPhysics.flickToward(t, x, y).let { (vx, vy) -> FlipPhysics.land(t, vx, vy, spin, GameRng.seeded(seed)).first }

        test("the flick's speed is how far it carries, its direction the line it flies") {
            val (vx, vy) = FlipPhysics.flickToward(t, 350.0, 200.0)
            FlipPhysics.carry(t, vx, vy).let { (x, y) ->
                x shouldBe (350.0 plusOrMinus 1e-9)
                y shouldBe (200.0 plusOrMinus 1e-9)
            }
            // Half as hard carries half as far along the same line.
            FlipPhysics.carry(t, vx / 2, vy / 2).second shouldBe ((t.handY + 200.0) / 2 plusOrMinus 1e-9)
        }

        test("a spin in the clean band flies true; straying outside it is wilder") {
            FlipPhysics.spinQuality(ideal) shouldBe 1.0
            FlipPhysics.spinQuality(FlipPhysics.CLEAN_SPIN_FROM) shouldBe 1.0
            FlipPhysics.spinQuality(0.0) shouldBeLessThan 0.1
            FlipPhysics.spinQuality(FlipPhysics.MAX_SPIN) shouldBe 0.0
            FlipPhysics.scatter(470.0, 8.0) shouldBeGreaterThan FlipPhysics.scatter(470.0, ideal)
        }

        test("throwing further scatters more") {
            FlipPhysics.scatter(570.0, ideal) shouldBeGreaterThan FlipPhysics.scatter(170.0, ideal)
        }

        test("the same flick from the same game state lands in the same place") {
            val rng = GameRng.seeded(42)
            FlipPhysics.land(t, 0.0, -1500.0, ideal, rng).first shouldBe FlipPhysics.land(t, 0.0, -1500.0, ideal, rng).first
            FlipPhysics.land(t, 0.0, -1500.0, ideal, rng).second shouldNotBe rng
        }

        test("a clean spin always turns over; too little spin almost never does") {
            val seeds = 0L until 400L
            seeds.count { flick(350.0, 200.0, seed = it).turnedOver } shouldBe 400
            seeds.count { flick(350.0, 200.0, spin = FlipPhysics.CLEAN_SPIN_FROM, seed = it).turnedOver } shouldBe 400
            seeds.count { flick(350.0, 200.0, spin = 1.0, seed = it).turnedOver } intShouldBeLessThan 5
            FlipPhysics.TOO_LITTLE_SPIN shouldBeLessThan FlipPhysics.CLEAN_SPIN_FROM
        }

        test("a clean flick lands where it was flicked: the hand, not luck, decides what it hits") {
            val misses = (0L until 400L).map {
                val landing = flick(350.0, 200.0, seed = it)
                kotlin.math.hypot(landing.x - 350.0, landing.y - 200.0)
            }
            misses.average() shouldBeLessThan 15.0
            // Within a card gap of the aim almost every time.
            misses.count { it > 30.0 } intShouldBeLessThan 16
        }

        test("spun too hard, it skids on along its line and comes down wild") {
            FlipPhysics.skid(ideal) shouldBe 0.0
            val landing = flick(350.0, 200.0, spin = 8.0)
            landing.skid shouldBeGreaterThan 80.0
            // The hand is straight below the aim, so skidding on carries it further up the table.
            val meanY = (0L until 200L).map { flick(350.0, 200.0, spin = 8.0, seed = it).y }.average()
            meanY shouldBeLessThan 200.0 - 60.0
        }

        test("a clean spin lands nearly square, a wild one skews") {
            fun skew(spin: Double) = (0L until 200L).map {
                val r = flick(350.0, 200.0, spin = spin, seed = it).rotation
                kotlin.math.abs(((r + 180.0) % 360.0) - 180.0)
            }.average()
            skew(ideal) shouldBeLessThan 12.0
            skew(9.0) shouldBeGreaterThan skew(ideal) * 2
        }

        test("absurd numbers are clamped, so no flick can reach an absurd point") {
            val landing = FlipPhysics.land(t, 1e12, -1e12, 1e9, GameRng.seeded(7)).first
            kotlin.math.hypot(landing.x - t.handX, landing.y - t.handY) shouldBeLessThan
                FlipPhysics.MAX_SPEED * FlipPhysics.FLIGHT_TIME + 600.0
            landing.spin shouldBe FlipPhysics.MAX_SPIN
            FlipPhysics.land(t, Double.NaN, 0.0, Double.NaN, GameRng.seeded(7)).first.speed shouldBe 0.0
        }
    }
})
