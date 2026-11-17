import { useState } from 'react'
import { useGameStore } from '@/store/gameStore.ts'
import type { ChooseNumberDecision } from '@/types'
import { ManaSymbol } from '../ui/ManaSymbols'
import { DecisionContextCards, resolveDecisionCards } from './DecisionContextCards'
import styles from './DecisionUI.module.css'
import manaStyles from './ManaDistributionUI.module.css'

/** A two-color "add X mana in any combination of {A} and/or {B}" split, read off the prompt. */
export interface ManaDistribution {
  readonly firstColor: string
  readonly secondColor: string
  readonly total: number
  /** Mana each chosen unit becomes (Nyxbloom Ancient); 1 when nothing multiplies it. */
  readonly unitMultiplier: number
}

/**
 * Parse the engine's two-color split prompt (AddDynamicManaExecutor). Returns null for any other
 * number prompt. Expected format:
 * "Choose how much {G} mana to add (rest will be {R}). Total: 3[ — each becomes 3 mana of that color]"
 */
export function parseManaDistributionPrompt(prompt: string): ManaDistribution | null {
  const match = prompt.match(/Choose how much \{(\w+)\} mana to add \(rest will be \{(\w+)\}\)\. Total: (\d+)/)
  if (!match?.[1] || !match[2] || !match[3]) return null
  const multiplier = prompt.match(/each becomes (\d+) mana/)
  return {
    firstColor: match[1],
    secondColor: match[2],
    total: parseInt(match[3], 10),
    unitMultiplier: multiplier?.[1] ? parseInt(multiplier[1], 10) : 1,
  }
}

/** Accent per mana symbol — tints each color's panel, pips and its half of the split bar. */
const MANA_TINT: Record<string, string> = {
  W: '#e8dfb8',
  U: '#3d8bd4',
  B: '#9a8a9e',
  R: '#d9493c',
  G: '#3fa05f',
  C: '#b0b0b8',
}

const tintOf = (symbol: string) => MANA_TINT[symbol] ?? '#888'

const COLOR_NAME: Record<string, string> = {
  W: 'white', U: 'blue', B: 'black', R: 'red', G: 'green', C: 'colorless',
}

const nameOf = (symbol: string) => COLOR_NAME[symbol] ?? symbol

/** Pips shrink as the total grows so a big Vivi still fits on one or two rows. */
const pipSize = (total: number) => (total <= 8 ? 30 : total <= 14 ? 24 : 18)

/**
 * Split X mana between two colors. Each color gets a panel with its own −/+ stepper, a pip
 * strip previews exactly what will be added (click a pip to make the split there), and the
 * two-tone slider runs the same way as the panels: the left share is the left color.
 */
export function ManaDistributionUI({
  decision,
  distribution,
  onMinimize,
}: {
  decision: ChooseNumberDecision
  distribution: ManaDistribution
  onMinimize?: (() => void) | undefined
}) {
  const { firstColor, secondColor, total, unitMultiplier } = distribution
  // Start at an even split: a mixed default shows at a glance that the colors combine.
  const [firstAmount, setFirstAmount] = useState(Math.ceil(total / 2))
  const submitNumberDecision = useGameStore((s) => s.submitNumberDecision)
  const gameState = useGameStore((s) => s.gameState)
  const cards = resolveDecisionCards(decision.context, gameState)

  const clamp = (n: number) => Math.min(total, Math.max(0, n))
  const setFirst = (n: number) => setFirstAmount(clamp(n))
  const secondAmount = total - firstAmount
  const even = Math.ceil(total / 2)
  const firstPct = total === 0 ? 50 : (firstAmount / total) * 100

  // Clicking pip i paints the split there; clicking the last first-color pip again un-paints it,
  // so a single pip can always be flipped either way.
  const handlePipClick = (index: number) => {
    setFirst(index + 1 === firstAmount ? index : index + 1)
  }

  const colorPanel = (symbol: string, amount: number, onChange: (delta: number) => void) => (
    <div
      className={`${manaStyles.panel} ${amount === 0 ? manaStyles.panelEmpty : ''}`}
      style={{ '--mana-tint': tintOf(symbol) } as React.CSSProperties}
    >
      <ManaSymbol symbol={symbol} size={36} />
      <div className={manaStyles.stepper}>
        <button
          type="button"
          className={manaStyles.stepButton}
          onClick={() => onChange(-1)}
          disabled={amount === 0}
          aria-label={`Less ${nameOf(symbol)}`}
        >
          −
        </button>
        <span className={manaStyles.count} aria-live="polite">
          {amount * unitMultiplier}
        </span>
        <button
          type="button"
          className={manaStyles.stepButton}
          onClick={() => onChange(1)}
          disabled={amount === total}
          aria-label={`More ${nameOf(symbol)}`}
        >
          +
        </button>
      </div>
    </div>
  )

  const confirmPart = (symbol: string, amount: number) =>
    amount > 0 && (
      <span className={manaStyles.confirmPart}>
        {amount * unitMultiplier}
        <ManaSymbol symbol={symbol} size={18} />
      </span>
    )

  return (
    <>
      <DecisionContextCards cards={cards} />
      <h2 className={styles.title}>
        {decision.context.sourceName ?? 'Add mana'}
      </h2>
      <p className={manaStyles.subtitle}>
        Add {total} mana in any combination of <ManaSymbol symbol={firstColor} size={16} /> and
        {' '}<ManaSymbol symbol={secondColor} size={16} />
      </p>
      {unitMultiplier > 1 && (
        <p className={manaStyles.multiplierNote}>Each mana you choose becomes {unitMultiplier}.</p>
      )}

      <div className={manaStyles.board}>
        <div className={manaStyles.panels}>
          {colorPanel(firstColor, firstAmount, (d) => setFirst(firstAmount + d))}
          {colorPanel(secondColor, secondAmount, (d) => setFirst(firstAmount - d))}
        </div>

        <div className={manaStyles.pips} role="group" aria-label="Mana to add">
          {Array.from({ length: total }, (_, i) => {
            const symbol = i < firstAmount ? firstColor : secondColor
            return (
              <button
                key={i}
                type="button"
                className={manaStyles.pip}
                onClick={() => handlePipClick(i)}
                                aria-label={`${i + 1} ${nameOf(firstColor)}, rest ${nameOf(secondColor)}`}
              >
                <ManaSymbol symbol={symbol} size={pipSize(total)} />
              </button>
            )
          })}
        </div>

        <input
          type="range"
          min={0}
          max={total}
          step={1}
          value={firstAmount}
          onChange={(e) => setFirst(parseInt(e.target.value, 10))}
          className={manaStyles.slider}
          style={{
            background: `linear-gradient(to right, ${tintOf(firstColor)} 0%, ${tintOf(firstColor)} ${firstPct}%, ${tintOf(secondColor)} ${firstPct}%, ${tintOf(secondColor)} 100%)`,
          }}
          aria-label={`Amount of ${nameOf(firstColor)} mana; the rest is ${nameOf(secondColor)}`}
        />

        <div className={manaStyles.presets}>
          <button
            type="button"
            className={`${manaStyles.preset} ${firstAmount === total ? manaStyles.presetActive : ''}`}
            onClick={() => setFirst(total)}
          >
            All <ManaSymbol symbol={firstColor} size={14} />
          </button>
          {total > 1 && (
            <button
              type="button"
              className={`${manaStyles.preset} ${firstAmount === even ? manaStyles.presetActive : ''}`}
              onClick={() => setFirst(even)}
            >
              Even
            </button>
          )}
          <button
            type="button"
            className={`${manaStyles.preset} ${firstAmount === 0 ? manaStyles.presetActive : ''}`}
            onClick={() => setFirst(0)}
          >
            All <ManaSymbol symbol={secondColor} size={14} />
          </button>
        </div>
      </div>

      <div className={styles.buttonContainer}>
        {onMinimize && (
          <button onClick={onMinimize} className={styles.viewBattlefieldButton}>
            View Battlefield
          </button>
        )}
        <button
          onClick={() => submitNumberDecision(decision.id, firstAmount)}
          className={`${styles.confirmButton} ${manaStyles.confirmButton}`}
        >
          Add {confirmPart(firstColor, firstAmount)}
          {firstAmount > 0 && secondAmount > 0 && <span className={manaStyles.confirmPlus}>+</span>}
          {confirmPart(secondColor, secondAmount)}
        </button>
      </div>
    </>
  )
}
