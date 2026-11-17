import type React from 'react'
import { useEffect, useLayoutEffect, useRef, useState } from 'react'
import { useGameStore } from '@/store/gameStore.ts'
import type { ClientAbilityIdentity, ClientYield } from '@/types/gameState'
import type { YieldKind } from '@/types'
import { YIELD_TONE } from '../yields'

/**
 * The yield menu for a stack ability, mirroring MTGO's persistent-yield options (backlog §C). Lets
 * the controller set "auto-pass this turn / auto-pass all game / always yes / always no" against
 * an ability, keyed by its {@link ClientAbilityIdentity} so the preference follows every current
 * and future copy. Opened by right-click on any stack ability, or from the "Yield" button under the
 * top of the stack. The server owns the authoritative yield state.
 */
export function YieldContextMenu({
  identity,
  sourceName,
  position,
  existing,
  ownAbility,
  onClose,
}: {
  identity: ClientAbilityIdentity
  sourceName: string
  /** Where the menu's top-left corner wants to sit; it is nudged back inside the viewport. */
  position: { x: number; y: number }
  /** The current yield on this ability, if any — drives the active marks + revoke row. */
  existing: ClientYield | undefined
  /** The viewer controls it, so its "you may" is theirs to answer. Otherwise only auto-pass applies. */
  ownAbility: boolean
  onClose: () => void
}) {
  const setAbilityYield = useGameStore((s) => s.setAbilityYield)
  const clearAbilityYield = useGameStore((s) => s.clearAbilityYield)
  const menuRef = useRef<HTMLDivElement>(null)
  const [placed, setPlaced] = useState(position)

  // Measure, then keep the whole menu on screen with an 8px margin.
  useLayoutEffect(() => {
    const el = menuRef.current
    if (!el) return
    const { width, height } = el.getBoundingClientRect()
    setPlaced({
      x: Math.max(8, Math.min(position.x, window.innerWidth - width - 8)),
      y: Math.max(8, Math.min(position.y, window.innerHeight - height - 8)),
    })
  }, [position.x, position.y])

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose() }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [onClose])

  const apply = (kind: YieldKind) => {
    setAbilityYield(identity.cardDefinitionId, identity.abilityId, kind)
    onClose()
  }

  return (
    <>
      {/* Click-away backdrop */}
      <div
        style={{ position: 'fixed', inset: 0, zIndex: 999 }}
        onClick={onClose}
        onContextMenu={(e) => { e.preventDefault(); onClose() }}
      />
      <div
        ref={menuRef}
        role="menu"
        aria-label={`Yields for ${sourceName}`}
        style={{
          position: 'fixed',
          left: placed.x,
          top: placed.y,
          zIndex: 1000,
          width: 264,
          maxWidth: 'calc(100vw - 16px)',
          background: 'rgba(18, 14, 28, 0.98)',
          border: '1px solid rgba(150, 100, 200, 0.5)',
          borderRadius: 10,
          boxShadow: '0 10px 28px rgba(0, 0, 0, 0.6)',
          overflow: 'hidden',
          fontSize: 12,
          color: '#e0d4f0',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <div style={{ padding: '10px 12px 8px', borderBottom: '1px solid rgba(150,100,200,0.25)' }}>
          <div style={{ fontWeight: 700, fontSize: 13, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
            {sourceName}
          </div>
          <div style={{ color: '#9c8cb4', fontSize: 11, marginTop: 2 }}>
            Stop being asked about this ability. Covers every copy of it.
          </div>
        </div>

        <SectionLabel>When it’s on the stack</SectionLabel>
        <MenuItem
          label="Auto-pass this turn"
          detail="Don’t stop for it again until end of turn"
          tone="pass"
          active={existing?.untilEndOfTurn ?? false}
          onClick={() => apply('YIELD_UNTIL_END_OF_TURN')}
        />
        <MenuItem
          label="Auto-pass all game"
          detail="Never stop to respond to it"
          tone="pass"
          active={existing?.wholeGame ?? false}
          onClick={() => apply('YIELD_WHOLE_GAME')}
        />

        {(ownAbility || existing?.autoAnswer != null) && (
          <>
            <SectionLabel>When it asks “you may…”</SectionLabel>
            <MenuItem
              label="Always answer Yes"
              detail="Your optional choice, answered for you"
              tone="yes"
              active={existing?.autoAnswer === true}
              onClick={() => apply('ALWAYS_ANSWER_YES')}
            />
            <MenuItem
              label="Always answer No"
              detail="Decline it without a prompt"
              tone="no"
              active={existing?.autoAnswer === false}
              onClick={() => apply('ALWAYS_ANSWER_NO')}
            />
          </>
        )}

        {existing && (
          <button
            type="button"
            role="menuitem"
            onClick={() => { clearAbilityYield(identity.cardDefinitionId, identity.abilityId); onClose() }}
            style={{
              display: 'block',
              width: '100%',
              marginTop: 4,
              padding: '9px 12px',
              background: 'transparent',
              border: 'none',
              borderTop: '1px solid rgba(150,100,200,0.25)',
              color: '#ff9a8c',
              cursor: 'pointer',
              textAlign: 'left',
              font: 'inherit',
              fontWeight: 600,
            }}
            onMouseEnter={(e) => { e.currentTarget.style.background = 'rgba(200, 80, 70, 0.16)' }}
            onMouseLeave={(e) => { e.currentTarget.style.background = 'transparent' }}
          >
            Stop yielding — ask me again
          </button>
        )}
      </div>
    </>
  )
}

function SectionLabel({ children }: { children: React.ReactNode }) {
  return (
    <div style={{ padding: '8px 12px 2px', color: '#8a7aa3', fontSize: 10, fontWeight: 700, letterSpacing: '0.06em', textTransform: 'uppercase' }}>
      {children}
    </div>
  )
}

function MenuItem({ label, detail, tone, active, onClick }: {
  label: string
  detail: string
  tone: keyof typeof YIELD_TONE
  active: boolean
  onClick: () => void
}) {
  const colors = YIELD_TONE[tone]
  return (
    <button
      type="button"
      role="menuitemcheckbox"
      aria-checked={active}
      onClick={onClick}
      onMouseEnter={(e) => { if (!active) e.currentTarget.style.background = 'rgba(150, 100, 200, 0.16)' }}
      onMouseLeave={(e) => { if (!active) e.currentTarget.style.background = 'transparent' }}
      style={{
        display: 'flex',
        width: '100%',
        alignItems: 'center',
        gap: 10,
        padding: '7px 12px',
        background: active ? colors.bg : 'transparent',
        border: 'none',
        color: '#e0d4f0',
        cursor: 'pointer',
        textAlign: 'left',
        font: 'inherit',
      }}
    >
      <span
        aria-hidden
        style={{
          flexShrink: 0,
          width: 16,
          height: 16,
          borderRadius: '50%',
          border: `1.5px solid ${active ? colors.fg : 'rgba(184, 168, 204, 0.45)'}`,
          background: active ? colors.fg : 'transparent',
          color: '#140f1e',
          fontSize: 10,
          fontWeight: 900,
          lineHeight: '13px',
          textAlign: 'center',
        }}
      >
        {active ? '✓' : ''}
      </span>
      <span style={{ minWidth: 0 }}>
        <span style={{ display: 'block', fontWeight: 600, color: active ? colors.fg : '#e0d4f0' }}>{label}</span>
        <span style={{ display: 'block', color: '#9c8cb4', fontSize: 11 }}>{detail}</span>
      </span>
    </button>
  )
}
