package com.wingedsheep.engine.mechanics.dexterity

import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.model.GameRng
import kotlinx.serialization.Serializable
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The numbers a flick is scored against, sent with the decision so the client draws the same spin
 * gauge the engine scores and turns the player's swipe into the same units. A flick carries
 * [flightTime] seconds of its release speed across the table; it spins for [airTime] seconds, so
 * below [tooLittleSpinBelow] turns per second it can't turn over completely. Inside
 * [cleanSpinFrom]..[cleanSpinTo] it flies true; a spin [spinSlop] outside that band is as wild as
 * it gets. The scatter terms are [FlipPhysics.scatter]'s.
 */
@Serializable
data class FlipReleaseProfile(
    val flightTime: Double = FlipPhysics.FLIGHT_TIME,
    val airTime: Double = FlipPhysics.AIR_TIME,
    val tooLittleSpinBelow: Double = FlipPhysics.TOO_LITTLE_SPIN,
    val cleanSpinFrom: Double = FlipPhysics.CLEAN_SPIN_FROM,
    val cleanSpinTo: Double = FlipPhysics.CLEAN_SPIN_TO,
    val spinSlop: Double = FlipPhysics.SPIN_SLOP,
    val maxSpin: Double = FlipPhysics.MAX_SPIN,
    val baseScatter: Double = FlipPhysics.BASE_SCATTER,
    val scatterPerMm: Double = FlipPhysics.SCATTER_PER_MM,
    val sloppyScatter: Double = FlipPhysics.SLOPPY_SCATTER,
)

/**
 * How a flicked card flies and where it lands.
 *
 * The player throws it for real: they wind up spin, then flick the card forward from their hand
 * and let go. The flick's velocity at release is the whole aim — its direction is the line, its
 * speed how far the card carries ([FLIGHT_TIME] seconds of it). The spin decides how it flies: too
 * little and it can't turn over completely; inside the clean band it flies true and lands within a
 * few millimetres of where the flick sent it; spun too hard it wobbles in the air, scatters, and
 * skids on along its line when it lands. All randomness comes from the game's seeded RNG, so a
 * flip replays identically.
 */
object FlipPhysics {
    /** Seconds of release speed the card carries: a 2 m/s flick lands 60 cm out. */
    const val FLIGHT_TIME = 0.3
    /** Seconds the card spins in the air: each turn per second of spin is two half-turns. */
    const val AIR_TIME = 0.6
    /** Below this spin (turns/s) the card can't make the two half-turns a full turn-over needs. */
    const val TOO_LITTLE_SPIN = 2.0 / (2.0 * AIR_TIME)
    /** The spin (turns/s) at which the card flies true. */
    const val CLEAN_SPIN_FROM = 2.4
    const val CLEAN_SPIN_TO = 4.0
    /** How far outside the clean band a spin must stray to be as wild as it gets. */
    const val SPIN_SLOP = 2.5
    /** The fastest spin and flick the engine believes; anything past them is clamped. */
    const val MAX_SPIN = 12.0
    const val MAX_SPEED = 6000.0

    /** Scatter (one standard deviation, mm) of a clean flick, before distance. */
    const val BASE_SCATTER = 3.0
    /** Extra scatter per mm the card carries. */
    const val SCATTER_PER_MM = 0.016
    /** Extra scatter of the wildest spin. */
    const val SLOPPY_SCATTER = 35.0
    /** How far (mm) an over-spun card skids on along its line per turn/s above the clean band. */
    const val SKID_PER_SPIN = 22.0

    /** Spread (degrees, one standard deviation) of the resting angle of a clean / the wildest spin. */
    const val CLEAN_SKEW = 7.0
    const val SLOPPY_SKEW = 40.0

    /**
     * Spread (half-turns, one standard deviation) around the spin's half-turn count, cut off at
     * [HALF_TURN_NOISE_CAP] deviations so any spin in the clean band always turns over.
     */
    private const val HALF_TURN_NOISE = 0.35
    private const val HALF_TURN_NOISE_CAP = 2.5

    /** The spin the AI and tests aim for: the middle of the clean band. */
    const val IDEAL_SPIN = (CLEAN_SPIN_FROM + CLEAN_SPIN_TO) / 2

    private fun clampSpin(spin: Double) = if (spin.isFinite()) kotlin.math.abs(spin).coerceAtMost(MAX_SPIN) else 0.0

    /** 1 for a spin in the clean band, falling to 0 as it strays [SPIN_SLOP] outside it. */
    fun spinQuality(spin: Double): Double {
        val s = clampSpin(spin)
        val miss = when {
            s < CLEAN_SPIN_FROM -> CLEAN_SPIN_FROM - s
            s > CLEAN_SPIN_TO -> s - CLEAN_SPIN_TO
            else -> 0.0
        }
        return 1.0 - min(1.0, miss / SPIN_SLOP)
    }

    /** The release velocity, clamped to [MAX_SPEED] and with non-numbers read as no flick at all. */
    private fun clampVelocity(vx: Double, vy: Double): Pair<Double, Double> {
        if (!vx.isFinite() || !vy.isFinite()) return 0.0 to 0.0
        val speed = hypot(vx, vy)
        return if (speed <= MAX_SPEED) vx to vy else vx * MAX_SPEED / speed to vy * MAX_SPEED / speed
    }

    /** Where a flick released at ([vx], [vy]) mm/s carries to before spin and scatter. */
    fun carry(table: FlipTable, vx: Double, vy: Double): Pair<Double, Double> {
        val (cx, cy) = clampVelocity(vx, vy)
        return (table.handX + cx * FLIGHT_TIME) to (table.handY + cy * FLIGHT_TIME)
    }

    /** The velocity (mm/s) of a flick that carries exactly to ([x], [y]) — for the AI and tests. */
    fun flickToward(table: FlipTable, x: Double, y: Double): Pair<Double, Double> =
        (x - table.handX) / FLIGHT_TIME to (y - table.handY) / FLIGHT_TIME

    /** How far (mm) an over-spun card skids on along its line; 0 at or below the clean band. */
    fun skid(spin: Double): Double = max(0.0, clampSpin(spin) - CLEAN_SPIN_TO) * SKID_PER_SPIN

    /** Standard deviation (mm) of where a flick that carries [distance] mm with [spin] lands. */
    fun scatter(distance: Double, spin: Double): Double =
        BASE_SCATTER + SCATTER_PER_MM * distance + SLOPPY_SCATTER * (1.0 - spinQuality(spin)).pow(1.6)

    /** Roll where the flick lands. Returns the landing and the advanced RNG. */
    fun land(
        table: FlipTable,
        velocityX: Double,
        velocityY: Double,
        spin: Double,
        rng: GameRng,
    ): Pair<FlipLanding, GameRng> {
        val (vx, vy) = clampVelocity(velocityX, velocityY)
        val speed = hypot(vx, vy)
        val w = clampSpin(spin)
        val (cx, cy) = carry(table, vx, vy)
        val distance = speed * FLIGHT_TIME
        val (ux, uy) = if (speed < 1.0) 0.0 to -1.0 else vx / speed to vy / speed
        val skid = skid(w)
        val sigma = scatter(distance, w)

        var r = rng
        fun uniform(): Double { val (d, next) = r.nextDouble(); r = next; return d }
        fun gaussian(): Double {
            // Box–Muller; 1 - u keeps the log argument away from zero.
            val u1 = 1.0 - uniform()
            val u2 = uniform()
            return sqrt(-2.0 * ln(u1)) * cos(2.0 * Math.PI * u2)
        }

        val x = cx + ux * skid + gaussian() * sigma
        val y = cy + uy * skid + gaussian() * sigma
        // A clean spin lands it nearly square to the thrower; a wild one can come down at any skew.
        val skew = CLEAN_SKEW + SLOPPY_SKEW * (1.0 - spinQuality(w))
        val rotation = ((gaussian() * skew) % 360.0 + 360.0) % 360.0
        val halfTurns = floor(max(0.0, 2.0 * w * AIR_TIME + gaussian().coerceIn(-HALF_TURN_NOISE_CAP, HALF_TURN_NOISE_CAP) * HALF_TURN_NOISE)).toInt()
        return FlipLanding(x = x, y = y, rotation = rotation, halfTurns = halfTurns, speed = speed, spin = w, skid = skid) to r
    }

    /**
     * Every card on [table] the flipped card, resting at [landing], overlaps — the touchable part of
     * it (an attachment only where it shows from under its host). Rotated-rectangle overlap by the
     * separating-axis test.
     */
    fun touched(table: FlipTable, landing: FlipLanding): List<EntityId> =
        table.cards.filter { overlaps(landing, it) }.map { it.entityId }

    fun overlaps(landing: FlipLanding, card: FlipTableCard): Boolean {
        val theta = Math.toRadians(landing.rotation)
        val c = cos(theta)
        val s = sin(theta)
        val hw = FlipTableLayout.CARD_WIDTH / 2
        val hh = FlipTableLayout.CARD_HEIGHT / 2
        // The landed card's corners.
        val orb = listOf(-hw to -hh, hw to -hh, hw to hh, -hw to hh).map { (dx, dy) ->
            (landing.x + dx * c - dy * s) to (landing.y + dx * s + dy * c)
        }
        val left = card.x - card.width / 2
        val right = card.x + card.width / 2
        val rect = listOf(left to card.hitTop, right to card.hitTop, right to card.hitBottom, left to card.hitBottom)
        val axes = listOf(1.0 to 0.0, 0.0 to 1.0, c to s, -s to c)
        return axes.all { (axisX, axisY) ->
            val a = orb.map { (px, py) -> px * axisX + py * axisY }
            val b = rect.map { (px, py) -> px * axisX + py * axisY }
            // Touching edges count as touching; strict separation is a miss.
            a.max() >= b.min() && b.max() >= a.min()
        }
    }
}
