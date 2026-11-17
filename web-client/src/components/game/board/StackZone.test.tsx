import { renderToStaticMarkup } from 'react-dom/server'
import { afterEach, describe, expect, it } from 'vitest'
import { useGameStore } from '@/store/gameStore.ts'
import { useResponsive } from '@/hooks/useResponsive.ts'
import { entityId, ZoneType, type ClientCard, type ClientGameState } from '@/types'
import { ResponsiveContext } from './shared'
import { StackDisplay } from './StackZone'

/**
 * The text box under the stack used to clamp an ability to five lines, so the Speed trigger read
 * "This ability triggers only once.." — a different rule from the one on the stack. These pin that
 * the whole text is rendered, never clipped, and that it is also offered on hover.
 */

const SPEED_TRIGGER =
  'Whenever one or more opponents lose life during your turn, if your speed is less than 4, ' +
  'your speed increases by 1. This ability triggers only once each turn.'

function stackWith(card: ClientCard): ClientGameState {
  return {
    viewingPlayerId: entityId('p1'),
    players: [{ playerId: entityId('p1'), name: 'Alice' }, { playerId: entityId('p2'), name: 'Bob' }],
    cards: { [card.id]: card },
    zones: [{ zoneId: { zoneType: ZoneType.STACK }, cardIds: [card.id] }],
  } as unknown as ClientGameState
}

function Harness() {
  return (
    <ResponsiveContext.Provider value={useResponsive()}>
      <StackDisplay />
    </ResponsiveContext.Provider>
  )
}

// Server rendering reads zustand's *initial* snapshot, not the live one, so the board is staged
// there and put back afterwards.
const initial = useGameStore.getInitialState()
const initialGameState = initial.gameState
function stage(gameState: ClientGameState) {
  Object.assign(initial, { gameState })
}
afterEach(() => {
  Object.assign(initial, { gameState: initialGameState })
})

describe('stack ability text', () => {
  it('shows a long triggered ability whole, without a line clamp', () => {
    const trigger = {
      id: entityId('speed-trigger'),
      name: 'Speed trigger',
      typeLine: 'Triggered Ability',
      cardTypes: ['Ability'],
      subtypes: [],
      keywords: [],
      oracleText: SPEED_TRIGGER,
      controllerId: entityId('p1'),
    } as unknown as ClientCard
    stage(stackWith(trigger))

    const html = renderToStaticMarkup(<Harness />)
    // Nothing on the stack is clamped — the old box was `-webkit-line-clamp: 5`.
    expect(html).not.toMatch(/line-clamp/i)
    // The full text is also on hover.
    expect(html).toContain(`title="${SPEED_TRIGGER}"`)
    expect(html).toContain('only once each turn.')
  })
})
