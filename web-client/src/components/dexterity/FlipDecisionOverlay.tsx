import { useCallback, useEffect, useMemo, useRef, useState, type PointerEvent as ReactPointerEvent } from 'react'
import { useGameStore } from '@/store/gameStore.ts'
import type { EntityId, FlipOntoBattlefieldDecision } from '@/types'
import { getCardImageUrl } from '@/utils/cardImages.ts'
import { FlipTableStage, type CardMark, type StagePointerKind } from './FlipTableStage'
import { FlipOrb } from './FlipOrb'
import { makeFlipView, previewTouches, spinQuality, type Point } from './flipGeometry'
import decisionStyles from '../decisions/DecisionUI.module.css'
import styles from './Flip.module.css'

/** Spin (turns/s) one full circle of the pointer winds into the card. */
const SPIN_PER_CIRCLE = 2
/** How fast wound-up spin bleeds away (seconds for it to fall to 1/e) once you stop circling. */
const SPIN_DECAY_S = 0.9
/** Pointer steps shorter than this (mm) are too small to read a direction from. */
const MIN_STEP_MM = 1.5
/** A turn sharper than this between two steps is a reversal, not circling, and winds nothing. */
const MAX_TURN_RAD = 2.4
/** The release velocity is read over the last this-many ms of the swipe. */
const VELOCITY_WINDOW_MS = 70
/** A release slower than this (mm/s) isn't a flick: the card stays in your hand. */
const MIN_FLICK_SPEED = 350
/** How much of the swipe the trail shows. */
const TRAIL_MS = 380
/** The spin the gauge runs up to (turns/s). */
const GAUGE_MAX_SPIN = 7
const GAUGE_HEIGHT = 220

interface Sample { t: number; x: number; y: number }

/**
 * The dexterity-flip mini-game (Chaos Orb, Falling Star), thrower's side: press and circle to spin
 * the card up, then flick it towards what you want to hit and let go mid-swipe. The swipe's speed
 * is how far it carries, its direction the line it flies, the spin whether it turns over (too
 * little) or wobbles and skids (too much). Nothing shows where a flick will land: the table is the
 * server's, the flick is the player's, and the server rolls the landing when it receives it.
 */
export function FlipDecisionOverlay({ decision }: { decision: FlipOntoBattlefieldDecision }) {
  const submitFlipThrow = useGameStore((s) => s.submitFlipThrow)
  const gameState = useGameStore((s) => s.gameState)
  const viewerId = useGameStore((s) => s.playerId)
  const { table, release } = decision
  const view = useMemo(() => makeFlipView(table, false), [table])
  const sourceName = decision.context.sourceName ?? 'Chaos Orb'
  // The card says what the flip does (destroy, deal damage, …); the table only says what it touches.
  const sourceId = decision.context.sourceId
  const rulesText = (sourceId ? gameState?.cards[sourceId]?.oracleText : null) ||
    'If it turns over completely, it affects what it lands on — yours too.'
  const orbImage = getCardImageUrl(sourceName, decision.flippedImageUri)

  const [hover, setHover] = useState<Point | null>(null)
  const [holding, setHolding] = useState(false)
  const [hidden, setHidden] = useState(false)
  /** Why the last release didn't throw, shown until the next press. */
  const [hint, setHint] = useState<string | null>(null)

  /** The swipe so far (table mm), for its release velocity and its trail. */
  const samples = useRef<Sample[]>([])
  /** Spin wound into the card (turns/s). */
  const spin = useRef(0)
  /** The last point a direction was read from, and that direction. */
  const anchor = useRef<Point | null>(null)
  const heading = useRef<number | null>(null)
  const submitted = useRef(false)

  const crossRef = useRef<HTMLDivElement>(null)
  const needleRef = useRef<HTMLDivElement>(null)
  const readoutRef = useRef<HTMLDivElement>(null)
  const trailRef = useRef<SVGPolylineElement>(null)
  const orbRef = useRef<HTMLDivElement>(null)

  const playerName = useCallback(
    (id: EntityId) => gameState?.players.find((p) => p.playerId === id)?.name ?? 'Opponent',
    [gameState],
  )

  // While holding: bleed the spin away, spin the card in the hand, draw the gauge and the trail.
  useEffect(() => {
    if (!holding) return
    let frame = 0
    let last = performance.now()
    let angle = 0
    const tick = (now: number) => {
      const dt = Math.max(0, now - last) / 1000
      last = now
      spin.current *= Math.exp(-dt / SPIN_DECAY_S)
      const w = spin.current
      angle = (angle + w * 360 * dt) % 360
      if (orbRef.current) {
        orbRef.current.style.transform = `translateY(-14px) rotateZ(-6deg) scale(1.05) rotateX(${angle}deg)`
      }
      if (needleRef.current) {
        needleRef.current.style.top = `${(1 - Math.min(1, w / GAUGE_MAX_SPIN)) * GAUGE_HEIGHT}px`
      }
      if (readoutRef.current) {
        readoutRef.current.textContent = `${w.toFixed(1)} turns/s`
        readoutRef.current.dataset.tone = spinQuality(release, w) >= 0.999 ? 'good' : 'off'
      }
      const trail = trailRef.current
      if (trail) {
        const recent = samples.current.filter((s) => now - s.t <= TRAIL_MS)
        trail.setAttribute('points', recent.map((s) => { const p = view.toStage(s); return `${p.x},${p.y}` }).join(' '))
      }
      frame = requestAnimationFrame(tick)
    }
    frame = requestAnimationFrame(tick)
    return () => cancelAnimationFrame(frame)
  }, [holding, view, release])

  const resetHand = useCallback(() => {
    samples.current = []
    spin.current = 0
    anchor.current = null
    heading.current = null
    if (orbRef.current) orbRef.current.style.transform = 'rotateZ(-6deg)'
    if (needleRef.current) needleRef.current.style.top = `${GAUGE_HEIGHT}px`
    if (readoutRef.current) { readoutRef.current.textContent = 'spin'; delete readoutRef.current.dataset.tone }
    trailRef.current?.setAttribute('points', '')
  }, [])

  useEffect(() => { resetHand() }, [resetHand])

  const cancel = useCallback(() => {
    setHolding(false)
    resetHand()
  }, [resetHand])

  /** Record a pointer step; turning the pointer round winds spin into the card. */
  const record = (p: Point) => {
    const now = performance.now()
    samples.current.push({ t: now, x: p.x, y: p.y })
    while (samples.current.length > 0 && now - samples.current[0]!.t > 1000) samples.current.shift()
    const from = anchor.current
    if (!from) { anchor.current = p; return }
    if (Math.hypot(p.x - from.x, p.y - from.y) < MIN_STEP_MM) return
    const a = Math.atan2(p.y - from.y, p.x - from.x)
    if (heading.current !== null) {
      let turn = a - heading.current
      while (turn > Math.PI) turn -= 2 * Math.PI
      while (turn < -Math.PI) turn += 2 * Math.PI
      if (Math.abs(turn) < MAX_TURN_RAD) {
        spin.current = Math.min(release.maxSpin, spin.current + (Math.abs(turn) / (2 * Math.PI)) * SPIN_PER_CIRCLE)
      }
    }
    anchor.current = p
    heading.current = a
  }

  /** Let go: read the swipe's velocity over its last few frames and throw — or keep the card. */
  const letGo = (p: Point) => {
    if (submitted.current) return
    const now = performance.now()
    samples.current.push({ t: now, x: p.x, y: p.y })
    const recent = samples.current.filter((s) => now - s.t <= VELOCITY_WINDOW_MS)
    const first = recent[0]
    const lastSample = recent[recent.length - 1]
    const dt = first && lastSample ? (lastSample.t - first.t) / 1000 : 0
    const vx = first && lastSample && dt > 0.008 ? (lastSample.x - first.x) / dt : 0
    const vy = first && lastSample && dt > 0.008 ? (lastSample.y - first.y) / dt : 0
    const speed = Math.hypot(vx, vy)
    const w = spin.current
    setHolding(false)
    if (speed < MIN_FLICK_SPEED) {
      setHint('It stayed in your hand — let go while you’re still swiping')
      resetHand()
      return
    }
    // The table's y grows towards the thrower: a forward flick has a negative vy.
    if (vy > -0.25 * speed) {
      setHint('Flick it forward, towards the table')
      resetHand()
      return
    }
    submitted.current = true
    submitFlipThrow(decision.id, vx, vy, w)
  }

  const onPointer = (kind: StagePointerKind, p: Point, e: ReactPointerEvent<HTMLDivElement>) => {
    if (submitted.current) return
    switch (kind) {
      case 'move':
        if (holding) record(p)
        else setHover(p)
        break
      case 'down':
        if (e.button === 2) { cancel(); break }
        if (e.button !== 0) break
        e.currentTarget.setPointerCapture(e.pointerId)
        resetHand()
        setHint(null)
        record(p)
        setHolding(true)
        break
      case 'up':
        if (holding) letGo(p)
        break
      case 'cancel':
        cancel()
        break
      case 'leave':
        if (!holding) setHover(null)
        break
    }
  }

  useEffect(() => {
    const key = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && holding) { e.preventDefault(); cancel() }
    }
    window.addEventListener('keydown', key)
    return () => window.removeEventListener('keydown', key)
  }, [holding, cancel])

  useEffect(() => {
    const cross = crossRef.current
    if (!cross || !hover) return
    const c = view.toStage(hover)
    cross.style.left = `${c.x}px`
    cross.style.top = `${c.y}px`
  }, [hover, view])

  // Reading the table before the throw: what a card resting under the pointer would cover.
  const covered = useMemo(
    () => (hover && !holding ? table.cards.filter((c) => previewTouches(hover, c)) : []),
    [hover, holding, table],
  )
  const marks = useMemo(() => {
    const m = new Map<EntityId, CardMark>()
    for (const c of covered) m.set(c.entityId, !c.eligible ? 'immune' : c.controllerId === viewerId ? 'own' : 'target')
    return m
  }, [covered, viewerId])
  const chips = useMemo(() => {
    const m = new Map<EntityId, { text: string; tone: 'danger' | 'warn' | 'calm' }>()
    for (const c of covered) {
      if (!c.eligible) m.set(c.entityId, { text: 'Safe', tone: 'calm' })
      else if (c.controllerId === viewerId) m.set(c.entityId, { text: 'Yours', tone: 'warn' })
    }
    return m
  }, [covered, viewerId])

  const hoverLabel = (() => {
    if (!hover || holding) return null
    const hits = covered.filter((c) => c.eligible)
    if (hits.length === 0) return covered.length > 0 ? `Nothing ${sourceName} affects here` : null
    return hits.map((c) => (c.name ?? 'Face-down permanent') + (c.controllerId === viewerId ? ' (yours)' : '')).join(' · ')
  })()

  if (hidden) {
    return (
      <button className={decisionStyles.floatingReturnButton} onClick={() => setHidden(false)}>
        Return to the {sourceName} flip
      </button>
    )
  }

  const hand = view.toStage({ x: table.handX, y: table.handY })
  // The spin gauge stands just off the table's right-hand edge, level with the hand.
  const gaugeX = view.toStage({ x: table.width, y: 0 }).x + 26
  const gaugeY = (turns: number) => (1 - Math.min(1, turns / GAUGE_MAX_SPIN)) * GAUGE_HEIGHT
  const stageHover = hover ? view.toStage(hover) : null

  return (
    <div className={styles.overlay} role="dialog" aria-label={`Flip ${sourceName}`}>
      <div className={styles.header}>
        <div className={styles.headerArt} style={{ backgroundImage: `url("${orbImage}")` }} />
        <div className={styles.headerText}>
          <div className={styles.title}>Flip {sourceName}</div>
          <div className={styles.subtitle}>{rulesText}</div>
        </div>
        <div className={styles.headerSpacer} />
        <button className={styles.ghostButton} onClick={() => setHidden(true)} disabled={holding}>
          View battlefield
        </button>
      </div>

      <FlipTableStage
        table={table}
        view={view}
        viewerId={viewerId}
        playerName={playerName}
        marks={marks}
        chips={chips}
        className={holding ? styles.flicking : styles.aiming}
        onPointer={onPointer}
      >
        <svg className={styles.trail} width={view.stageWidth} height={view.stageHeight} aria-hidden>
          <polyline ref={trailRef} points="" />
        </svg>
        <div ref={crossRef} className={styles.crosshair} style={{ display: hover && !holding ? undefined : 'none' }} />
        {stageHover && hoverLabel && (
          <div className={styles.aimLabel} style={{ left: stageHover.x, top: stageHover.y }}>
            {hoverLabel}
          </div>
        )}

        <div className={styles.orbAnchor} style={{ left: hand.x, top: hand.y, perspective: 700 }}>
          <FlipOrb ref={orbRef} imageUrl={orbImage} className={styles.orbInHand} faceClassName={styles.orbGlow} />
        </div>

        <div className={styles.gauge} style={{ left: gaugeX, top: hand.y + 44 - GAUGE_HEIGHT, height: GAUGE_HEIGHT }}>
          <div className={styles.gaugeTrack}>
            <div
              className={`${styles.gaugeZone} ${styles.zoneWild}`}
              style={{ top: 5, height: gaugeY(release.cleanSpinTo + release.spinSlop / 2) - 5 }}
            />
            <div
              className={`${styles.gaugeZone} ${styles.zoneClean}`}
              style={{ top: gaugeY(release.cleanSpinTo), height: gaugeY(release.cleanSpinFrom) - gaugeY(release.cleanSpinTo) }}
            />
            <div className={`${styles.gaugeZone} ${styles.zoneSoft}`} style={{ top: gaugeY(release.tooLittleSpinBelow), bottom: 5 }} />
          </div>
          <div ref={needleRef} className={styles.gaugeNeedle} style={{ top: GAUGE_HEIGHT, opacity: holding ? 1 : 0.35 }} />
          <div className={styles.gaugeTick} style={{ top: 12 }}>wobbles</div>
          <div
            className={styles.gaugeTick}
            style={{ top: (gaugeY(release.cleanSpinFrom) + gaugeY(release.cleanSpinTo)) / 2, color: '#ffe9a8' }}
          >
            clean
          </div>
          <div className={styles.gaugeTick} style={{ top: GAUGE_HEIGHT - 12 }}>no turn</div>
          <div ref={readoutRef} className={styles.gaugeReadout}>spin</div>
        </div>
      </FlipTableStage>

      <div className={styles.footer}>
        <div className={styles.steps} aria-live="polite">
          <span className={`${styles.step} ${!holding ? styles.stepActive : ''}`}>
            <span className={styles.stepNum}>1</span>Press and circle to spin it up to the gold
          </span>
          <span className={`${styles.step} ${holding ? styles.stepActive : ''}`}>
            <span className={styles.stepNum}>2</span>Flick it at your target and let go mid-swipe
          </span>
        </div>
        <span className={`${styles.subtitle} ${hint ? styles.hintWarn : ''}`}>
          {hint ?? (holding
            ? 'Faster flicks fly further · too little spin won’t turn over, too much wobbles · Esc to stop'
            : 'Swipe speed is distance, swipe direction is the line')}
        </span>
      </div>
    </div>
  )
}
