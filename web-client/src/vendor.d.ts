/** Ambient typing for the bundled world-atlas TopoJSON basemap (admin GeoMap). The shape is an
 * opaque TopoJSON Topology; consumers pass it straight to topojson-client's `feature()`. Typed as
 * `unknown` so importing the large JSON doesn't balloon type-checking with its literal type. */
declare module 'world-atlas/countries-110m.json' {
  const topology: unknown
  export default topology
}

/** ISO alpha-2 → [longitude, latitude] country centroids, generated at build time from
 * `world-countries` by the `country-centroids` plugin in `vite.config.ts` (admin GeoMap). */
declare module 'virtual:country-centroids' {
  const centroids: Record<string, [number, number]>
  export default centroids
}
