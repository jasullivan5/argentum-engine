package com.wingedsheep.mtg.sets.legality

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeToSequence
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Offline sync against a Scryfall bulk-data dump (the "All Cards" or "Default Cards" JSON).
 * Walks every printing in the dump, unions the per-format `legal` flags (and `restricted`, written
 * as `<FORMAT>_RESTRICTED`) across same-named printings, and writes the result to per-letter files in `mtg-sets/src/main/resources/`.
 *
 * Run: `./gradlew :mtg-sets:syncLegalityFromDump --args="/path/to/all-cards-YYYYMMDDhhmmss.json"`.
 *
 * Why per-letter:
 *  - Bundling every Scryfall card in a single JSON resource yields a multi-megabyte file that's
 *    awkward to diff and reload. Splitting by first-letter bucket (`legalities_a.json` …
 *    `legalities_z.json`, plus `legalities_other.json` for the long tail) keeps individual files
 *    small and lets [LegalityData] load buckets lazily on demand.
 *
 * Why we union across printings: tokens, art-series cards, and playtest reprints share names
 * with real cards but carry empty/`not_legal` legalities. Picking the first match would corrupt
 * popular names; the union is safe because Scryfall's per-format legality is a property of the
 * card (oracle id), not of any individual printing.
 *
 * Why we keep *every* Scryfall format key: deck-construction formats come and go (Alchemy,
 * Brawl, Standard Brawl…). Persisting the full set means adding a new [DeckFormat] enum value
 * later doesn't require re-running the sync — the data is already there.
 *
 * Why we keep *every* card: registering a new card in the engine should not require re-syncing
 * legalities. By storing all Scryfall printings up front, [LegalityData.stamp] can resolve any
 * future card immediately.
 */
private val dumpMapSerializer = MapSerializer(String.serializer(), ListSerializer(String.serializer()))
private val dumpOutputJson = Json { prettyPrint = true; prettyPrintIndent = "  " }

@OptIn(ExperimentalSerializationApi::class)
fun main(args: Array<String>) {
    val dumpArg = args.firstOrNull()
        ?: error("Usage: syncLegalityFromDump <path-to-scryfall-bulk-cards.json>")
    val dumpPath = Paths.get(dumpArg)
    require(Files.exists(dumpPath)) { "Dump not found: $dumpPath" }

    val outDir = Paths.get("mtg-sets/src/main/resources")
    Files.createDirectories(outDir)

    println("Scanning $dumpPath (every card, every format)…")

    // Two-level accumulator: cardName → (formatKey → legal?). Using a nested map keeps the union
    // semantics simple — a printing votes "legal" by setting the value, and we never overwrite
    // a true with a false.
    val legalByName = sortedMapOf<String, MutableSet<String>>()
    val parser = Json { ignoreUnknownKeys = true; isLenient = false }

    var scanned = 0L
    Files.newInputStream(dumpPath).use { stream ->
        val cards = parser.decodeToSequence(stream, ScryfallDumpCard.serializer())
        for (card in cards) {
            scanned++
            if (scanned % 200_000 == 0L) {
                println("  scanned $scanned printings (${legalByName.size} unique names so far)")
            }
            val rawName = card.name ?: continue
            // Split / DFC / adventure printings come through as "Front // Back". We index both
            // the joined name and the front face so a CardDefinition carrying just "Front" still
            // resolves. Don't index empty/blank names (a few token entries in older dumps).
            val frontFace = rawName.substringBefore(" // ").trim()
            val names = if (frontFace == rawName.trim()) listOf(rawName) else listOf(rawName, frontFace)
            for (name in names) {
                if (name.isBlank()) continue
                val bucket = legalByName.getOrPut(name) { sortedSetOf() }
                for ((formatKey, status) in card.legalities) {
                    when (status) {
                        "legal" -> bucket += formatKey.uppercase()
                        // Vintage's restricted list: legal as a single copy. Kept apart from
                        // "legal" because Vintage itself caps these at one, and the Highlander
                        // formats read Vintage's pool as legal-or-restricted.
                        "restricted" -> bucket += formatKey.uppercase() + LegalityData.RESTRICTED_SUFFIX
                    }
                }
            }
        }
    }
    println("Finished scanning $scanned printings → ${legalByName.size} unique names.")

    // Partition into per-letter buckets and write each file.
    val byBucket = sortedMapOf<String, java.util.TreeMap<String, List<String>>>()
    for ((name, formats) in legalByName) {
        val bucket = LegalityData.bucketFor(name)
        val target = byBucket.getOrPut(bucket) { java.util.TreeMap() }
        // Cards a future engine release might not have any "legal" Scryfall row for (banned in
        // every supported format) still get an entry so we can distinguish "known card with no
        // legal home" from "name not in dump". The empty list reads as the former.
        target[name] = formats.toList()
    }

    // Names absent from the dump entirely are simply absent from byBucket. We don't synthesize
    // NOT_FOUND markers — that sentinel only makes sense for the live syncer (which gets a real
    // 404 from Scryfall). The loader treats a missing entry the same way as the marker.

    deleteOldOutputs(outDir)

    var totalEntries = 0
    for ((bucket, entries) in byBucket) {
        val outPath = outDir.resolve("legalities_$bucket.json")
        Files.writeString(outPath, dumpOutputJson.encodeToString(dumpMapSerializer, entries))
        totalEntries += entries.size
        println("  legalities_$bucket.json: ${entries.size} cards")
    }
    println("Wrote $totalEntries entries across ${byBucket.size} bucket files in $outDir.")
}

private fun deleteOldOutputs(outDir: Path) {
    // Drop the legacy monolithic file plus any pre-existing per-letter files so a deletion (a
    // letter that no longer has any cards) actually drops out of the bundle. We don't `rm -rf`
    // the resources dir — it carries other resources we mustn't touch.
    val monolith = outDir.resolve("legalities.json")
    if (Files.exists(monolith)) Files.delete(monolith)
    Files.list(outDir).use { stream ->
        stream
            .filter { it.fileName.toString().startsWith("legalities_") && it.fileName.toString().endsWith(".json") }
            .forEach(Files::delete)
    }
}

@Serializable
private data class ScryfallDumpCard(
    val name: String? = null,
    val legalities: Map<String, String> = emptyMap(),
)
