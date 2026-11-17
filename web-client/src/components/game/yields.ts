import type { ClientAbilityIdentity, ClientYield } from '@/types/gameState'

/** The viewer's active yield on one ability, if any. */
export function findYield(
  yields: readonly ClientYield[] | undefined,
  identity: ClientAbilityIdentity | null | undefined,
): ClientYield | undefined {
  if (!identity || !yields) return undefined
  return yields.find((y) => y.cardDefinitionId === identity.cardDefinitionId && y.abilityId === identity.abilityId)
}

/** One active dimension of a yield, as a short chip label plus a sentence for tooltips. */
export interface YieldTag {
  readonly label: string
  readonly detail: string
  readonly tone: 'pass' | 'yes' | 'no'
}

/** Every dimension active on [y], most decisive first (the auto-answer, then the pass scope). */
export function yieldTags(y: ClientYield): YieldTag[] {
  const tags: YieldTag[] = []
  if (y.autoAnswer === true) tags.push({ label: 'Always yes', detail: 'Its “you may” is answered Yes for you', tone: 'yes' })
  if (y.autoAnswer === false) tags.push({ label: 'Always no', detail: 'Its “you may” is answered No for you', tone: 'no' })
  if (y.wholeGame) tags.push({ label: 'Auto-pass · game', detail: 'Priority passes on it for the rest of the game', tone: 'pass' })
  else if (y.untilEndOfTurn) tags.push({ label: 'Auto-pass · turn', detail: 'Priority passes on it until end of turn', tone: 'pass' })
  return tags
}

export const YIELD_TONE: Record<YieldTag['tone'], { fg: string; bg: string; border: string }> = {
  pass: { fg: '#cbb8ff', bg: 'rgba(110, 80, 190, 0.28)', border: 'rgba(160, 130, 240, 0.55)' },
  yes: { fg: '#8ff0b4', bg: 'rgba(40, 140, 90, 0.26)', border: 'rgba(90, 210, 140, 0.55)' },
  no: { fg: '#ffb0a0', bg: 'rgba(170, 60, 50, 0.26)', border: 'rgba(230, 110, 95, 0.55)' },
}
