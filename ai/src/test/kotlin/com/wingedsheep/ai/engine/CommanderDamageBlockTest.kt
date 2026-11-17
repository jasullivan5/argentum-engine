package com.wingedsheep.ai.engine

import com.wingedsheep.ai.puzzles.advanceToDeclaration
import com.wingedsheep.ai.puzzles.seatId
import com.wingedsheep.engine.core.DeclareAttackers
import com.wingedsheep.engine.core.DeclareBlockers
import com.wingedsheep.engine.state.components.battlefield.CountersComponent
import com.wingedsheep.engine.state.components.identity.CommanderComponent
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.CounterType
import com.wingedsheep.sdk.core.Format
import com.wingedsheep.sdk.core.Step
import io.kotest.assertions.withClue
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldNotBeEmpty
import io.kotest.matchers.shouldBe

/**
 * Commander damage is a second life total, and the blocker — and the attacker reading its
 * crack-back — has to price it (CR 903.10a: 21 — or
 * the format's threshold — combat damage from one commander loses the game).
 *
 * Production game `1cca8ef9` (Tarkir: Dragonstorm commander sealed, threshold 16) turn 13: the
 * opposing commander, a 6/6 trampling Eshki Dragonclaw, had already dealt the AI 10. The AI sat at
 * 17 life with Watcher of the Wayside, Underfoot Underdogs and a 1/1 Goblin untapped, declared no
 * blocks, took 6 — 16 commander damage — and lost at 11 life. Any one blocker keeps the tally
 * at 15 or lower; the life total never made the hit look lethal, so no chump pass ran.
 *
 * Seat 1 attacks with its commander, seat 2 is the AI declaring blockers. The crack-back case
 * turns the same position around: the AI attacking with its only blocker opens the hit that
 * finishes the commander count, however many life points it has.
 */
class CommanderDamageBlockTest : ScenarioTestBase() {

    private fun blocksFor(priorCommanderDamage: Int): Map<String, List<String>> {
        val game = scenario().withRngSeed(20261029L).withPlayers()
            .withFormat(Format.Commander(commanderDamageThreshold = 16, deckSize = 60, startingLife = 25))
            .withCardOnBattlefield(1, "Eshki Dragonclaw")
            .withCardOnBattlefield(1, "Dragonologist")
            .withCardOnBattlefield(2, "Watcher of the Wayside")
            .withCardOnBattlefield(2, "Underfoot Underdogs")
            .withCardOnBattlefield(2, "Raging Goblin")
            .withLifeTotal(1, 18)
            .withLifeTotal(2, 17)
            .build()

        val eshki = game.findPermanents("Eshki Dragonclaw").single()
        game.state = game.state
            .updateEntity(eshki) {
                it.with(CommanderComponent(ownerId = game.seatId(1)))
                    .with(CountersComponent().withAdded(CounterType.PLUS_ONE_PLUS_ONE, 2))
            }
            .recordCommanderDamage(eshki, game.seatId(2), priorCommanderDamage)

        game.advanceToDeclaration(1, Step.DECLARE_ATTACKERS)
        game.execute(DeclareAttackers(game.seatId(1), mapOf(eshki to game.seatId(2))))
        game.advanceToDeclaration(2, Step.DECLARE_BLOCKERS)

        val action = AIPlayer.create(cardRegistry, game.seatId(2), AiProfile.LIVE).chooseAction(game.state)
        val name = { id: com.wingedsheep.sdk.model.EntityId ->
            game.state.getEntity(id)!!.get<com.wingedsheep.engine.state.components.identity.CardComponent>()!!.name
        }
        return (action as? DeclareBlockers)?.blockers.orEmpty()
            .filterValues { it.isNotEmpty() }
            .map { (blocker, blocked) -> name(blocker) to blocked.map(name) }
            .toMap()
    }

    /**
     * The AI (seat 1, 25 life) holds one Grizzly Bears against the opponent's tapped commander
     * (Eshki Dragonclaw 6/6, trample) that has already dealt it [priorCommanderDamage] of 16.
     */
    private fun attacksWithBears(priorCommanderDamage: Int): Boolean {
        val game = scenario().withRngSeed(20261029L).withPlayers()
            .withFormat(Format.Commander(commanderDamageThreshold = 16, deckSize = 60, startingLife = 25))
            .withCardOnBattlefield(1, "Grizzly Bears")
            .withCardOnBattlefield(2, "Eshki Dragonclaw", tapped = true)
            .withLifeTotal(1, 25)
            .withLifeTotal(2, 20)
            .build()
        val eshki = game.findPermanents("Eshki Dragonclaw").single()
        game.state = game.state
            .updateEntity(eshki) {
                it.with(CommanderComponent(ownerId = game.seatId(2)))
                    .with(CountersComponent().withAdded(CounterType.PLUS_ONE_PLUS_ONE, 2))
            }
            .recordCommanderDamage(eshki, game.seatId(1), priorCommanderDamage)
        game.advanceToDeclaration(1, Step.DECLARE_ATTACKERS)
        val action = AIPlayer.create(cardRegistry, game.seatId(1), AiProfile.LIVE).chooseAction(game.state)
        return (action as? DeclareAttackers)?.attackers.orEmpty().isNotEmpty()
    }

    init {
        test("game 1cca8ef9 turn 13: the AI blocks the commander that would deal its 16th damage") {
            val blocks = blocksFor(priorCommanderDamage = 10)
            withClue("blocks were $blocks") {
                blocks.filterValues { "Eshki Dragonclaw" in it }.shouldNotBeEmpty()
            }
        }

        test("crack-back: the AI keeps home the blocker that stops the commander's lethal hit") {
            attacksWithBears(priorCommanderDamage = 10) shouldBe false
        }

        test("crack-back: with no commander damage taken, the free attack goes in") {
            attacksWithBears(priorCommanderDamage = 0) shouldBe true
        }

        test("with no commander damage taken, 6 from 17 life is no reason to chump") {
            val blocks = blocksFor(priorCommanderDamage = 0)
            withClue("blocks were $blocks") { blocks.shouldBeEmpty() }
        }
    }
}
