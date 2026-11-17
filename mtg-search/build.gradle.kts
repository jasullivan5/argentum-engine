import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrLink

plugins {
    // Multiplatform so the one search implementation runs in both places it is needed: on the JVM
    // for the server (`/api/cards/search`, deck-spec validation) and in the browser, where the
    // deckbuilder filters the in-memory catalog on every keystroke without a round trip.
    kotlin("multiplatform")
}

kotlin {
    jvmToolchain(21)

    jvm()

    js {
        // A plain ES-module library; the web-client's Vite build is the bundler. Whole-program
        // granularity (gradle.properties) makes it one self-contained .mjs with the stdlib inlined.
        outputModuleName.set("mtg-search")
        nodejs()
        useEsModules()
        binaries.library()
        generateTypeScriptDefinitions()
    }

    sourceSets {
        // Pure Kotlin — no Spring, no rules-engine, no mtg-sdk. The search language operates on a
        // [SearchCard] projection defined in this module, so consumers adapt their own card
        // representation by implementing the interface.
        jvmTest.dependencies {
            implementation(libs.kotestRunner)
            implementation(libs.kotestAssertions)
            implementation(libs.kotestProperty)
        }
    }
}

// Source maps embed absolute paths, so the checked-in copy would differ per machine.
tasks.withType<KotlinJsIrLink>().configureEach {
    compilerOptions.sourceMap.set(false)
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}

// ---------------------------------------------------------------------------------------------
// The web-client's copy of the browser build.
//
// The web-client builds with plain npm (its own CI job and Docker context), so it can't run
// Gradle; it imports the compiled module from `web-client/src/generated/mtg-search/`, checked in.
// `just search-js` refreshes that copy, and `verifyWebClientSearch` (part of `test`) fails when it
// no longer matches what this module compiles to — the same contract as `docs/sdk-index.md`.
// ---------------------------------------------------------------------------------------------

val webClientSearchDir = rootProject.layout.projectDirectory.dir("web-client/src/generated/mtg-search")
val jsLibrary = tasks.named<KotlinJsIrLink>("compileProductionLibraryKotlinJs") {
    // The plugin finalizes the link with an npm-package sync whose TypeScript check downloads Node
    // and runs an npm install. The web-client reads the link output directly and type-checks it
    // itself, so a plain compile — what `test` and `just search-js` run — skips all of that.
    setFinalizedBy(emptyList<Any>())
}
// Read without the task wiring a Provider would carry, which would bring the sync task back.
val jsLibraryDir = provider { jsLibrary.get().destinationDirectory.get().asFile }

val syncWebClientSearch by tasks.registering(Sync::class) {
    group = "build"
    description = "Copies the compiled JS search module into web-client/src/generated/mtg-search."
    dependsOn(jsLibrary)
    from(jsLibraryDir) { include("*.mjs", "*.d.mts") }
    into(webClientSearchDir)
    preserve { include("README.md") }
}

val verifyWebClientSearch by tasks.registering {
    group = "verification"
    description = "Fails when web-client/src/generated/mtg-search is stale. Fix: just search-js."
    dependsOn(jsLibrary)
    mustRunAfter(syncWebClientSearch) // `just search-js test` checks the fresh copy
    val built = jsLibraryDir
    val checkedIn = webClientSearchDir.asFile
    inputs.dir(checkedIn)
    doLast {
        // The Kotlin/JS compiler lists a class's interfaces in `initMetadataFor*(…, [KtSet, Collection])`
        // in an order that varies between compiles of identical source, so those lists are compared
        // as sets; anything else that differs is a real change.
        val bracketList = Regex("""\[([^\[\]]*)]""")
        fun canonical(text: String) = text.lines().joinToString("\n") { line ->
            if (!line.startsWith("initMetadataFor")) line
            else line.replace(bracketList) { m -> m.groupValues[1].split(", ").sorted().joinToString(", ", "[", "]") }
        }
        val stale = built.get().listFiles { f -> f.name.endsWith(".mjs") || f.name.endsWith(".d.mts") }!!
            .filter { file ->
                val copy = checkedIn.resolve(file.name)
                !copy.exists() || canonical(copy.readText()) != canonical(file.readText())
            }
        if (stale.isNotEmpty()) {
            throw GradleException(
                "web-client/src/generated/mtg-search is stale (${stale.joinToString { it.name }}). " +
                    "Run `just search-js` and commit the result.",
            )
        }
    }
}

// `test` is what `just test` and CI's `sdk` group run; a multiplatform module has no task by
// that name. JS tests are left out: there are none, and the browser-side checks of this code are
// the web-client's vitest suites, which run against the compiled copy.
tasks.register("test") {
    group = "verification"
    description = "Runs the JVM tests and checks the web-client's copy of the JS build is current."
    dependsOn("jvmTest", verifyWebClientSearch)
}

tasks.named("check") { dependsOn(verifyWebClientSearch) }
