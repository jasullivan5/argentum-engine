import { useState } from 'react'
import { useGameStore } from '@/store/gameStore.ts'
import type { YesNoDecision, ClientGameState } from '@/types'
import { AbilityText } from '../ui/ManaSymbols'
import { DecisionContextCards, hasDecisionContextCards, resolveDecisionCards } from './DecisionContextCards'
import styles from './DecisionUI.module.css'

/** Tooltip on a "yes" the server marked impossible. */
const NOT_POSSIBLE = 'Not possible right now'

/**
 * Yes/No decision - make a binary choice.
 *
 * Shows the source card (the ability owner), the triggering entity when there is one, and — for a
 * prompt raised once per object (Killing Wave asks "pay X life or sacrifice it" for each of your
 * creatures in turn) — the subject this instance is about.
 *
 * Supports an optional hint (e.g., keyword reminder text) and a View Battlefield button.
 *
 * A pure "you may" of a card ability (`canRememberAnswer`) also offers "remember my answer": the
 * answer is submitted as usual and set as an Always Yes / Always No yield on the ability, so every
 * later copy resolves without asking. Revocable from the stack's yield menu or the Active yields
 * panel.
 */
export function YesNoDecisionUI({
  decision,
  gameState,
  onMinimize,
}: {
  decision: YesNoDecision
  gameState: ClientGameState | null
  onMinimize?: () => void
}) {
  const submitYesNoDecision = useGameStore((s) => s.submitYesNoDecision)
  // The server asks even when "yes" is impossible, so the question doesn't reveal hidden cards;
  // then "no" is the only answer and "yes" is shown disabled.
  const yesAvailable = decision.yesAvailable !== false
  const setAbilityYield = useGameStore((s) => s.setAbilityYield)
  const identity = decision.canRememberAnswer ? decision.context.abilityIdentity : null
  const [remember, setRemember] = useState(false)

  const answer = (choice: boolean) => {
    // The yield goes first on the same socket, so it is in force before the answer is processed.
    if (remember && identity) {
      setAbilityYield(identity.cardDefinitionId, identity.abilityId, choice ? 'ALWAYS_ANSWER_YES' : 'ALWAYS_ANSWER_NO')
    }
    submitYesNoDecision(decision.id, choice)
  }

  const handleYes = () => {
    if (yesAvailable) answer(true)
  }

  const handleNo = () => {
    answer(false)
  }

  const cards = resolveDecisionCards(decision.context, gameState)
  const showCardContext = hasDecisionContextCards(cards)

  return (
    <>
      {/* "Cast it transformed" (a Siege's defeat trigger) offers the face the source card hides. */}
      <DecisionContextCards cards={cards} showSourceBackFace={/\bcast it transformed\b/i.test(decision.prompt)} />

      <h2 className={styles.title}>
        <AbilityText text={decision.prompt} size={20} />
      </h2>

      {!showCardContext && decision.context.sourceName && (
        <p className={styles.subtitle}>
          {decision.context.sourceName}
        </p>
      )}

      {/* Hint text (e.g., keyword reminder text) */}
      {decision.hint && (
        <p className={styles.hint}>
          <AbilityText text={decision.hint} size={14} />
        </p>
      )}

      {decision.peerOptions ? (
        <>
          {/* Two outcomes of equal standing (endure: counters or a Spirit) — neither is "decline". */}
          <div className={styles.peerOptions}>
            <PeerOption label={decision.yesText} detail={decision.yesDetail} onClick={handleYes} disabled={!yesAvailable} />
            <span className={styles.peerOptionsOr} aria-hidden="true">or</span>
            <PeerOption label={decision.noText} detail={decision.noDetail} onClick={handleNo} />
          </div>
          {onMinimize && (
            <div className={styles.buttonContainer}>
              <button onClick={onMinimize} className={styles.viewBattlefieldButton}>
                View Battlefield
              </button>
            </div>
          )}
        </>
      ) : (
        /* Yes/No buttons + optional View Battlefield */
        <div className={styles.buttonContainer}>
          {onMinimize && (
            <button onClick={onMinimize} className={styles.viewBattlefieldButton}>
              View Battlefield
            </button>
          )}
          <button onClick={handleYes} className={styles.yesButton} disabled={!yesAvailable} title={yesAvailable ? undefined : NOT_POSSIBLE}>
            <AbilityText text={decision.yesText} size={16} />
          </button>
          <button onClick={handleNo} className={styles.noButton}>
            <AbilityText text={decision.noText} size={16} />
          </button>
        </div>
      )}

      {identity && (
        <label className={`${styles.rememberAnswer} ${remember ? styles.rememberAnswerOn : ''}`}>
          <input type="checkbox" checked={remember} onChange={(e) => setRemember(e.target.checked)} />
          <span>
            Remember my answer — don’t ask again{decision.context.sourceName ? ` for ${decision.context.sourceName}` : ''}
            <span className={styles.rememberAnswerHint}>
              Every copy of this ability gets the same answer for the rest of the game. Undo it from Active yields.
            </span>
          </span>
        </label>
      )}
    </>
  )
}

function PeerOption({ label, detail, onClick, disabled = false }: { label: string; detail?: string | null | undefined; onClick: () => void; disabled?: boolean }) {
  return (
    <button onClick={onClick} className={styles.peerOption} disabled={disabled} title={disabled ? NOT_POSSIBLE : undefined}>
      <span className={styles.peerOptionLabel}>
        <AbilityText text={label} size={18} />
      </span>
      {detail && <span className={styles.peerOptionDetail}>{detail}</span>}
    </button>
  )
}
