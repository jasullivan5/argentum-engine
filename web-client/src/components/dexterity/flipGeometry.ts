/**
 * Display geometry for dexterity flips (Chaos Orb).
 *
 * The server owns the table layout and decides where a flip lands; these helpers only turn its
 * millimetre table into screen space and read the spin gauge from the numbers it sends.
 */
import type { FlipReleaseProfile, FlipTable, FlipTableCard } from '@/types'

export const CARD_W = 63
export const CARD_H = 88

/** Room either side of the table — the spin gauge stands in the right-hand one. */
const SIDE_MARGIN = 96
const FAR_MARGIN = 34
const HAND_MARGIN = 64

export interface Point {
  readonly x: number
  readonly y: number
}

/**
 * Maps table coordinates to the stage the viewer sees. The flipper sees the table from their own
 * seat (their battlefield at the bottom); everyone else sees it turned round, from the far side.
 */
export interface FlipView {
  readonly stageWidth: number
  readonly stageHeight: number
  readonly fromFarSide: boolean
  toStage(p: Point): Point
  fromStage(p: Point): Point
  /** Turn a landing angle into the viewer's frame. */
  rotation(deg: number): number
}

export function makeFlipView(table: FlipTable, fromFarSide: boolean): FlipView {
  const stageWidth = table.width + 2 * SIDE_MARGIN
  const stageHeight = table.height + FAR_MARGIN + (table.handY - table.height) + HAND_MARGIN
  // Flipper's view: table top sits FAR_MARGIN down. Far side: the hand edge is at the top.
  const toStage = (p: Point): Point =>
    fromFarSide
      ? { x: SIDE_MARGIN + (table.width - p.x), y: stageHeight - FAR_MARGIN - p.y }
      : { x: SIDE_MARGIN + p.x, y: FAR_MARGIN + p.y }
  const fromStage = (p: Point): Point =>
    fromFarSide
      ? { x: table.width - (p.x - SIDE_MARGIN), y: stageHeight - FAR_MARGIN - p.y }
      : { x: p.x - SIDE_MARGIN, y: p.y - FAR_MARGIN }
  return {
    stageWidth,
    stageHeight,
    fromFarSide,
    toStage,
    fromStage,
    rotation: (deg) => (fromFarSide ? deg + 180 : deg),
  }
}

/** 1 for a spin in the clean band, falling to 0 as it strays outside it (mirrors the engine). */
export function spinQuality(profile: FlipReleaseProfile, spin: number): number {
  const w = Math.min(profile.maxSpin, Math.abs(spin))
  const miss = w < profile.cleanSpinFrom ? profile.cleanSpinFrom - w : w > profile.cleanSpinTo ? w - profile.cleanSpinTo : 0
  return 1 - Math.min(1, miss / profile.spinSlop)
}

/** Whether an unrotated card resting at [p] would overlap [card]'s touchable area (aim preview). */
export function previewTouches(p: Point, card: FlipTableCard): boolean {
  return (
    p.x + CARD_W / 2 >= card.x - card.width / 2 &&
    p.x - CARD_W / 2 <= card.x + card.width / 2 &&
    p.y + CARD_H / 2 >= card.hitTop &&
    p.y - CARD_H / 2 <= card.hitBottom
  )
}
