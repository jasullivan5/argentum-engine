/**
 * Shared deck-stats panel used by the deckbuilder right rail and the DeckPicker
 * (quick-game / tournament Custom Decks lobbies). Renders card count, legality,
 * colour pips, and a stacked-by-colour mana curve.
 */
import styles from './DeckSummary.module.css'

export interface DeckValidationIssue {
  code: string
  message: string
  cardName: string | null
}

/** A points-list format's tally (Canadian Highlander): points spent of the cap, per card. */
export interface DeckPoints {
  total: number
  cap: number
  /** Card name → points it costs in this deck. Only cards that cost points appear. */
  byCard: Record<string, number>
}

export interface DeckValidationResult {
  valid: boolean
  totalCards: number
  errors: DeckValidationIssue[]
  warnings: DeckValidationIssue[]
  /** Present only for a format with a points list. */
  points?: DeckPoints | null
}

/** "7 / 10 points", or null when the format has no points list. */
export function pointsLabel(v: DeckValidationResult | null): string | null {
  const p = v?.points
  return p ? `${p.total} / ${p.cap} points` : null
}

export function pointsClass(v: DeckValidationResult | null): string {
  const p = v?.points
  return p && p.total > p.cap ? styles.statusBad! : styles.statusPoints!
}

/** Minimal card shape `computeDeckStats` needs. Both `cardFilter.CardSummary` and the
 * picker's own CardSummary structurally satisfy this. */
export interface DeckStatsCard {
  cmc: number
  colors: string[]
  cardTypes: string[]
}

export interface DeckStats {
  colorCounts: Array<[string, number]>
  curve: number[]
  curveByColor: Array<Record<string, number>>
}

export const COLOR_DOT: Record<string, string> = {
  WHITE: '#f5f3da',
  BLUE: '#62a8ff',
  BLACK: '#3a3a3a',
  RED: '#ff6a4a',
  GREEN: '#4ab86a',
}

const CURVE_COLOR_ORDER = ['WHITE', 'BLUE', 'BLACK', 'RED', 'GREEN', 'COLORLESS']

const CURVE_COLOR_HEX: Record<string, string> = {
  WHITE: '#f5f3da',
  BLUE: '#62a8ff',
  BLACK: '#5a5a5a',
  RED: '#ff6a4a',
  GREEN: '#4ab86a',
  COLORLESS: '#9aa3b2',
}

export function statusClass(v: DeckValidationResult | null, total: number): string {
  if (total === 0) return styles.statusEmpty!
  if (!v) return styles.statusEmpty!
  return v.valid ? styles.statusOk! : styles.statusBad!
}

export function statusLabel(v: DeckValidationResult | null, total: number): string {
  if (total === 0) return 'Empty'
  if (!v) return 'Validating…'
  if (v.valid) return 'Legal ✓'
  return `${v.errors.length} issue${v.errors.length === 1 ? '' : 's'}`
}

export function computeDeckStats(
  deck: Record<string, number>,
  cards: Record<string, DeckStatsCard>,
): DeckStats {
  const colorCount: Record<string, number> = {}
  const curve = [0, 0, 0, 0, 0, 0, 0, 0]
  const curveByColor: Array<Record<string, number>> = Array.from(
    { length: curve.length },
    () => ({}),
  )
  for (const [name, count] of Object.entries(deck)) {
    if (count <= 0) continue
    const c = cards[name.split('#')[0] ?? name]
    if (!c) continue
    for (const col of c.colors) colorCount[col] = (colorCount[col] ?? 0) + count
    if (!c.cardTypes.includes('LAND')) {
      const idx = Math.min(c.cmc, curve.length - 1)
      curve[idx] = (curve[idx] ?? 0) + count
      // Multicolor cards split their count across each contributing color so the stacked
      // segments add up to the total bar height. Colorless cards go in their own bucket.
      const cs = c.colors.length === 0 ? ['COLORLESS'] : c.colors
      const share = count / cs.length
      for (const col of cs) {
        curveByColor[idx]![col] = (curveByColor[idx]![col] ?? 0) + share
      }
    }
  }
  return {
    colorCounts: Object.entries(colorCount).sort((a, b) => b[1] - a[1]),
    curve,
    curveByColor,
  }
}

export function ManaCurveBars({
  curve,
  curveByColor,
}: {
  curve: number[]
  curveByColor: Array<Record<string, number>>
}) {
  const max = Math.max(1, ...curve)
  return (
    <div className={styles.curveContainer}>
      <div className={styles.curveBars}>
        {curve.map((n, i) => {
          const segments = CURVE_COLOR_ORDER
            .map((col) => ({ col, share: curveByColor[i]?.[col] ?? 0 }))
            .filter((s) => s.share > 0)
          const cmcLabel = i === curve.length - 1 ? `${i}+` : `${i}`
          return (
            <div key={i} className={styles.curveBarColumn}>
              <div className={styles.curveBarCount}>{n > 0 ? n : ''}</div>
              <div
                className={styles.curveBarTrack}
                title={`CMC ${cmcLabel}: ${n}`}
              >
                <div
                  className={styles.curveBarStack}
                  style={{ height: `${(n / max) * 100}%` }}
                >
                  {segments.map(({ col, share }) => (
                    <div
                      key={col}
                      className={styles.curveBarSegment}
                      style={{
                        flex: share,
                        background: CURVE_COLOR_HEX[col] ?? '#888',
                      }}
                    />
                  ))}
                </div>
              </div>
              <div className={styles.curveLabel}>{cmcLabel}</div>
            </div>
          )
        })}
      </div>
    </div>
  )
}

export function DeckSummary({
  validation,
  totalCards,
  stats,
  compactOnSmallScreens = false,
}: {
  validation: DeckValidationResult | null
  totalCards: number
  stats: DeckStats
  /** Drop the curve chart on phones and short screens, where the summary shares a dialog. */
  compactOnSmallScreens?: boolean
}) {
  return (
    <div className={styles.summary} data-compact={compactOnSmallScreens || undefined}>
      <div className={styles.summaryRow}>
        <span>{totalCards} cards</span>
        <span className={statusClass(validation, totalCards)}>
          {statusLabel(validation, totalCards)}
        </span>
      </div>

      {pointsLabel(validation) && (
        <div className={styles.summaryRow}>
          <span>Points</span>
          <span className={pointsClass(validation)}>{pointsLabel(validation)}</span>
        </div>
      )}

      {validation && validation.errors.length > 0 && (
        <ul className={styles.issues}>
          {validation.errors.slice(0, 6).map((e, i) => (
            <li key={i}>• {e.message}</li>
          ))}
          {validation.errors.length > 6 && <li>+{validation.errors.length - 6} more…</li>}
        </ul>
      )}

      {totalCards > 0 && stats.colorCounts.length > 0 && (
        <>
          <div className={styles.summaryRow}>
            <span>Colours</span>
            <span className={styles.colorPips}>
              {stats.colorCounts.map(([color, n]) => (
                <span key={color} className={styles.colorPip} title={`${color}: ${n}`}>
                  <span className={styles.colorDot} style={{ background: COLOR_DOT[color] ?? '#888' }} />
                  {n}
                </span>
              ))}
            </span>
          </div>
          <ManaCurveBars curve={stats.curve} curveByColor={stats.curveByColor} />
        </>
      )}
    </div>
  )
}
