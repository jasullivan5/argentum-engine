package com.wingedsheep.ai.engine

import com.wingedsheep.ai.puzzles.advanceToDeclaration
import com.wingedsheep.ai.puzzles.seatId
import com.wingedsheep.engine.core.DeclareAttackers
import com.wingedsheep.engine.state.components.battlefield.CountersComponent
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.CounterType
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.model.EntityId
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.collections.shouldNotContain

/**
 * Attack plans scored by the race ([AiProfile.attacksReadTheRace]): our next swing priced beside
 * theirs, a tapped attacker charged for its missing block once, and the crack-back checked against
 * the defender declining a block that costs it a creature.
 *
 * Built from the 2026-10-15 losses with vanilla creatures. Seat 1 is the AI, declaring attackers;
 * seat 2's creatures are its would-be blockers.
 */
class AttacksReadTheRaceTest : ScenarioTestBase() {

    private companion object {
        const val SCENARIO_SEED = 20261016L
        // [AiProfile.pressWhenAhead] is off too: on its own it also sends the team in 42518dd1
        // (a dominant board pressing a player on 6), so it would hide the reproduction.
        val WITHOUT = AiProfile.LIVE.copy(id = "live-without-race", attacksReadTheRace = false, pressWhenAhead = false)
    }

    private data class Creature(val name: String, val tapped: Boolean = false, val sick: Boolean = false)

    private fun position(
        ours: List<Creature>,
        theirs: List<Creature>,
        ourLife: Int,
        theirLife: Int,
    ): TestGame {
        var builder = scenario().withRngSeed(SCENARIO_SEED).withPlayers()
            .withLifeTotal(1, ourLife)
            .withLifeTotal(2, theirLife)
        ours.forEach { builder = builder.withCardOnBattlefield(1, it.name, tapped = it.tapped, summoningSickness = it.sick) }
        theirs.forEach { builder = builder.withCardOnBattlefield(2, it.name, tapped = it.tapped, summoningSickness = it.sick) }
        return builder.build().advanceToDeclaration(1, Step.DECLARE_ATTACKERS)
    }

    private fun TestGame.attackersChosenBy(profile: AiProfile): Set<EntityId> =
        (AIPlayer.create(cardRegistry, seatId(1), profile).chooseAction(state) as? DeclareAttackers)
            ?.attackers?.keys.orEmpty()

    private fun TestGame.addCounters(cardName: String, type: CounterType, count: Int) {
        val id = findPermanent(cardName)!!
        state = state.updateEntity(id) { c ->
            c.with((c.get<CountersComponent>() ?: CountersComponent()).withCounters(type, count))
        }
    }

    private fun TestGame.names(ids: Collection<EntityId>): List<String> =
        ids.map { state.getEntity(it)?.get<CardComponent>()?.name ?: it.value }

    /**
     * Game 39e8aac6 turn 29 at the AI's declare-attackers step: AI on 3, the opponent on 15 with a
     * 7/7 trampler, a 2/2 and — with [livingLibrary] — a Living Library and nine lands, seven
     * tapped from casting the trampler. Centaur Courser stands in for a 3/3 token.
     */
    private fun ruricThar(livingLibrary: Boolean): TestGame {
        var builder = scenario().withRngSeed(SCENARIO_SEED).withPlayers()
            .withLifeTotal(1, 3)
            .withLifeTotal(2, 15)
            .withCardOnBattlefield(1, "Centaur Courser")
            .withCardOnBattlefield(1, "Tinybones, Pocket Nuisance")
            .withCardOnBattlefield(1, "Tetsuko Umezawa, Fugitive")
            .withCardOnBattlefield(1, "Diviner of Victory")
            .withCardOnBattlefield(1, "Void Extrapolator", summoningSickness = true)
            .withCardOnBattlefield(2, "Ruric Thar, Magecrusher")
            .withCardOnBattlefield(2, "Simulacrum Shaper")
            .withLandsOnBattlefield(1, "Island", 9)
        if (livingLibrary) builder = builder.withCardOnBattlefield(2, "Living Library")
        repeat(9) { i -> builder = builder.withCardOnBattlefield(2, "Forest", tapped = i < 7) }
        repeat(6) { builder = builder.withCardInLibrary(1, "Island").withCardInLibrary(2, "Forest") }
        // Threshold: the Extrapolator is a 3/3.
        repeat(7) { builder = builder.withCardInGraveyard(1, "Island") }
        return builder.build().advanceToDeclaration(1, Step.DECLARE_ATTACKERS)
    }

    init {
        test("a vigilant fatty attacks into a smaller board") {
            val game = position(
                ours = listOf(Creature("Vigilant Baloth")),
                theirs = listOf(Creature("Grizzly Bears"), Creature("Hill Giant")),
                ourLife = 20,
                theirLife = 20,
            )
            val attackers = game.attackersChosenBy(AiProfile.LIVE)
            withClue("attacked with ${game.names(attackers)}") {
                attackers shouldContain game.findPermanent("Vigilant Baloth")!!
            }
        }

        // Game 42518dd1 turn 17: 17–6 up, six power against one untapped 3/3 reach blocker, and
        // no attack. Sending the team costs a flier and leaves the opponent on 2 facing a board
        // that kills them through both their blockers next turn.
        test("42518dd1 turn 17: clearly ahead, the team attacks past a lone reach blocker") {
            val game = position(
                ours = listOf(
                    Creature("Wind Drake"),
                    Creature("Sunspire Griffin"),
                    Creature("Muck Rats"),
                    Creature("Plated Seastrider"),
                    Creature("Pensive Minotaur", sick = true),
                ),
                theirs = listOf(Creature("Norwood Archers"), Creature("Grizzly Bears", tapped = true)),
                ourLife = 17,
                theirLife = 6,
            )
            val before = game.attackersChosenBy(WITHOUT)
            withClue("reproduction: attacked with ${game.names(before)}") { before.shouldBeEmpty() }

            val after = game.attackersChosenBy(AiProfile.LIVE)
            withClue("attacked with ${game.names(after)}") {
                game.names(after) shouldContain "Wind Drake"
                game.names(after) shouldContain "Sunspire Griffin"
            }
        }

        test("a lone small attacker stays home against bigger untapped blockers") {
            val game = position(
                ours = listOf(Creature("Grizzly Bears")),
                theirs = listOf(Creature("Hill Giant"), Creature("Rumbling Baloth")),
                ourLife = 20,
                theirLife = 20,
            )
            val attackers = game.attackersChosenBy(AiProfile.LIVE)
            withClue("attacked with ${game.names(attackers)}") { attackers.shouldBeEmpty() }
        }

        // Game c71b39d3 turn 24: at 1 life the AI swung Skrull 4/4 beside a vigilant 4/7 flier.
        // The simulated defender blocked the Skrull and lost a creature doing it, which left one
        // attacker against the flier — safe. The human took the hit instead, kept both creatures,
        // and swung two attackers into one blocker. Here the predicted block is Craw Wurm trading
        // with the Baloth; declined, the Wurm and the Griffin come back at a lone Angel.
        test("c71b39d3 turn 24: at 1 life the second blocker stays home") {
            val game = position(
                ours = listOf(Creature("Serra Angel"), Creature("Rumbling Baloth")),
                theirs = listOf(Creature("Craw Wurm"), Creature("Sunspire Griffin")),
                ourLife = 1,
                theirLife = 15,
            )
            val baloth = game.findPermanent("Rumbling Baloth")!!
            val before = game.attackersChosenBy(WITHOUT)
            withClue("reproduction: attacked with ${game.names(before)}") { before shouldContain baloth }

            val after = game.attackersChosenBy(AiProfile.LIVE)
            withClue("attacked with ${game.names(after)}") {
                after shouldNotContain baloth
            }
        }

        // Game ee882769 turn 20: at 9 life the AI sent Yargle, its only real blocker, at a Jace
        // token beside its 3/3. Opposite: two 2/1s, a 2/2, a 2/3 flier and four 1/1 fliers that
        // untap, plus a 1/2 locked down by the AI's own Infinite Coursework. Both creatures home
        // block two 2-power attackers and the swing back leaves the AI on 1; either one away and
        // it is lethal. The crack-back estimate counted the locked 1/2, read staying home as
        // lethal too, and with every plan dead the attack won on board score. The swing back came
        // for 10 and the game ended at -1.
        test("ee882769 turn 20: a creature that doesn't untap isn't in the crack-back, so the blockers stay home") {
            var builder = scenario().withRngSeed(SCENARIO_SEED).withPlayers()
                .withLifeTotal(1, 9)
                .withLifeTotal(2, 18)
                .withCardOnBattlefield(1, "Void Extrapolator")
                .withCardOnBattlefield(1, "Yargle, Goliath of Otaria")
                .withLandsOnBattlefield(1, "Swamp", 9)
                .withCardOnBattlefield(2, "Mindseeker Oculus")
                .withCardOnBattlefield(2, "Mindseeker Oculus")
                .withCardOnBattlefield(2, "Fatehold Chronologist")
                .withCardOnBattlefield(2, "Grizzly Bears")
                .withCardOnBattlefield(2, "Stingerquill Voxmancer", tapped = true)
                .withCardAttachedTo(1, "Infinite Coursework", "Stingerquill Voxmancer")
                .withCardOnBattlefield(2, "Jace", isToken = true)
                .withLandsOnBattlefield(2, "Island", 9)
            // The four Thopters attacked last turn; they untap for the swing back.
            repeat(4) { builder = builder.withCardOnBattlefield(2, "Suntail Hawk", tapped = true) }
            repeat(6) { builder = builder.withCardInLibrary(1, "Swamp").withCardInLibrary(2, "Island") }
            // Threshold: the Extrapolator is a 3/3.
            repeat(7) { builder = builder.withCardInGraveyard(1, "Swamp") }
            val game = builder.build()
            game.addCounters("Fatehold Chronologist", CounterType.PLUS_ONE_PLUS_ONE, 1)
            game.addCounters("Jace", CounterType.LOYALTY, 2)
            val ready = game.advanceToDeclaration(1, Step.DECLARE_ATTACKERS)

            val attackers = ready.attackersChosenBy(AiProfile.LIVE)
            withClue("attacked with ${ready.names(attackers)}") { attackers.shouldBeEmpty() }
        }

        // Game 39e8aac6 turn 29: at 3 life the AI sent its unblockable chip attackers (a 2/1, a
        // 1/3 and a 1/1) at an opponent on 15, keeping a 3/3 and a summoning-sick 3/3 home against
        // a 7/7 trampler and a 2/2. Two halves of the crack-back were wrong. It let one blocker
        // meet the trampler however many stood idle, so even staying home read as four damage
        // through — "lethal" at 3 whatever the AI did, and the attack won on board score. And it
        // read creatures only: beside them sat Living Library ("{6}, Sacrifice this creature:
        // shuffle target creature an opponent controls into its library") and nine lands that
        // untap. In the game the opponent shuffled a 3/3 away, the trampler came in, and exactly
        // three damage got through. Home, the 1/3 alone would have soaked it.
        test("39e8aac6 turn 29: a visible removal ability on their board is in the crack-back") {
            val ready = ruricThar(livingLibrary = true)
            val tetsuko = ready.findPermanent("Tetsuko Umezawa, Fugitive")!!

            val before = ready.attackersChosenBy(
                AiProfile.LIVE.copy(id = "live-without-crackback-stress", stressCrackBack = false),
            )
            withClue("reproduction: attacked with ${ready.names(before)}") { before shouldContain tetsuko }

            // The 2/1 alone may still go: home, the rest hold the trampler even with one of them
            // shuffled away.
            val attackers = ready.attackersChosenBy(AiProfile.LIVE)
            withClue("attacked with ${ready.names(attackers)}") { attackers shouldNotContain tetsuko }
        }

        // The same board without the Library: what stays home holds the trampler and the 2/2
        // off, so the chip attack still goes in.
        test("39e8aac6 turn 29 without the removal ability: the chip attack still goes in") {
            val ready = ruricThar(livingLibrary = false)
            val attackers = ready.attackersChosenBy(AiProfile.LIVE)
            withClue("attacked with ${ready.names(attackers)}") { attackers.shouldNotBeEmpty() }
        }

        test("still attacks when the opponent's blockers are all tapped and the swing back is survivable") {
            val game = position(
                ours = listOf(Creature("Hill Giant"), Creature("Grizzly Bears")),
                theirs = listOf(Creature("Grizzly Bears", tapped = true)),
                ourLife = 20,
                theirLife = 9,
            )
            game.attackersChosenBy(AiProfile.LIVE).shouldNotBeEmpty()
        }
    }
}
