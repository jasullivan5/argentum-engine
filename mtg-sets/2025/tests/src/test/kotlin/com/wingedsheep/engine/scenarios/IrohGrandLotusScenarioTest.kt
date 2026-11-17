package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Phase
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

/**
 * Iroh, Grand Lotus {3}{G}{U}{R} — Legendary Creature — Human Noble Ally (5/5)
 *  - Firebending 2
 *  - "During your turn, each non-Lesson instant and sorcery card in your graveyard has flashback.
 *     The flashback cost is equal to that card's mana cost."
 *  - "During your turn, each Lesson card in your graveyard has flashback {1}."
 *
 * The two clauses are whole-graveyard flashback grants ([GraveyardCardsHaveCastKeyword], CR 702.34):
 * a continuous static that grants flashback to *every* matching card in your graveyard, gated to
 * your turn. These tests pin: a non-Lesson instant/sorcery becomes castable from the graveyard for
 * its own mana cost (and is exiled on resolution — flashback); a Lesson becomes castable for {1};
 * the grant is off on an opponent's turn and absent without Iroh; and non-instant/sorcery cards
 * (creatures) are never granted flashback.
 */
class IrohGrandLotusScenarioTest : ScenarioTestBase() {

    init {
        context("Iroh, Grand Lotus — turn-gated whole-graveyard flashback") {

            test("non-Lesson sorcery in your graveyard flashes back for its mana cost, then is exiled") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Iroh, Grand Lotus")
                    .withCardInGraveyard(1, "Divination")
                    .withCardInLibrary(1, "Lightning Bolt")
                    .withCardInLibrary(1, "Counterspell")
                    .withLandsOnBattlefield(1, "Island", 3)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                // The enumerated flashback action advertises the card's own mana cost ({2}{U}).
                val fb = game.getLegalActions(1).firstOrNull {
                    it.actionType == "CastWithFlashback" && it.description.contains("Divination")
                }
                withClue("Divination is offered with flashback while Iroh is in play on your turn") {
                    fb.shouldNotBeNull()
                }
                fb!!.manaCostString shouldBe "{2}{U}"

                val handBefore = game.handSize(1)
                game.castSpellFromGraveyard(1, "Divination").error shouldBe null
                game.resolveStack()

                withClue("Divination drew two cards") {
                    game.handSize(1) shouldBe handBefore + 2
                }
                withClue("Flashback exiles Divination — it leaves the graveyard for exile") {
                    game.isInExile(1, "Divination") shouldBe true
                    game.isInGraveyard(1, "Divination") shouldBe false
                }
            }

            test("Lesson card in your graveyard flashes back for {1}") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Iroh, Grand Lotus")
                    .withCardInGraveyard(1, "Firebending Lesson")
                    .withLandsOnBattlefield(1, "Mountain", 1)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                // Firebending Lesson (Instant — Lesson) targets a creature; Iroh itself is a legal
                // target, so the flashback action is offered — for the fixed {1}, not its {R} cost.
                val fb = game.getLegalActions(1).firstOrNull {
                    it.actionType == "CastWithFlashback" && it.description.contains("Firebending Lesson")
                }
                withClue("Firebending Lesson is offered with flashback while Iroh is in play") {
                    fb.shouldNotBeNull()
                }
                fb!!.manaCostString shouldBe "{1}"
            }

            test("on an opponent's turn the flashback grant is inactive") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Iroh, Grand Lotus")
                    // Lightning Strike is an instant, so timing never blocks it — isolating the
                    // "during your turn" gate as the reason it can't be flashed back.
                    .withCardInGraveyard(1, "Lightning Strike")
                    .withCardOnBattlefield(2, "Grizzly Bears")
                    .withLandsOnBattlefield(1, "Mountain", 2)
                    .withActivePlayer(2)
                    .withPriorityPlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val flashbackActions = game.getLegalActions(1).filter {
                    it.actionType == "CastWithFlashback"
                }
                withClue("Iroh's grant is 'during your turn' — off on Player2's turn") {
                    flashbackActions.shouldBeEmpty()
                }
            }

            test("without Iroh in play, graveyard cards are not granted flashback") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardInGraveyard(1, "Divination")
                    .withCardInGraveyard(1, "Firebending Lesson")
                    // A creature is present so a missing Lesson action can't be blamed on "no target".
                    .withCardOnBattlefield(1, "Grizzly Bears")
                    .withLandsOnBattlefield(1, "Island", 3)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val flashbackActions = game.getLegalActions(1).filter {
                    it.actionType == "CastWithFlashback"
                }
                withClue("No granter on the battlefield — no flashback is offered") {
                    flashbackActions.shouldBeEmpty()
                }
            }

            test("a creature card in your graveyard is not granted flashback") {
                val game = scenario()
                    .withPlayers("Player1", "Player2")
                    .withCardOnBattlefield(1, "Iroh, Grand Lotus")
                    .withCardInGraveyard(1, "Grizzly Bears")
                    .withLandsOnBattlefield(1, "Forest", 3)
                    .withActivePlayer(1)
                    .inPhase(Phase.PRECOMBAT_MAIN, Step.PRECOMBAT_MAIN)
                    .build()

                val flashbackActions = game.getLegalActions(1).filter {
                    it.actionType == "CastWithFlashback" && it.description.contains("Grizzly Bears")
                }
                withClue("Flashback only applies to instant/sorcery cards — not creatures") {
                    flashbackActions.shouldBeEmpty()
                }
            }
        }
    }
}
