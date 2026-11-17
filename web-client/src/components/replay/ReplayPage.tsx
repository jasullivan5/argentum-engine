/**
 * `/replay/:gameId` — the shareable, deep-linkable replay URL.
 *
 * This route owns only "which replay, and did it load": the id from the URL, the public-replay
 * fetch and loading/error states. Everything after that — including stamping the seat roster,
 * transport, scrubber, share/export, the board — is {@link ReplayPlayer}, shared with the in-app overlay so
 * the two cannot drift apart again.
 */
import { useCallback, useEffect, useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import {
  reconstructSnapshots,
  type PublicReplayData,
  type SpectatorStateUpdate,
} from '@/replay/reconstructSnapshots.ts'
import { ReplayPlayer, type ReplayMetadata } from './ReplayPlayer'
import { PageShell, pageStyles } from '@/components/ui/PageShell'
import styles from './Replay.module.css'

export function ReplayPage() {
  const { gameId } = useParams<{ gameId: string }>()
  const navigate = useNavigate()
  const location = useLocation()

  const [snapshots, setSnapshots] = useState<SpectatorStateUpdate[]>([])
  const [metadata, setMetadata] = useState<ReplayMetadata | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!gameId) return
    let cancelled = false

    async function loadReplay() {
      setLoading(true)
      setError(null)
      try {
        const response = await fetch(`/api/public/replays/${gameId}`)
        if (!response.ok) {
          setError(
            response.status === 404
              ? 'Replay not found. It may have expired or the game ID is invalid.'
              : 'Failed to load replay.',
          )
          setLoading(false)
          return
        }
        const data = await response.json() as PublicReplayData
        if (cancelled) return
        setMetadata(data.metadata)
        setSnapshots(reconstructSnapshots(data.initialSnapshot, data.deltas))
      } catch {
        if (!cancelled) setError('Failed to load replay.')
      }
      if (!cancelled) setLoading(false)
    }

    loadReplay()
    return () => { cancelled = true }
  }, [gameId])

  const goHome = useCallback(() => { navigate('/') }, [navigate])
  // Back returns to wherever the replay was opened from (admin Activity, a tournament, a profile);
  // only a replay opened cold — a shared link, a new tab — has nowhere to go back to but home.
  const canGoBack = location.key !== 'default'
  const goBack = useCallback(() => { if (canGoBack) navigate(-1); else navigate('/') }, [canGoBack, navigate])

  if (loading || error) {
    return (
      <PageShell title="Replay" width="narrow" plain>
        <div className={styles.stateCenter}>
          <section className={`${pageStyles.panel} ${styles.stateCard}`} aria-live="polite">
            {loading ? (
              <>
                <span className={styles.spinner} aria-hidden />
                <p className={styles.stateText}>Loading replay…</p>
              </>
            ) : (
              <>
                <h1 className={styles.stateTitle}>Replay unavailable</h1>
                <p className={styles.stateText}>{error}</p>
                <button type="button" onClick={goBack} className={pageStyles.buttonPrimary}>
                  {canGoBack ? 'Go back' : 'Back to home'}
                </button>
              </>
            )}
          </section>
        </div>
      </PageShell>
    )
  }

  return (
    <ReplayPlayer
      snapshots={snapshots}
      gameId={gameId ?? ''}
      metadata={metadata}
      onExit={goBack}
      onHome={goHome}
    />
  )
}
