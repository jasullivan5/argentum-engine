import { useState, useMemo } from 'react'
import { useGameStore } from '@/store/gameStore.ts'
import type { ChooseNumberDecision } from '@/types'
import { ManaDistributionUI, parseManaDistributionPrompt } from './ManaDistributionUI'
import { DecisionContextCards, hasDecisionContextCards, resolveDecisionCards } from './DecisionContextCards'
import styles from './DecisionUI.module.css'

/**
 * Choose number decision - select a number from a range.
 * Detects mana distribution prompts and renders a specialized mana UI.
 */
export function ChooseNumberDecisionUI({
  decision,
  onMinimize,
}: {
  decision: ChooseNumberDecision
  onMinimize?: () => void
}) {
  // A two-color mana split gets its own picker.
  const manaDistribution = useMemo(() => parseManaDistributionPrompt(decision.prompt), [decision.prompt])

  if (manaDistribution) {
    return <ManaDistributionUI decision={decision} distribution={manaDistribution} onMinimize={onMinimize} />
  }
  return <NumberPickerUI decision={decision} onMinimize={onMinimize} />
}

/** Default number selection: a button grid for short ranges, a stepper for wide ones. */
function NumberPickerUI({
  decision,
  onMinimize,
}: {
  decision: ChooseNumberDecision
  onMinimize?: (() => void) | undefined
}) {
  const [selectedNumber, setSelectedNumber] = useState(decision.minValue)
  const submitNumberDecision = useGameStore((s) => s.submitNumberDecision)
  const gameState = useGameStore((s) => s.gameState)
  const cards = resolveDecisionCards(decision.context, gameState)

  const clamp = (n: number) => Math.min(decision.maxValue, Math.max(decision.minValue, n))

  const handleConfirm = () => {
    submitNumberDecision(decision.id, clamp(selectedNumber))
  }

  // A short range is fastest to tap as a button grid; a wide range (e.g.
  // "any number", sent as 0–99) becomes an unusable wall of buttons, so we
  // switch to a stepper with direct numeric entry.
  const rangeSize = decision.maxValue - decision.minValue + 1
  const useStepper = rangeSize > 12

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const parsed = parseInt(e.target.value, 10)
    setSelectedNumber(Number.isNaN(parsed) ? decision.minValue : clamp(parsed))
  }

  return (
    <>
      <DecisionContextCards cards={cards} />

      <h2 className={styles.title}>
        {decision.prompt}
      </h2>

      {!hasDecisionContextCards(cards) && decision.context.sourceName && (
        <p className={styles.subtitle}>
          {decision.context.sourceName}
        </p>
      )}

      {useStepper ? (
        <>
          <div className={styles.stepperContainer}>
            <button
              type="button"
              className={styles.stepperButton}
              onClick={() => setSelectedNumber((n) => clamp(n - 1))}
              disabled={selectedNumber <= decision.minValue}
              aria-label="Decrease"
            >
              −
            </button>
            <input
              type="number"
              className={styles.stepperInput}
              min={decision.minValue}
              max={decision.maxValue}
              value={selectedNumber}
              onChange={handleInputChange}
            />
            <button
              type="button"
              className={styles.stepperButton}
              onClick={() => setSelectedNumber((n) => clamp(n + 1))}
              disabled={selectedNumber >= decision.maxValue}
              aria-label="Increase"
            >
              +
            </button>
          </div>
          <p className={styles.stepperRangeHint}>
            {decision.minValue} – {decision.maxValue}
          </p>
        </>
      ) : (
        /* Number selection buttons */
        <div className={styles.numberContainer}>
          {Array.from({ length: rangeSize }, (_, i) => decision.minValue + i).map((num) => (
            <button
              key={num}
              onClick={() => setSelectedNumber(num)}
              className={`${styles.numberButton} ${selectedNumber === num ? styles.numberButtonSelected : ''}`}
            >
              {num}
            </button>
          ))}
        </div>
      )}

      {/* Confirm + optional View Battlefield */}
      <div className={styles.buttonContainer}>
        {onMinimize && (
          <button onClick={onMinimize} className={styles.viewBattlefieldButton}>
            View Battlefield
          </button>
        )}
        <button onClick={handleConfirm} className={styles.confirmButton}>
          Confirm
        </button>
      </div>
    </>
  )
}
