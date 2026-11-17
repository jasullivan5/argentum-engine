import { defineConfig, type Plugin } from 'vite'
import react from '@vitejs/plugin-react'
import { execSync } from 'child_process'
import { readFileSync } from 'fs'
import { createRequire } from 'module'
import path from 'path'

let commitHash = process.env.VITE_COMMIT_HASH?.slice(0, 7) || 'unknown'
try {
  if (commitHash === 'unknown') {
    commitHash = execSync('git rev-parse --short HEAD').toString().trim()
  }
} catch {
  // git may not be available (e.g. Docker build)
}

// Backend the dev server proxies to — override when the game server runs on a non-default port.
const gameServerUrl = process.env.GAME_SERVER_URL || 'http://localhost:8080'

/**
 * The contributor guide is a static site under `public/contribute/`. nginx serves its index for
 * `/contribute` and `/contribute/` (`try_files $uri $uri/`), but Vite's dev server only serves
 * public files by exact path and hands everything else to the SPA — which renders the home screen.
 * Point the directory URLs at the file, in dev only.
 */
const contributeGuideInDev: Plugin = {
  name: 'contribute-guide-index',
  apply: 'serve',
  configureServer(server) {
    server.middlewares.use((req, res, next) => {
      // Redirect like nginx does, so the guide's relative asset paths resolve under /contribute/.
      if (req.url === '/contribute') {
        res.statusCode = 301
        res.setHeader('Location', '/contribute/')
        res.end()
        return
      }
      if (req.url === '/contribute/' || req.url?.startsWith('/contribute/?')) req.url = '/contribute/index.html'
      next()
    })
  },
}

/**
 * The admin GeoMap needs one thing from `world-countries`: each country's centroid, by ISO alpha-2.
 * Importing the package bundles the whole dataset (~1.3 MB of names, borders, currencies,
 * translations…) into the admin chunk. This virtual module reads it at build time and emits only
 * `{ cca2: [lng, lat] }` — a few kB.
 */
const COUNTRY_CENTROIDS_ID = 'virtual:country-centroids'
const countryCentroids: Plugin = {
  name: 'country-centroids',
  resolveId(id) {
    return id === COUNTRY_CENTROIDS_ID ? '\0' + COUNTRY_CENTROIDS_ID : undefined
  },
  load(id) {
    if (id !== '\0' + COUNTRY_CENTROIDS_ID) return undefined
    const file = createRequire(import.meta.url).resolve('world-countries/countries.json')
    const countries = JSON.parse(readFileSync(file, 'utf8')) as Array<{ cca2: string; latlng: [number, number] }>
    // world-countries stores [lat, lng]; d3 projections want [lng, lat].
    const centroids = Object.fromEntries(countries.map((c) => [c.cca2, [c.latlng[1], c.latlng[0]]]))
    return `export default ${JSON.stringify(centroids)}`
  },
}

export default defineConfig({
  plugins: [react(), contributeGuideInDev, countryCentroids],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  define: {
    __COMMIT_HASH__: JSON.stringify(commitHash),
  },
  server: {
    port: 5173,
    proxy: {
      '/game': {
        target: gameServerUrl,
        ws: true,
        changeOrigin: true,
      },
      '/api': {
        target: gameServerUrl,
        changeOrigin: true,
      },
    },
  },
  // `vite preview` serves the real production bundle. Without the same proxy it can't reach the
  // game server, so the only way to exercise a build the way a player meets it — minified, no
  // StrictMode double-render — was to deploy it. Perf work needs those numbers, not dev's.
  preview: {
    port: 5175,
    proxy: {
      '/game': {
        target: gameServerUrl,
        ws: true,
        changeOrigin: true,
      },
      '/api': {
        target: gameServerUrl,
        changeOrigin: true,
      },
    },
  },
  build: {
    sourcemap: true,
  },
})
