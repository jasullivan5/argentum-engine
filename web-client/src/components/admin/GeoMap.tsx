/**
 * World map of where players connect from, for the admin dashboard. The server resolves raw IPs to
 * coarse locations server-side and sends only aggregated {@link GeoBucket}s (country/region/city +
 * game count) — this draws them on an actual map instead of a long table: a muted Natural-Earth
 * basemap (world-atlas TopoJSON) with a proportional bubble per country, sized by games and placed at
 * the country's centroid (world-countries lat/long, keyed by ISO alpha-2). A compact ranked list sits
 * beside it for the precise numbers the bubbles only approximate; locations with no resolvable country
 * (e.g. "Local network") fall to that list rather than vanishing.
 */
import { useMemo, useState } from 'react'
import type React from 'react'
import { geoNaturalEarth1, geoPath } from 'd3-geo'
import { feature } from 'topojson-client'
import CENTROIDS from 'virtual:country-centroids'
import worldTopology from 'world-atlas/countries-110m.json'
import type { GeoBucket } from '@/api/adminStats'
import { adminTheme } from './adminUi'

const WIDTH = 760
const HEIGHT = 380

interface CountryAgg {
  code: string | null
  country: string
  games: number
}

/** Sum games per resolved country; rows without a country code are folded into one "unknown" entry. */
function aggregateByCountry(buckets: GeoBucket[]): CountryAgg[] {
  const byCode = new Map<string, CountryAgg>()
  for (const b of buckets) {
    const key = b.countryCode ?? '∅'
    const existing = byCode.get(key)
    if (existing) existing.games += b.games
    else byCode.set(key, { code: b.countryCode, country: b.country ?? 'Unknown / local', games: b.games })
  }
  return [...byCode.values()].sort((a, b) => b.games - a.games)
}

export function GeoMap({ buckets }: { buckets: GeoBucket[] }) {
  const [hover, setHover] = useState<{ label: string; x: number; y: number } | null>(null)

  const { paths, markers, ranked, maxGames } = useMemo(() => {
    const fc = feature(
      worldTopology as never,
      (worldTopology as { objects: { countries: unknown } }).objects.countries as never,
    ) as unknown as { features: unknown[] }
    const projection = geoNaturalEarth1().fitSize([WIDTH, HEIGHT], fc as never)
    const pathGen = geoPath(projection)
    const countryPaths = fc.features.map((f) => pathGen(f as never) ?? '')

    const ranked = aggregateByCountry(buckets)
    const maxGames = Math.max(1, ...ranked.map((r) => r.games))
    const markers = ranked
      .filter((r) => r.code && CENTROIDS[r.code])
      .map((r) => {
        const [x, y] = projection(CENTROIDS[r.code!]!) ?? [0, 0]
        // Area-proportional radius (sqrt), clamped to a readable range.
        const radius = 4 + (Math.sqrt(r.games) / Math.sqrt(maxGames)) * 20
        return { ...r, x, y, radius }
      })
      .sort((a, b) => b.radius - a.radius) // big bubbles first so small ones stay clickable on top
    return { paths: countryPaths, markers, ranked, maxGames }
  }, [buckets])

  return (
    <div style={styles.wrap}>
      <div style={styles.mapBox}>
        <svg viewBox={`0 0 ${WIDTH} ${HEIGHT}`} style={styles.svg} role="img" aria-label="Player connection map">
          <g>
            {paths.map((d, i) => (
              <path key={i} d={d} fill={adminTheme.panelAlt} stroke={adminTheme.border} strokeWidth={0.4} />
            ))}
          </g>
          <g>
            {markers.map((m) => (
              <circle
                key={m.code}
                cx={m.x}
                cy={m.y}
                r={m.radius}
                fill={adminTheme.accentSolid}
                fillOpacity={0.5}
                stroke={adminTheme.accent}
                strokeWidth={1}
                style={{ cursor: 'default' }}
                onMouseEnter={() => setHover({ label: `${m.country} · ${m.games}`, x: m.x, y: m.y })}
                onMouseLeave={() => setHover(null)}
              >
                <title>{`${m.country}: ${m.games} game${m.games === 1 ? '' : 's'}`}</title>
              </circle>
            ))}
          </g>
          {hover && (
            <g transform={`translate(${hover.x}, ${hover.y - 12})`} pointerEvents="none">
              <text textAnchor="middle" style={styles.hoverText}>
                {hover.label}
              </text>
            </g>
          )}
        </svg>
      </div>

      <div style={styles.list}>
        <div style={styles.listHead}>Top locations</div>
        {ranked.slice(0, 12).map((r) => (
          <div key={r.code ?? r.country} style={styles.listRow}>
            <span style={styles.listName}>{r.country}</span>
            <span style={styles.listBarTrack}>
              <span style={{ ...styles.listBar, width: `${(r.games / maxGames) * 100}%` }} />
            </span>
            <span style={styles.listCount}>{r.games}</span>
          </div>
        ))}
      </div>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  wrap: { display: 'grid', gridTemplateColumns: 'minmax(0, 2fr) minmax(180px, 1fr)', gap: 18, alignItems: 'start' },
  mapBox: { backgroundColor: adminTheme.bg, border: `1px solid ${adminTheme.borderSoft}`, borderRadius: 12, padding: 8 },
  svg: { width: '100%', height: 'auto', display: 'block' },
  hoverText: { fill: adminTheme.text, fontSize: 11, fontWeight: 700, paintOrder: 'stroke', stroke: adminTheme.bg, strokeWidth: 3 },
  list: { display: 'flex', flexDirection: 'column', gap: 8 },
  listHead: { color: adminTheme.textMuted, fontSize: 11, fontWeight: 600, textTransform: 'uppercase', letterSpacing: 0.6 },
  listRow: { display: 'grid', gridTemplateColumns: '1fr 56px auto', alignItems: 'center', gap: 8, fontSize: 12 },
  listName: { color: adminTheme.textSecondary, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' },
  listBarTrack: { height: 6, borderRadius: 999, backgroundColor: adminTheme.panelAlt, overflow: 'hidden' },
  listBar: { display: 'block', height: '100%', backgroundColor: adminTheme.accentSolid, borderRadius: 999 },
  listCount: { color: adminTheme.text, fontVariantNumeric: 'tabular-nums', minWidth: 28, textAlign: 'right' },
}
