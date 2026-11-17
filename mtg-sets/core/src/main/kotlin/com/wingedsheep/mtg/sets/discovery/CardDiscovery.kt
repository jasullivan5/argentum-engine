package com.wingedsheep.mtg.sets.discovery

import com.wingedsheep.sdk.model.BasicLandArt
import com.wingedsheep.sdk.model.CardDefinition
import com.wingedsheep.sdk.model.MtgSet
import com.wingedsheep.sdk.model.Printing
import io.github.classgraph.ClassGraph
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap

/**
 * Auto-discovers card-shaped values declared as top-level Kotlin `val`s in a given package,
 * so a set's `cards`, `basicLands`, and `printings` lists do not have to be maintained by hand.
 *
 * Each file in `definitions/{set}/cards/` declares one of:
 *   - `val Foo = card("Foo") { ... }` — a [CardDefinition] for a new card,
 *   - `val FooPlains1 = basicLand("Plains") { ... }` — a basic-land variant (also a CardDefinition),
 *   - `val FooReprint = Printing(...)` — a [Printing] row for a card that already exists
 *     elsewhere in the codebase, contributing this set's art / collector number.
 *
 * Those compile to static fields on a synthetic `*Kt` class per file; we scan the package,
 * read those fields reflectively, and split them into:
 *
 *   - [findIn] — non-basic-land [CardDefinition]s, suitable for `MtgSet.cards`.
 *   - [findBasicLandsIn] — basic-land [CardDefinition]s, suitable for `MtgSet.basicLands`.
 *   - [findPrintingsIn] — [Printing] rows, suitable for `MtgSet.printings`.
 *
 * The split is by static type + `typeLine.isBasicLand`. Aggregate vals like
 * `val FooBasicLands = listOf(...)` are skipped automatically because their field type
 * is `List<CardDefinition>`, not `CardDefinition`.
 *
 * `cards` are stamped with their `setCode` centrally at registry-load time
 * (`GameBeansConfig.stamp(...)`), so [findIn] returns them unstamped. `basicLands`, on the
 * other hand, are consumed directly off `MtgSet.basicLands` by the booster/sealed/random-deck
 * paths (which never go through the card registry and key land identifiers on `setCode`), so
 * they must carry their `setCode` at the source: use the [findBasicLandsIn] overload that takes
 * a `setCode` to stamp them in one place instead of each set repeating `.copy(setCode = code)`.
 * Printings already carry their own `setCode` so no stamping is needed.
 *
 * [findSets] discovers the set objects themselves the same way, so `MtgSetCatalog` no longer has
 * to hand-maintain an import block plus a parallel list — both of which were easy to forget.
 */
object CardDiscovery {

    private data class ScannedPackage(
        val cards: List<CardDefinition>,
        val basicLands: List<CardDefinition>,
        val printings: List<Printing>,
    )

    private val cache = ConcurrentHashMap<String, ScannedPackage>()

    /** Every set and every card lives under here; see [corpusIndex]. */
    private const val CORPUS_PACKAGE = "com.wingedsheep.mtg.sets.definitions"

    /** Class names found by a classpath scan: by package, plus the [MtgSet] implementations. */
    private class ClassIndex(
        val classNamesByPackage: Map<String, List<String>>,
        val setClassNames: List<String>,
    ) {
        /** Class names in [packageName] and its subpackages, matching ClassGraph's `acceptPackages`. */
        fun classNamesUnder(packageName: String): List<String> =
            classNamesByPackage.flatMap { (pkg, names) ->
                if (pkg == packageName || pkg.startsWith("$packageName.")) names else emptyList()
            }
    }

    /**
     * One scan of the whole card corpus, shared by every set.
     *
     * Each set's `cards`, `basicLands` and `printings` used to run its own ClassGraph scan, and a
     * scan reopens every jar on the classpath however narrow its package filter — several hundred
     * sets made that a large slice of server startup, most of it on a fat jar's nested jars.
     */
    private val corpusIndex: ClassIndex by lazy { indexClasses(CORPUS_PACKAGE) }

    private fun indexClasses(packageName: String): ClassIndex =
        ClassGraph()
            .acceptPackages(packageName)
            .enableClassInfo()
            .scan()
            .use { scanResult ->
                ClassIndex(
                    classNamesByPackage = scanResult.allClasses.groupBy({ it.packageName }, { it.name }),
                    setClassNames = scanResult.getClassesImplementing(MtgSet::class.java.name)
                        .filter { !it.isInterface && !it.isAbstract }
                        .map { it.name },
                )
            }

    private fun indexFor(packageName: String): ClassIndex =
        if (packageName == CORPUS_PACKAGE || packageName.startsWith("$CORPUS_PACKAGE.")) corpusIndex
        else indexClasses(packageName)

    private fun loadClassOrNull(name: String): Class<*>? =
        runCatching { Class.forName(name, false, CardDiscovery::class.java.classLoader) }.getOrNull()

    fun findIn(packageName: String): List<CardDefinition> =
        cache.getOrPut(packageName) { scan(packageName) }.cards

    fun findBasicLandsIn(packageName: String): List<CardDefinition> =
        cache.getOrPut(packageName) { scan(packageName) }.basicLands

    /**
     * Like [findBasicLandsIn], but stamps each variant with [setCode]. This is the single place
     * basic lands get their set identity — sets call this instead of repeating
     * `.copy(setCode = code)`, so the stamp can't drift between sets and consumers that read
     * `MtgSet.basicLands` directly (booster/sealed/random-deck) always see a set-stamped land.
     */
    fun findBasicLandsIn(packageName: String, setCode: String): List<CardDefinition> =
        findBasicLandsIn(packageName).map { it.copy(setCode = setCode) }

    fun findPrintingsIn(packageName: String): List<Printing> =
        cache.getOrPut(packageName) { scan(packageName) }.printings

    /**
     * Auto-discovers every [MtgSet] implementation under [packageName] (recursively), so the
     * set catalog is a view over the classpath instead of a hand-maintained import block + list.
     *
     * Every set is declared as a Kotlin `object`, which compiles to a class carrying a public
     * static `INSTANCE` field — that's the singleton we read. Non-object implementations (none
     * today) are skipped: they have no `INSTANCE`, and a set with no stable singleton has no
     * place in the catalog. Ordering is non-deterministic here; callers sort.
     */
    fun findSets(packageName: String): List<MtgSet> =
        indexFor(packageName).setClassNames
            .filter { it.startsWith("$packageName.") }
            .mapNotNull { name ->
                val cls = loadClassOrNull(name) ?: return@mapNotNull null
                runCatching { cls.getField("INSTANCE").get(null) as? MtgSet }.getOrNull()
            }

    private fun scan(packageName: String): ScannedPackage {
        val cards = mutableListOf<CardDefinition>()
        val basicLands = mutableListOf<CardDefinition>()
        val printings = mutableListOf<Printing>()
        for (className in indexFor(packageName).classNamesUnder(packageName)) {
            if (!className.endsWith("Kt")) continue
            val cls = loadClassOrNull(className) ?: continue
            // A top-level `val` produces a private static field plus a public static
            // getter. The getter's return type is what we trust as the val's declared
            // type; the field's name pairs them up. This filters out synthetic helpers
            // (private-by-default) and aggregate `List<...>` vals.
            val exposedFields = cls.declaredMethods
                .asSequence()
                .filter { Modifier.isStatic(it.modifiers) && Modifier.isPublic(it.modifiers) }
                .filter { it.parameterCount == 0 }
                .filter { it.returnType == CardDefinition::class.java || it.returnType == Printing::class.java }
                .filter { it.name.startsWith("get") && it.name.length > 3 }
                .associateBy(
                    { it.name.substring(3).replaceFirstChar { c -> c.lowercase() } },
                    { it.returnType },
                )
            for (field in cls.declaredFields) {
                if (!Modifier.isStatic(field.modifiers)) continue
                val key = field.name.replaceFirstChar { it.lowercase() }
                val declaredType = exposedFields[key] ?: continue
                field.isAccessible = true
                when (declaredType) {
                    CardDefinition::class.java -> {
                        val v = field.get(null) as? CardDefinition ?: continue
                        if (v.typeLine.isBasicLand) basicLands += v else cards += v
                    }
                    Printing::class.java -> {
                        val v = field.get(null) as? Printing ?: continue
                        printings += v
                    }
                }
            }
        }
        return ScannedPackage(
            cards = cards.sortedBy { it.name },
            // Name alone is not a total order for basics — every art variant of a type shares one
            // name, so a stable sort by name would leave the within-type order at the mercy of
            // whatever order reflection happened to yield this run. Break the tie on collector
            // number so `basicLands` is deterministic and each type leads with its standard art
            // (see [BasicLandArt]); limited deck building takes that first variant.
            basicLands = basicLands.sortedWith(compareBy<CardDefinition> { it.name } then BasicLandArt.standardFirst),
            // Sort by name first so `printings.toString()` reads naturally; the
            // (setCode, collectorNumber) tiebreakers are only relevant if a future
            // discovery picks up >1 reprint of the same card from the same package.
            printings = printings.sortedWith(
                compareBy({ it.name }, { it.setCode }, { it.collectorNumber }),
            ),
        )
    }
}
