package com.wingedsheep.engine.view

import com.wingedsheep.engine.core.*
import com.wingedsheep.engine.state.FACE_DOWN_DISPLAY_NAME
import com.wingedsheep.engine.state.GameState
import com.wingedsheep.sdk.model.EntityId

/**
 * Renders a [PendingDecision] for one viewer, hiding the identity of every face-down object that
 * viewer may not look under.
 *
 * Decisions are built once, from the engine's full state, and some of them reach more than one
 * player: the combat-damage board goes to both choosers, and a selection can offer an opponent's
 * face-down creatures (Stand or Fall's piles). The engine therefore records real names and ids,
 * and every consumer that shows a decision to a player — the game server, a replay, an
 * observation — renders it through here. Each answer comes from [Visibility.cardNameFor], so a
 * reveal, a "look at face-down creatures" static, or CR 708.5's controller access is honoured
 * exactly as the client state honours it.
 *
 * A source marked [DecisionContext.sourceIsPrivate] — a card in a hidden zone the question does
 * not reveal — is masked as well, to anyone who may not see that card: its name is dropped
 * rather than replaced, since there is no face-down label to show for a card in a hand.
 *
 * Masked fields: [com.wingedsheep.engine.core.DecisionContext]`.sourceName` on any decision, the
 * combat board's nodes and prompt, and the `cardInfo` maps. Other prompts are not rewritten: they
 * are built for their chooser, who controls the source they name. A new decision that names an
 * object to a player who may not see it must mask that text here too.
 */
class DecisionMasker(private val visibility: Visibility) {

    fun maskFor(
        decision: PendingDecision,
        state: GameState,
        viewerId: EntityId,
        isSpectator: Boolean = false,
    ): PendingDecision {
        fun hidden(entityId: EntityId) =
            visibility.isCardIdentityHiddenFrom(state, entityId, viewerId, isSpectator)

        fun label(entityId: EntityId) =
            visibility.cardNameFor(state, entityId, viewerId, isSpectator) ?: FACE_DOWN_DISPLAY_NAME

        fun maskInfo(cardInfo: Map<EntityId, SearchCardInfo>?) = cardInfo?.mapValues { (entityId, info) ->
            if (hidden(entityId)) maskedInfo(label(entityId)) else info
        }

        val sourceId = decision.context.sourceId
        val context = when {
            sourceId == null || decision.context.sourceName == null -> decision.context
            hidden(sourceId) -> decision.context.copy(sourceName = label(sourceId))
            isPrivateSourceHiddenFrom(decision, state, viewerId, isSpectator) -> decision.context.copy(sourceName = null)
            else -> decision.context
        }

        return when (decision) {
            is CombatResolutionDecision -> {
                // The single-attacker prompt and source name embed that attacker's name.
                val single = decision.attackers.singleOrNull()
                val singleHidden = single != null && hidden(single.id)
                decision.copy(
                    attackers = decision.attackers.map { if (hidden(it.id)) it.copy(name = label(it.id)) else it },
                    blockers = decision.blockers.map { if (hidden(it.id)) it.copy(name = label(it.id)) else it },
                    prompt = if (singleHidden) decision.prompt.replace(single.name, label(single.id)) else decision.prompt,
                    context = if (singleHidden && context.sourceName == single.name) {
                        context.copy(sourceName = label(single.id))
                    } else {
                        context
                    },
                )
            }
            is SelectCardsDecision -> decision.copy(context = context, cardInfo = maskInfo(decision.cardInfo))
            // Built for the controller, who may look at their own face-down sources; anyone else
            // shown it sees the label and no art.
            is OrderTriggersDecision -> decision.copy(context = context, triggers = decision.triggers.map {
                if (hidden(it.sourceId)) it.copy(sourceName = label(it.sourceId), imageUri = null) else it
            })
            is OrderObjectsDecision -> decision.copy(context = context, cardInfo = maskInfo(decision.cardInfo))
            is SplitPilesDecision -> decision.copy(context = context, cardInfo = maskInfo(decision.cardInfo))
            // Listed one by one, with no `else`, so a new decision type does not compile until
            // someone decides what it shows.
            is ChooseTargetsDecision -> decision.copy(context = context)
            is YesNoDecision -> decision.copy(context = context)
            is BatchYesNoDecision -> decision.copy(context = context)
            is ChooseModeDecision -> decision.copy(context = context)
            is ChooseColorDecision -> decision.copy(context = context)
            is ChooseNumberDecision -> decision.copy(context = context)
            is DistributeDecision -> decision.copy(context = context)
            is ChooseOptionDecision -> decision.copy(context = context)
            is ChooseReplacementDecision -> decision.copy(context = context)
            is AssignDamageDecision -> decision.copy(context = context)
            is SearchLibraryDecision -> decision.copy(context = context)
            is ReorderLibraryDecision -> decision.copy(context = context)
            is SelectManaSourcesDecision -> decision.copy(context = context)
            is BudgetModalDecision -> decision.copy(context = context)
            // Face-down permanents already lie on the table nameless and imageless.
            is FlipOntoBattlefieldDecision -> decision.copy(context = context)
            is PlayCardDecision -> decision.copy(context = context)
        }
    }

    /**
     * Whether [viewerId] may not know what [decision]'s source is: a face-down source they may not
     * look under, or a [DecisionContext.sourceIsPrivate] card they may not see where it lies. A
     * status shown to them then carries none of the source's name, art or id.
     */
    fun isSourceHiddenFrom(
        decision: PendingDecision,
        state: GameState,
        viewerId: EntityId,
        isSpectator: Boolean = false,
    ): Boolean {
        val sourceId = decision.context.sourceId ?: return false
        return visibility.isCardIdentityHiddenFrom(state, sourceId, viewerId, isSpectator) ||
            isPrivateSourceHiddenFrom(decision, state, viewerId, isSpectator)
    }

    private fun isPrivateSourceHiddenFrom(
        decision: PendingDecision,
        state: GameState,
        viewerId: EntityId,
        isSpectator: Boolean,
    ): Boolean {
        val sourceId = decision.context.sourceId
        if (!decision.context.sourceIsPrivate || sourceId == null) return false
        // A source that has left every zone was put somewhere public or ceased to exist.
        val zoneKey = state.zones.entries.firstOrNull { sourceId in it.value }?.key ?: return false
        return !visibility.isCardIdentityVisibleTo(state, zoneKey, sourceId, viewerId, isSpectator)
    }

    /** What a face-down object shows: its label and nothing of the card underneath (CR 708.2a). */
    private fun maskedInfo(label: String) = SearchCardInfo(
        name = label,
        manaCost = "",
        typeLine = if (label == FACE_DOWN_DISPLAY_NAME) "Creature" else "",
        power = if (label == FACE_DOWN_DISPLAY_NAME) 2 else null,
    )
}
