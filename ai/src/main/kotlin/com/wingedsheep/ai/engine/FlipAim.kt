package com.wingedsheep.ai.engine

import com.wingedsheep.ai.engine.evaluation.BoardPresence
import com.wingedsheep.engine.core.FlipOntoBattlefieldDecision
import com.wingedsheep.engine.core.FlipThrowResponse
import com.wingedsheep.engine.mechanics.dexterity.FlipLanding
import com.wingedsheep.engine.mechanics.dexterity.FlipPhysics
import com.wingedsheep.engine.mechanics.dexterity.FlipTableCard
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.sdk.model.EntityId
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Where the AI throws a dexterity flip (Chaos Orb).
 *
 * The AI plays the mini-game with a human's hands: it picks a goal, then flicks a little too hard
 * or too soft, a little off line and with a little more or less spin than it meant, like a decent
 * player does — so it usually hits what it aims at, sometimes clips the card beside it, and now
 * and then misses outright. It
 * scores each candidate aim point by its *expected* haul under that unsteadiness plus the
 * engine's own scatter: the value of the opponents' permanents it would destroy minus a little
 * more than the value of its own. Candidates are each counted permanent and the midpoints of
 * nearby pairs (an Orb on the seam between two cards takes both). When nothing scores above
 * zero it throws the Orb off the table rather than hurt itself.
 */
object FlipAim {
    /** The AI's error in flick speed (one standard deviation, as a fraction) — about a human's. */
    const val SPEED_SPREAD = 0.1
    /** The AI's error in flick direction (one standard deviation, radians: about 3°). */
    const val ANGLE_SPREAD = 0.05
    /** The AI's error in spin (one standard deviation, turns/s) around the clean band's middle. */
    const val SPIN_SPREAD = 0.8
    private const val OWN_LOSS_WEIGHT = 1.25
    private const val PAIR_REACH = 140.0
    private val ROTATIONS = listOf(-FlipPhysics.CLEAN_SKEW, 0.0, FlipPhysics.CLEAN_SKEW)

    /**
     * [samples] landings per candidate per rotation; fewer for playouts, more for the real move.
     * [hand] draws the release error; by default it is seeded from the decision, so the same
     * decision always gets the same throw.
     */
    fun respond(
        state: GameState,
        decision: FlipOntoBattlefieldDecision,
        playerId: EntityId,
        samples: Int = 16,
        hand: Random = Random(decision.id.hashCode()),
    ): FlipThrowResponse {
        val table = decision.table
        val projected = state.projectedState
        val worth = table.cards.filter { it.eligible }.associate { card ->
            val value = 1.0 + (state.getEntity(card.entityId)?.get<CardComponent>()
                ?.let { BoardPresence.permanentValue(state, projected, card.entityId, it) } ?: 0.0)
            val mine = card.controllerId == playerId || !state.isOpponentTo(card.controllerId, playerId)
            card.entityId to if (mine) -OWN_LOSS_WEIGHT * value else value
        }
        val targets = table.cards.filter { (worth[it.entityId] ?: 0.0) > 0.0 }
        val candidates = targets.map { centre(it) } + targets.flatMapIndexed { i, a ->
            targets.drop(i + 1).mapNotNull { b ->
                val (ax, ay) = centre(a)
                val (bx, by) = centre(b)
                if (hypot(ax - bx, ay - by) <= PAIR_REACH) (ax + bx) / 2 to (ay + by) / 2 else null
            }
        }

        val offsets = spread(samples)
        fun expected(x: Double, y: Double): Double {
            val distance = hypot(x - table.handX, y - table.handY)
            val handSigma = distance * hypot(SPEED_SPREAD, ANGLE_SPREAD)
            val sigma = hypot(FlipPhysics.scatter(distance, FlipPhysics.IDEAL_SPIN), handSigma)
            var total = 0.0
            for ((dx, dy) in offsets) for (rotation in ROTATIONS) {
                val landing = FlipLanding(x + dx * sigma, y + dy * sigma, rotation, halfTurns = 2)
                for (card in table.cards) {
                    val w = worth[card.entityId] ?: continue
                    if (FlipPhysics.overlaps(landing, card)) total += w
                }
            }
            return total / (offsets.size * ROTATIONS.size)
        }

        val best = candidates.map { it to expected(it.first, it.second) }.maxByOrNull { it.second }
        val (x, y) = if (best != null && best.second > 0.0) best.first else table.width / 2 to -2 * table.height
        val (vx, vy) = FlipPhysics.flickToward(table, x, y)
        val speed = hypot(vx, vy) * (1.0 + gaussian(hand) * SPEED_SPREAD)
        val angle = kotlin.math.atan2(vy, vx) + gaussian(hand) * ANGLE_SPREAD
        val spin = FlipPhysics.IDEAL_SPIN + gaussian(hand) * SPIN_SPREAD
        return FlipThrowResponse(decision.id, speed * cos(angle), speed * sin(angle), spin)
    }

    private fun gaussian(random: Random): Double =
        sqrt(-2.0 * kotlin.math.ln(1.0 - random.nextDouble())) * cos(2.0 * PI * random.nextDouble())

    private fun centre(card: FlipTableCard) = card.x to (card.hitTop + card.hitBottom) / 2

    /** A deterministic, roughly unit-normal spread of [n] points (a sunflower over the normal's CDF). */
    private fun spread(n: Int): List<Pair<Double, Double>> = List(n) { i ->
        val u = (i + 0.5) / n
        val r = sqrt(-2.0 * kotlin.math.ln(1.0 - u))
        val angle = i * PI * (3.0 - sqrt(5.0))
        r * cos(angle) to r * sin(angle)
    }
}
