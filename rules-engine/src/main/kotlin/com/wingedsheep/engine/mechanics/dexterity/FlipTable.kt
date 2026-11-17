package com.wingedsheep.engine.mechanics.dexterity

import com.wingedsheep.engine.state.GameState
import com.wingedsheep.engine.state.components.battlefield.AttachedToComponent
import com.wingedsheep.engine.state.components.battlefield.TappedComponent
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.state.components.identity.FaceDownComponent
import com.wingedsheep.engine.state.components.identity.TokenComponent
import com.wingedsheep.sdk.model.EntityId
import kotlinx.serialization.Serializable

/**
 * The battlefield as a physical table, for dexterity flips (Chaos Orb).
 *
 * Paper Magic's dexterity cards read *where cards physically lie*, which a digital battlefield
 * doesn't have. The engine supplies it: every permanent becomes a card-sized rectangle (real card
 * dimensions, in millimetres) laid out the way players lay out a paper battlefield — creatures in
 * front, other permanents behind them, lands at the back, tapped cards turned sideways, Auras and
 * Equipment tucked behind their host with a strip showing. The flipping player sits at the bottom
 * edge; every other player's battlefield is a band across the table from them.
 *
 * The layout is deterministic in the game state, so the decision, the resolution and every client
 * replaying the flip see the same table. Coordinates: x right, y down, origin at the top-left
 * corner of the table; card positions are rectangle centres.
 */
@Serializable
data class FlipTable(
    val width: Double,
    val height: Double,
    /** Each player's area of the table, top to bottom — the flipping player's band is last. */
    val bands: List<FlipTableBand>,
    /** Every permanent on the table, back-to-front draw order (attachments before their hosts). */
    val cards: List<FlipTableCard>,
    /** Where the flipping player's hand is when they let go — just past the bottom edge. */
    val handX: Double,
    val handY: Double,
)

/** One player's area of the table. */
@Serializable
data class FlipTableBand(
    val playerId: EntityId,
    val top: Double,
    val height: Double,
)

/**
 * One permanent lying on the table. [width]/[height] are its footprint after tapping (a tapped
 * card is turned sideways). [hitTop]..[hitBottom] narrow the touchable part of an attachment to the
 * strip that shows from under its host; for every other card it's the whole rectangle.
 *
 * [name]/[imageUri] are what anyone at the table can see: null for a face-down permanent.
 * [eligible] is whether the flip's filter counts this permanent ("nontoken permanents").
 */
@Serializable
data class FlipTableCard(
    val entityId: EntityId,
    val controllerId: EntityId,
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
    val tapped: Boolean,
    val eligible: Boolean,
    val isToken: Boolean = false,
    val faceDown: Boolean = false,
    val attachedTo: EntityId? = null,
    val hitTop: Double = y - height / 2,
    val hitBottom: Double = y + height / 2,
    val name: String? = null,
    val imageUri: String? = null,
)

/**
 * Where a flipped card came to rest. [halfTurns] is how many half rotations it made in the air
 * (CR has no rule for dexterity; the card's own text is the rule: it must "turn over completely at
 * least once"), so [turnedOver] is `halfTurns >= 2` and an odd count lands it face down.
 * [rotation] is its resting angle on the table in degrees. [x]/[y] may lie off the table — a flip
 * that sails past the edge lands on the floor and touches nothing.
 */
@Serializable
data class FlipLanding(
    val x: Double,
    val y: Double,
    val rotation: Double,
    val halfTurns: Int,
    /** How fast (mm/s) the flick left the hand. */
    val speed: Double = 0.0,
    /** How fast (turns/s) it was spinning. */
    val spin: Double = 0.0,
    /** How far (mm) an over-spun card skidded on along its line after it came down. */
    val skid: Double = 0.0,
) {
    val turnedOver: Boolean get() = halfTurns >= 2
}

object FlipTableLayout {
    const val CARD_WIDTH = 63.0
    const val CARD_HEIGHT = 88.0
    private const val GAP = 10.0
    private const val ROW_GAP = 16.0
    private const val MARGIN = 34.0
    private const val BAND_GAP = 36.0
    private const val MAX_ROW_WIDTH = 780.0
    private const val MIN_TABLE_WIDTH = 760.0
    private const val MIN_BAND_HEIGHT = 2 * CARD_HEIGHT + ROW_GAP + 2 * MARGIN

    /** How much of an Aura or Equipment shows from behind its host. */
    const val ATTACHMENT_PEEK = 22.0

    /** Distance from the table's bottom edge to the flipping player's hand. */
    private const val HAND_OFFSET = 70.0

    private enum class RowKind { CREATURES, OTHERS, LANDS }

    private data class Footprint(val id: EntityId, val w: Double, val h: Double, val attachments: List<EntityId>)

    private data class Placed(val footprint: Footprint, val x: Double, val y: Double)

    /**
     * Lay out the battlefield for [flipperId]'s flip. [exclude] is the card being flipped (it is in
     * the air, not on the table). [isEligible] decides each permanent's [FlipTableCard.eligible].
     */
    fun build(
        state: GameState,
        flipperId: EntityId,
        exclude: EntityId?,
        isEligible: (EntityId) -> Boolean,
    ): FlipTable {
        val projected = state.projectedState
        val onTable = state.getBattlefield().filter { it != exclude }
        val onTableSet = onTable.toSet()

        // Attachments ride with their host; one whose host isn't on the table lies on its own.
        val attachmentsByHost = onTable
            .mapNotNull { id ->
                val host = state.getEntity(id)?.get<AttachedToComponent>()?.targetId
                if (host != null && host in onTableSet && host != id) host to id else null
            }
            .groupBy({ it.first }, { it.second })
        val attached = attachmentsByHost.values.flatten().toSet()

        val playerOrder = run {
            val order = state.turnOrder.ifEmpty { onTable.mapNotNull { projected.getController(it) }.distinct() }
            val start = order.indexOf(flipperId).coerceAtLeast(0)
            // Opponents in turn order after the flipper, then the flipper's own band at the bottom.
            (order.drop(start + 1) + order.take(start)).filter { it != flipperId } + flipperId
        }

        fun footprint(id: EntityId): Footprint {
            val tapped = state.getEntity(id)?.has<TappedComponent>() == true
            return Footprint(
                id = id,
                w = if (tapped) CARD_HEIGHT else CARD_WIDTH,
                h = if (tapped) CARD_WIDTH else CARD_HEIGHT,
                attachments = attachmentsDeep(id, attachmentsByHost),
            )
        }

        fun rowKind(id: EntityId): RowKind = when {
            projected.isCreature(id) -> RowKind.CREATURES
            projected.hasType(id, "LAND") -> RowKind.LANDS
            else -> RowKind.OTHERS
        }

        // Rows per player, nearest the centre line first.
        val rowsByPlayer: Map<EntityId, List<List<Footprint>>> = playerOrder.associateWith { player ->
            val mine = onTable.filter { it !in attached && (projected.getController(it) ?: flipperId) == player }
            RowKind.entries.flatMap { kind ->
                wrap(mine.filter { rowKind(it) == kind }.map(::footprint))
            }
        }

        val widest = rowsByPlayer.values.flatten().maxOfOrNull(::rowWidth) ?: 0.0
        val width = maxOf(MIN_TABLE_WIDTH, widest + 2 * MARGIN)

        val bands = mutableListOf<FlipTableBand>()
        val cards = mutableListOf<FlipTableCard>()
        var top = 0.0
        for ((index, player) in playerOrder.withIndex()) {
            val isFlipper = player == flipperId
            val rows = rowsByPlayer.getValue(player)
            val rowHeights = rows.map(::rowHeight)
            val content = rowHeights.sum() + ROW_GAP * (rows.size - 1).coerceAtLeast(0)
            val bandHeight = maxOf(MIN_BAND_HEIGHT, content + 2 * MARGIN)
            bands += FlipTableBand(player, top, bandHeight)

            // The flipper's front row sits at the top of their band (nearest the centre line);
            // an opponent's sits at the bottom of theirs, so creatures face each other.
            val placedRows = mutableListOf<Pair<List<Footprint>, Double>>() // row to its centre y
            var cursor = if (isFlipper) top + MARGIN else top + bandHeight - MARGIN
            for ((rowIndex, row) in rows.withIndex()) {
                val h = rowHeights[rowIndex]
                val centre = if (isFlipper) cursor + h / 2 else cursor - h / 2
                placedRows += row to centre
                cursor = if (isFlipper) cursor + h + ROW_GAP else cursor - h - ROW_GAP
            }

            for ((row, rowCentre) in placedRows) {
                // Hosts in this row sit at the row's front edge, attachments peek out behind them.
                val peekDepth = row.maxOf { it.attachments.size } * ATTACHMENT_PEEK
                val backDir = if (isFlipper) 1.0 else -1.0
                var x = (width - rowWidth(row)) / 2
                for (fp in row) {
                    val cx = x + fp.w / 2
                    val cy = rowCentre - backDir * peekDepth / 2
                    // Deepest attachment first, so hosts draw on top.
                    for ((depth, attachmentId) in fp.attachments.withIndex().reversed()) {
                        val ay = cy + backDir * ATTACHMENT_PEEK * (depth + 1)
                        val aw = CARD_WIDTH
                        val ah = CARD_HEIGHT
                        // The strip that shows: the attachment's back edge, ATTACHMENT_PEEK deep.
                        val back = ay + backDir * ah / 2
                        val stripTop = minOf(back, back - backDir * ATTACHMENT_PEEK)
                        val stripBottom = maxOf(back, back - backDir * ATTACHMENT_PEEK)
                        cards += card(state, attachmentId, player, cx, ay, aw, ah, isEligible, attachedTo = fp.id)
                            .copy(hitTop = stripTop, hitBottom = stripBottom)
                    }
                    cards += card(state, fp.id, player, cx, cy, fp.w, fp.h, isEligible)
                    x += fp.w + GAP
                }
            }
            top += bandHeight + if (index < playerOrder.lastIndex) BAND_GAP else 0.0
        }

        return FlipTable(
            width = width,
            height = top,
            bands = bands,
            cards = cards,
            handX = width / 2,
            handY = top + HAND_OFFSET,
        )
    }

    private fun attachmentsDeep(host: EntityId, byHost: Map<EntityId, List<EntityId>>): List<EntityId> {
        val out = mutableListOf<EntityId>()
        val seen = mutableSetOf(host)
        fun visit(id: EntityId) {
            for (a in byHost[id].orEmpty()) if (seen.add(a)) { out += a; visit(a) }
        }
        visit(host)
        return out
    }

    private fun card(
        state: GameState,
        id: EntityId,
        controller: EntityId,
        x: Double,
        y: Double,
        w: Double,
        h: Double,
        isEligible: (EntityId) -> Boolean,
        attachedTo: EntityId? = null,
    ): FlipTableCard {
        val entity = state.getEntity(id)
        val faceDown = entity?.has<FaceDownComponent>() == true
        val cardComponent = entity?.get<CardComponent>()
        return FlipTableCard(
            entityId = id,
            controllerId = controller,
            x = x,
            y = y,
            width = w,
            height = h,
            tapped = entity?.has<TappedComponent>() == true,
            eligible = isEligible(id),
            isToken = entity?.has<TokenComponent>() == true,
            faceDown = faceDown,
            attachedTo = attachedTo,
            name = if (faceDown) null else cardComponent?.name,
            imageUri = if (faceDown) null else cardComponent?.imageUri,
        )
    }

    private fun rowWidth(row: List<Footprint>): Double =
        if (row.isEmpty()) 0.0 else row.sumOf { it.w } + GAP * (row.size - 1)

    private fun rowHeight(row: List<Footprint>): Double =
        (row.maxOfOrNull { it.h } ?: CARD_HEIGHT) + row.maxOf { it.attachments.size } * ATTACHMENT_PEEK

    private fun wrap(cards: List<Footprint>): List<List<Footprint>> {
        if (cards.isEmpty()) return emptyList()
        val rows = mutableListOf<MutableList<Footprint>>()
        var current = mutableListOf<Footprint>()
        var w = 0.0
        for (c in cards) {
            val added = if (current.isEmpty()) c.w else w + GAP + c.w
            if (current.isNotEmpty() && added > MAX_ROW_WIDTH) {
                rows += current
                current = mutableListOf(c)
                w = c.w
            } else {
                current += c
                w = added
            }
        }
        rows += current
        return rows
    }
}
