package com.wingedsheep.ai.engine

import com.wingedsheep.ai.llm.CardSummary
import com.wingedsheep.ai.llm.MulliganInfo
import com.wingedsheep.engine.registry.CardRegistry
import com.wingedsheep.sdk.model.EntityId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * [AiProfile.curveAwareMulligan]: a seven-card 2–5-lander is kept only when it can cast something
 * in its colours, and a two-lander only when it has early plays off those two lands. Opening hands
 * from the 2026-10-31 AI-vs-humans review, plus the ordinary keeps that must stay keeps.
 */
class EngineAiMulliganCurveTest : FunSpec({

    val registry = CardRegistry()
    val live = EngineAiPlayerController(registry, EntityId.of("ai"), gameStateProvider = { null })
    val without = EngineAiPlayerController(
        registry, EntityId.of("ai"), gameStateProvider = { null },
        profile = AiProfile.LIVE.copy(id = "live-without-curve-mulligan", curveAwareMulligan = false),
    )

    fun land(name: String, typeLine: String = "Basic Land — $name", oracle: String? = null) =
        CardSummary(name = name, typeLine = typeLine, oracleText = oracle)

    fun spell(name: String, cost: String, typeLine: String = "Creature") =
        CardSummary(name = name, manaCost = cost, typeLine = typeLine)

    fun hand(
        vararg cards: CardSummary,
        onThePlay: Boolean,
        mulligans: Int = 0,
        bottom: Int = mulligans,
    ): MulliganInfo {
        val ids = cards.indices.map { EntityId.of("card-$it") }
        return MulliganInfo(
            hand = ids, mulliganCount = mulligans, cardsToPutOnBottom = bottom,
            cards = ids.zip(cards).toMap(), isOnThePlay = onThePlay,
        )
    }

    // 088bcff0 turn 1, on the play: two lands, nothing below mana value 3. Sat on three lands to turn 11.
    val topHeavyTwoLander = arrayOf(
        spell("Serra Angel", "{3}{W}{W}"), land("Plains"), spell("Triskelion", "{6}", "Artifact Creature"),
        spell("Shivan Dragon", "{4}{R}{R}"), spell("Serra Angel", "{3}{W}{W}"), land("Mountain"),
        spell("Sword of Forge and Frontier", "{3}", "Artifact — Equipment"),
    )

    test("088bcff0: a two-lander of 3–6 drops on the play is mulliganed") {
        live.decideMulligan(hand(*topHeavyTwoLander, onThePlay = true)) shouldBe false
    }

    test("088bcff0: without the flag the land count alone keeps it") {
        without.decideMulligan(hand(*topHeavyTwoLander, onThePlay = true)) shouldBe true
    }

    test("844df659: a two-lander with no play below mana value 3 is mulliganed on the draw too") {
        live.decideMulligan(
            hand(
                land("Swamp"), spell("Ruric Thar, Magecrusher", "{5}{G}{G}"),
                spell("Greenhouse Propagator", "{2}{G}"), spell("Winter, Tormented Loner", "{2}{B}"),
                land("Forest"), spell("Restore with Empathy", "{2}{G}", "Sorcery"),
                spell("Way of the Paradox", "{2}{G}", "Sorcery"),
                onThePlay = false,
            ),
        ) shouldBe false
    }

    test("849f7063: a two-lander with a one-drop and a two-drop in its colours is kept on the play") {
        live.decideMulligan(
            hand(
                spell("Memory Trap", "{2}{W}", "Instant"), spell("Traxos, Scourge Eternal", "{4}", "Artifact Creature"),
                spell("Lich's Relic", "{B}", "Artifact"), land("Swamp"),
                spell("Multiply by Zero", "{1}{B}", "Instant"), land("Plains"),
                spell("Way of the Healer", "{3}{W}", "Sorcery"),
                onThePlay = true,
            ),
        ) shouldBe true
    }

    test("a two-lander with one cheap spell and a three-drop is kept on the draw") {
        live.decideMulligan(
            hand(
                land("Forest"), land("Mountain"), spell("Bear", "{1}{G}"), spell("Ogre", "{2}{R}"),
                spell("Giant", "{4}{G}"), spell("Dragon", "{4}{R}{R}"), spell("Wurm", "{5}{G}{G}"),
                onThePlay = false,
            ),
        ) shouldBe true
    }

    test("a two-lander whose only cheap spell needs a colour the lands don't make goes back on the draw") {
        live.decideMulligan(
            hand(
                land("Forest"), land("Forest"), spell("Shock Trooper", "{1}{R}"), spell("Ogre", "{2}{R}"),
                spell("Elf", "{2}{G}"), spell("Giant", "{4}{G}"), spell("Wurm", "{5}{G}{G}"),
                onThePlay = false,
            ),
        ) shouldBe false
    }

    test("an ordinary three-lander with a curve is kept") {
        live.decideMulligan(
            hand(
                land("Plains"), land("Island"), land("Plains"), spell("Knight", "{1}{W}"),
                spell("Drake", "{3}{U}"), spell("Angel", "{3}{W}{W}"), spell("Sphinx", "{5}{U}{U}"),
                onThePlay = true,
            ),
        ) shouldBe true
    }

    test("a three-lander of the wrong colours for every spell is mulliganed") {
        live.decideMulligan(
            hand(
                land("Plains"), land("Plains"), land("Plains"), spell("Goblin", "{R}"),
                spell("Ogre", "{2}{R}"), spell("Dragon", "{4}{R}{R}"), spell("Bolt", "{R}", "Instant"),
                onThePlay = false,
            ),
        ) shouldBe false
    }

    test("dual lands and hybrid costs are read by their colours") {
        val dual = land("Hallowed Fountain", "Land — Plains Island")
        val textDual = land("Coastal Tower", "Land", "{T}: Add {W} or {U}.")
        live.decideMulligan(
            hand(
                dual, textDual, spell("Hybrid Bear", "{W/U}{W/U}"), spell("Spirit", "{1}{U}"),
                spell("Angel", "{3}{W}{W}"), spell("Sphinx", "{5}{U}{U}"), spell("Wall", "{2}{W}"),
                onThePlay = true,
            ),
        ) shouldBe true
    }

    test("a land whose colours can't be read never causes a mulligan") {
        val fetch = land("Evolving Wilds", "Land", "{T}, Sacrifice Evolving Wilds: Search your library for a basic land card, put it onto the battlefield tapped, then shuffle.")
        live.decideMulligan(
            hand(
                fetch, land("Plains"), land("Plains"), spell("Goblin", "{R}"),
                spell("Ogre", "{2}{R}"), spell("Dragon", "{4}{R}{R}"), spell("Bolt", "{R}", "Instant"),
                onThePlay = true,
            ),
        ) shouldBe true
    }

    test("a hand kept at six or fewer is read by its land count alone") {
        live.decideMulligan(hand(*topHeavyTwoLander, onThePlay = true, mulligans = 1)) shouldBe true
    }

    test("Commander's free first mulligan still reads the seven-card hand; the floor keeps after two") {
        live.decideMulligan(hand(*topHeavyTwoLander, onThePlay = true, mulligans = 1, bottom = 0)) shouldBe false
        live.decideMulligan(hand(*topHeavyTwoLander, onThePlay = true, mulligans = 2, bottom = 1)) shouldBe true
    }
})
