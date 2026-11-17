/**
 * URL-backed navigation for the admin dashboard. Every admin screen has its own address
 * (`/admin`, `/admin/<area>`, `/admin/players/<id>`), so the browser's Back/Forward, a refresh and a
 * shared link all land where the admin was — not back on the hub, or out of the dashboard entirely.
 *
 * Going *into* a screen pushes a history entry marked {@link OpenedHere}; the in-page "← Back" then
 * pops that entry instead of pushing the parent on top, so the on-screen back and the browser's back
 * walk the same trail. Arriving by deep link (no marked entry) the in-page back replaces the current
 * entry with the parent instead.
 */
import { useCallback } from 'react'
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom'

/** History state on an entry an admin screen pushed — going up then goes Back instead of piling up entries. */
interface OpenedHere {
  adminOpenedHere?: boolean
}

export function useAdminNav() {
  const navigate = useNavigate()
  const location = useLocation()
  const openedHere = !!(location.state as OpenedHere | null)?.adminOpenedHere

  /** Open an admin screen below the current one; `path` is relative to `/admin` (`''` is the hub). */
  const open = useCallback(
    (path: string) => navigate(adminPath(path), { state: { adminOpenedHere: true } satisfies OpenedHere }),
    [navigate],
  )

  /** Leave the current screen for its parent. */
  const up = useCallback(
    (parentPath: string) => {
      if (openedHere) navigate(-1)
      else navigate(adminPath(parentPath), { replace: true })
    },
    [navigate, openedHere],
  )

  return { open, up }
}

/**
 * A modal or sub-view kept in the query string (`?tournament=12`), so leaving the page — to a replay,
 * a profile — and coming Back reopens it. Opening pushes an entry, so the browser's Back closes it.
 */
export function useAdminParam(key: string) {
  const navigate = useNavigate()
  const location = useLocation()
  const [params, setParams] = useSearchParams()
  const value = params.get(key)
  const openedHere = !!(location.state as OpenedHere | null)?.adminOpenedHere

  const set = useCallback(
    (next: string) =>
      setParams(
        (prev) => {
          const p = new URLSearchParams(prev)
          p.set(key, next)
          return p
        },
        { state: { adminOpenedHere: true } satisfies OpenedHere },
      ),
    [key, setParams],
  )

  const clear = useCallback(() => {
    if (openedHere) {
      navigate(-1)
      return
    }
    setParams(
      (prev) => {
        const p = new URLSearchParams(prev)
        p.delete(key)
        return p
      },
      { replace: true },
    )
  }, [key, navigate, openedHere, setParams])

  return [value, set, clear] as const
}

/** The open {@link TournamentDetailModal}'s id, kept in `?tournament=`. */
export function useAdminTournamentParam() {
  const [raw, set, clear] = useAdminParam('tournament')
  const id = raw != null && /^\d+$/.test(raw) ? Number(raw) : null
  const open = useCallback((tournamentId: number) => set(String(tournamentId)), [set])
  return [id, open, clear] as const
}

/** A zero-based page number kept in the query string; paging replaces the entry rather than adding one per click. */
export function useAdminPageParam(key = 'page') {
  const location = useLocation()
  const [params, setParams] = useSearchParams()
  const page = parsePage(params.get(key))

  const setPage = useCallback(
    (next: number | ((cur: number) => number)) =>
      setParams(
        (prev) => {
          const p = new URLSearchParams(prev)
          const cur = parsePage(p.get(key))
          const value = typeof next === 'function' ? next(cur) : next
          if (value <= 0) p.delete(key)
          else p.set(key, String(value + 1))
          return p
        },
        // Keep the entry's state, so the in-page back still pops it after paging.
        { replace: true, state: location.state },
      ),
    [key, location.state, setParams],
  )

  return [page, setPage] as const
}

/** `?page=` is one-based for people reading the URL; anything unparseable is the first page. */
function parsePage(raw: string | null) {
  const n = Number(raw ?? '1')
  return Number.isInteger(n) && n > 1 ? n - 1 : 0
}

function adminPath(path: string) {
  return path ? `/admin/${path}` : '/admin'
}
