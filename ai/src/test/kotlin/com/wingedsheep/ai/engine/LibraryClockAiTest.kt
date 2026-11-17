package com.wingedsheep.ai.engine

import com.wingedsheep.ai.engine.evaluation.LibraryClock
import com.wingedsheep.engine.core.ActivateAbility
import com.wingedsheep.engine.core.GameAction
import com.wingedsheep.engine.core.PlayLand
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.state.components.player.LandDropsComponent
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Format
import com.wingedsheep.sdk.core.Step
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldNotBeInstanceOf

/**
 * [AiProfile.libraryClock] — the evaluator prices how close each library is to running out.
 *
 * Game a7fc150e (2026-10-31 AI review, Momir Basic), AI turn 13: the avatar flipped Territorial
 * Bruntar ("Landfall — exile cards from the top of your library until you exile a nonland card"),
 * then the AI played a Plains. A Momir library is all basic lands, so the trigger exiled all 48
 * cards; the AI decked out two turns later at 20 life. Nothing the evaluator read could see an
 * empty library — only the draw step that follows.
 */
class LibraryClockAiTest : ScenarioTestBase() {

    private val avatar = "Momir Vig, Simic Visionary"
    private val basics = listOf("Plains", "Island", "Swamp", "Mountain", "Forest")
    private val profiles = listOf(AiProfile.LIVE, AiProfile.CURRENT.copy(id = "current-library-clock", libraryClock = true))
    private val before = AiProfile.LIVE.copy(id = "live-without-library-clock", libraryClock = false)

    /** The turn-13 board, Momir pool narrowed to the card the avatar actually flipped. */
    private fun momirPosition(bruntarOut: Boolean): TestGame {
        val builder = scenario()
            .withPlayers()
            .withFormat(Format.MomirBasic(startingLife = 24, eligibleCreatureNames = listOf("Territorial Bruntar")))
            .withCardInCommandZone(1, avatar)
            .withTurnNumber(13)
            .withActivePlayer(1)
        listOf("Forest", "Swamp", "Plains", "Swamp", "Island", "Forest").forEach {
            builder.withCardOnBattlefield(1, it)
        }
        builder.withCardOnBattlefield(1, "Black Cat, Cunning Thief")
        if (bruntarOut) builder.withCardOnBattlefield(1, "Territorial Bruntar")
        listOf("Forest", "Island", "Forest", "Island", "Plains").forEach { builder.withCardInHand(1, it) }
        repeat(48) { builder.withCardInLibrary(1, basics[it % basics.size]) }
        builder.withLandsOnBattlefield(2, "Mountain", 6)
        builder.withCardOnBattlefield(2, "Chandra's Incinerator")
        builder.withCardOnBattlefield(2, "Aven Archer")
        repeat(44) { builder.withCardInLibrary(2, basics[it % basics.size]) }
        return builder.build()
    }

    /** Activate the avatar for X=6 (flipping Bruntar, the pool's one card) and resolve it. */
    private fun TestGame.flipBruntar(profile: AiProfile) {
        state = state.updateEntity(player1Id) { it.with(LandDropsComponent(remaining = 0, maxPerTurn = 1)) }
        val activate = choice(profile).shouldBeInstanceOf<ActivateAbility>()
        activate.xValue shouldBe 6
        execute(activate).error shouldBe null
        resolveStack()
        state.getBattlefield(player1Id).any { nameOf(it) == "Territorial Bruntar" } shouldBe true
        state = state.updateEntity(player1Id) { it.with(LandDropsComponent(remaining = 1, maxPerTurn = 1)) }
    }

    private fun TestGame.choice(profile: AiProfile): GameAction =
        AIPlayer.create(cardRegistry, player1Id, profile).chooseAction(state)

    private fun TestGame.nameOf(id: com.wingedsheep.sdk.model.EntityId): String? =
        state.getEntity(id)?.get<CardComponent>()?.name

    init {
        test("a7fc150e T13: after Momir flips Territorial Bruntar, the AI keeps its land in hand") {
            // Replay the logged order: the activation first (land drop withheld so the AI offers
            // it), then the land drop back on offer with Bruntar on the battlefield.
            momirPosition(bruntarOut = false).run {
                flipBruntar(before)
                choice(before).shouldBeInstanceOf<PlayLand>()
            }
            for (profile in profiles) {
                momirPosition(bruntarOut = false).run {
                    flipBruntar(profile)
                    choice(profile).shouldNotBeInstanceOf<PlayLand>()
                }
            }
        }

        test("Momir with Territorial Bruntar out: the avatar is activated without a land drop first") {
            // The land-first rule for Momir (#4119) must not hold the activation hostage to a land
            // drop that would exile the whole library.
            momirPosition(bruntarOut = true).choice(before).shouldBeInstanceOf<PlayLand>()
            for (profile in profiles) {
                val game = momirPosition(bruntarOut = true)
                val activate = game.choice(profile).shouldBeInstanceOf<ActivateAbility>()
                game.nameOf(activate.sourceId) shouldBe avatar
                activate.xValue shouldBe 6
            }
        }

        test("Territorial Bruntar on a library of nonland cards: the land drop is still played") {
            // The trigger exiles one card and lets us cast it — a land drop like any other, here
            // the one that casts the Hill Giant in hand.
            for (profile in profiles) {
                val game = scenario().withPlayers()
                    .withTurnNumber(9)
                    .withActivePlayer(1)
                    .withLandsOnBattlefield(1, "Mountain", 3)
                    .withCardOnBattlefield(1, "Territorial Bruntar")
                    .withCardInHand(1, "Mountain")
                    .withCardInHand(1, "Hill Giant")
                    .apply { repeat(20) { withCardInLibrary(1, "Hill Giant") } }
                    .apply { repeat(20) { withCardInLibrary(2, "Forest") } }
                    .withLandsOnBattlefield(2, "Forest", 3)
                    .build()
                game.state.step shouldBe Step.PRECOMBAT_MAIN
                game.choice(profile).shouldBeInstanceOf<PlayLand>()
            }
        }

        test("a library far from empty scores nothing; an empty one is most of a loss") {
            LibraryClock.libraryValue(60) shouldBe 0.0
            LibraryClock.libraryValue(LibraryClock.SAFE_CARDS) shouldBe 0.0
            (LibraryClock.libraryValue(0) < LibraryClock.libraryValue(1)) shouldBe true
            (LibraryClock.libraryValue(0) <= -50.0) shouldBe true
        }
    }
}
