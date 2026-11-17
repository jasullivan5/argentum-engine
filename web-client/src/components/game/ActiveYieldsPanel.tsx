import { useState } from 'react'
import { useGameStore } from '@/store/gameStore.ts'
import type { ClientYield } from '@/types/gameState'
import { yieldTags, YIELD_TONE } from './yields'

/**
 * A small fixed panel listing the player's active persistent yields (MTGO right-click yields —
 * backlog §C) with per-yield revoke and a clear-all, so a player is never trapped by a yield they
 * forgot they set. Hidden when there are no yields. Server-authoritative: it reflects
 * `gameState.activeYields` (masked to the viewer) and dispatches revoke intents.
 */
export function ActiveYieldsPanel() {
  const yields = useGameStore((s) => s.gameState?.activeYields) ?? []
  const clearAbilityYield = useGameStore((s) => s.clearAbilityYield)
  const clearAllYields = useGameStore((s) => s.clearAllYields)
  const [collapsed, setCollapsed] = useState(false)

  if (yields.length === 0) return null

  return (
    <div
      role="region"
      aria-label="Active yields"
      style={{
        position: 'fixed',
        // Clear of the right-hand zone column (deck / graveyard / exile), just above the pass controls.
        right: 104,
        bottom: 96,
        zIndex: 60,
        width: 248,
        background: 'rgba(18, 14, 28, 0.95)',
        border: '1px solid rgba(150, 100, 200, 0.45)',
        borderRadius: 10,
        boxShadow: '0 6px 18px rgba(0, 0, 0, 0.5)',
        color: '#e0d4f0',
        fontSize: 11,
        overflow: 'hidden',
      }}
    >
      <button
        type="button"
        onClick={() => setCollapsed((c) => !c)}
        aria-expanded={!collapsed}
        title="Abilities you've told the game to stop asking about"
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 8,
          width: '100%',
          padding: '7px 10px',
          background: 'rgba(80, 50, 120, 0.4)',
          border: 'none',
          color: 'inherit',
          font: 'inherit',
          cursor: 'pointer',
          textAlign: 'left',
        }}
      >
        <span aria-hidden style={{ color: '#cbb8ff', fontSize: 13, fontWeight: 800, lineHeight: 1 }}>»</span>
        <span style={{ fontWeight: 700, flex: 1 }}>Active yields</span>
        <span style={{
          minWidth: 18,
          padding: '0 6px',
          borderRadius: 999,
          background: 'rgba(160, 130, 240, 0.3)',
          color: '#e6dcff',
          fontWeight: 800,
          textAlign: 'center',
        }}>
          {yields.length}
        </span>
        <span aria-hidden style={{ color: '#b8a8cc' }}>{collapsed ? '▸' : '▾'}</span>
      </button>

      {!collapsed && (
        <>
          <div style={{ maxHeight: '40vh', overflowY: 'auto', padding: '2px 0' }}>
            {yields.map((y) => (
              <YieldRow
                key={`${y.cardDefinitionId}|${y.abilityId}`}
                entry={y}
                onRevoke={() => clearAbilityYield(y.cardDefinitionId, y.abilityId)}
              />
            ))}
          </div>
          <button
            type="button"
            onClick={() => clearAllYields()}
            onMouseEnter={(e) => { e.currentTarget.style.background = 'rgba(200, 80, 70, 0.14)' }}
            onMouseLeave={(e) => { e.currentTarget.style.background = 'transparent' }}
            style={{
              display: 'block',
              width: '100%',
              padding: '7px 10px',
              background: 'transparent',
              border: 'none',
              borderTop: '1px solid rgba(150,100,200,0.25)',
              color: '#ff9a8c',
              cursor: 'pointer',
              font: 'inherit',
              fontWeight: 600,
              textAlign: 'center',
            }}
          >
            Clear all — ask me again
          </button>
        </>
      )}
    </div>
  )
}

function YieldRow({ entry, onRevoke }: { entry: ClientYield; onRevoke: () => void }) {
  const tags = yieldTags(entry)
  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 8, padding: '6px 10px' }}>
      <div style={{ minWidth: 0 }}>
        <div style={{ fontWeight: 600, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }} title={entry.displayName}>
          {entry.displayName}
        </div>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4, marginTop: 3 }}>
          {tags.map((t) => {
            const tone = YIELD_TONE[t.tone]
            return (
              <span
                key={t.label}
                title={t.detail}
                style={{
                  padding: '0 6px',
                  borderRadius: 999,
                  background: tone.bg,
                  border: `1px solid ${tone.border}`,
                  color: tone.fg,
                  fontSize: 10,
                  fontWeight: 700,
                  lineHeight: '15px',
                  whiteSpace: 'nowrap',
                }}
              >
                {t.label}
              </span>
            )
          })}
        </div>
      </div>
      <button
        type="button"
        onClick={onRevoke}
        title="Revoke — ask me again"
        aria-label={`Revoke yields on ${entry.displayName}`}
        onMouseEnter={(e) => { e.currentTarget.style.background = 'rgba(180, 70, 70, 0.45)' }}
        onMouseLeave={(e) => { e.currentTarget.style.background = 'rgba(150, 60, 60, 0.25)' }}
        style={{
          flexShrink: 0,
          width: 22,
          height: 22,
          lineHeight: '20px',
          textAlign: 'center',
          background: 'rgba(150, 60, 60, 0.25)',
          border: '1px solid rgba(200, 90, 90, 0.5)',
          borderRadius: 6,
          color: '#ffc0b0',
          cursor: 'pointer',
          padding: 0,
          fontSize: 13,
        }}
      >
        ×
      </button>
    </div>
  )
}
