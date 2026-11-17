import { useEffect, useRef, useState } from 'react'
import { useGameStore } from '@/store/gameStore'
import { usePreferences } from '@/store/preferencesStore'
import styles from './TriggerOrderNotice.module.css'

/** Long enough to read two lines and decide; paused while the pointer rests on the notice. */
const NOTICE_MS = 12000
/** How long the "you'll choose from now on" confirmation stays up. */
const CONFIRM_MS = 3200

/**
 * "Your 2 triggers went on the stack in the order they triggered — Got it / Always let me order":
 * a tip shown the first time automatic ordering puts several of the player's triggers on the
 * stack in an order they could have chosen (CR 603.3b), and never again. A player who never opened
 * Preferences would not otherwise learn the order is theirs to choose — in Full Control it always
 * is, so that's the quick switch for the turn that needs it.
 *
 * The abilities are already on the stack, so "Always let me order" applies from the next ones on.
 */
export function TriggerOrderNotice({ topOffset = 0 }: { topOffset?: number }) {
  const notice = useGameStore((s) => s.triggerOrderNotice)
  const dismiss = useGameStore((s) => s.dismissTriggerOrderNotice)
  // The forced-answer notice owns the slot right under the top bar; sit below it while it's up.
  const belowAutoAnswer = useGameStore((s) => s.autoAnswerNotice != null)
  const updatePreferences = usePreferences((s) => s.update)
  const [confirmed, setConfirmed] = useState(false)
  const [hovered, setHovered] = useState(false)
  const timer = useRef<ReturnType<typeof setTimeout> | null>(null)

  useEffect(() => setConfirmed(false), [notice?.key])

  useEffect(() => {
    if (!notice || hovered) return
    timer.current = setTimeout(() => {
      if (useGameStore.getState().triggerOrderNotice?.key === notice.key) dismiss()
    }, confirmed ? CONFIRM_MS : NOTICE_MS)
    return () => {
      if (timer.current) clearTimeout(timer.current)
    }
  }, [notice, hovered, confirmed, dismiss])

  if (!notice) return null

  const top = topOffset + 52 + (belowAutoAnswer ? 40 : 0)

  if (confirmed) {
    return (
      <div role="status" aria-live="polite" className={`${styles.notice} ${styles.done}`} style={{ top }} onClick={dismiss}>
        <span className={styles.icon} aria-hidden>✓</span>
        <span className={styles.text}>
          <span className={styles.headline}>You’ll choose the order from now on</span>
          <span className={styles.detail}>Switch back under Order my triggers automatically in Preferences → Gameplay.</span>
        </span>
      </div>
    )
  }

  const chooseOrder = () => {
    updatePreferences('gameplay', { autoOrderTriggers: false })
    setConfirmed(true)
    setHovered(false)
  }

  return (
    <div
      role="status"
      aria-live="polite"
      className={styles.notice}
      style={{ top }}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      data-testid="trigger-order-notice"
    >
      <span className={styles.icon} aria-hidden>⇅</span>
      <span className={styles.text}>
        <span className={styles.headline}>
          Your {notice.count} triggers went on the stack in the order they triggered
        </span>
        <span className={styles.detail}>
          Switch to Full Control whenever you want to choose the order yourself.
        </span>
      </span>
      <span className={styles.actions}>
        <button type="button" className={styles.secondary} onClick={dismiss}>
          Got it
        </button>
        <button type="button" className={styles.primary} onClick={chooseOrder}>
          Always let me order
        </button>
      </span>
    </div>
  )
}
