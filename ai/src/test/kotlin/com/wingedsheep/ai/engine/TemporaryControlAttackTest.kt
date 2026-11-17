package com.wingedsheep.ai.engine

import com.wingedsheep.ai.engine.knowledge.HoldPolicy
import com.wingedsheep.ai.engine.knowledge.IntentCatalog
import com.wingedsheep.ai.engine.knowledge.TimingVerdict
import com.wingedsheep.ai.puzzles.advanceToDeclaration
import com.wingedsheep.ai.puzzles.advanceToPriority
import com.wingedsheep.ai.puzzles.seatId
import com.wingedsheep.engine.core.CastSpell
import com.wingedsheep.engine.core.DeclareAttackers
import com.wingedsheep.engine.core.GameAction
import com.wingedsheep.engine.state.components.identity.CardComponent
import com.wingedsheep.engine.state.components.stack.ChosenTarget
import com.wingedsheep.engine.support.ScenarioTestBase
import com.wingedsheep.sdk.core.Step
import com.wingedsheep.sdk.core.Zone
import com.wingedsheep.sdk.model.EntityId
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * A creature stolen until end of turn ("Threaten" — Involuntary Employment, Sibling Rivalry) is
 * worth exactly the attack it makes: it goes home at cleanup, so it can never block for us, and
 * losing it in combat costs the opponent, not us.
 *
 * Game `380b3fe6` (2026-10-31), turn 16: the AI cast Involuntary Employment on the opponent's tapped
 * Lathril, Blade of the Elves — untapped, hasty, menace — with the opponent on 8, then declared no
 * attack. Game `f8b2915e`, turns 7 and 9: Sibling Rivalry on Amalia Benavides Aguirre (paying 3 life
 * to ward) twice, and no attack either time.
 */
class TemporaryControlAttackTest : ScenarioTestBase() {

    private companion object {
        const val SCENARIO_SEED = 20261031L
        val WITHOUT = AiProfile.LIVE.copy(id = "live-without-borrowed-go-home", borrowedCreaturesGoHome = false)
    }

    private fun TestGame.choice(profile: AiProfile = AiProfile.LIVE): GameAction =
        AIPlayer.create(cardRegistry, seatId(1), profile).chooseAction(state)

    private fun TestGame.inHand(name: String) = state.getZone(player1Id, Zone.HAND).first {
        state.getEntity(it)?.get<CardComponent>()?.name == name
    }

    private fun TestGame.attackersChosenBy(profile: AiProfile = AiProfile.LIVE) =
        (AIPlayer.create(cardRegistry, seatId(1), profile).chooseAction(state) as DeclareAttackers).attackers.keys

    private fun TestGame.castsSteal(action: GameAction, spell: String): Boolean =
        (action as? CastSpell)?.cardId == inHand(spell)

    private fun TestGame.stealVerdict(spell: String, target: String): TimingVerdict =
        HoldPolicy(intents, borrowedCreaturesGoHome = true).verdictFor(
            state, player1Id, spell,
            cast = CastSpell(player1Id, inHand(spell), listOf(ChosenTarget.Permanent(findPermanent(target)!!))),
        )

    /** Cast [spell] on [target] and resolve it, then stop at our attack declaration. */
    private fun TestGame.stealAndDeclare(spell: String, target: String): EntityId {
        val stolen = findPermanent(target)!!
        castSpell(1, spell, stolen).error shouldBe null
        resolveStack()
        state.projectedState.getController(stolen) shouldBe player1Id
        advanceToDeclaration(1, Step.DECLARE_ATTACKERS)
        return stolen
    }

    private val intents by lazy { IntentCatalog.of(cardRegistry) }

    /**
     * Game `380b3fe6`, turn 16, the AI's precombat main: no creatures, 16 life, five Mountains,
     * Involuntary Employment in hand. The opponent is on 8 with Lathril tapped from last turn's
     * attack and a wide board of untapped would-be blockers. Hill Giant stands in for the
     * 4/3 Infernal Vessel, Llanowar Elves for the Elf Warrior tokens.
     */
    private fun lathril(): TestGame = scenario()
        .withRngSeed(SCENARIO_SEED)
        .withPlayers()
        .withTurnNumber(16)
        .withActivePlayer(1)
        .withLifeTotal(1, 16)
        .withLifeTotal(2, 8)
        .withLandsOnBattlefield(1, "Mountain", 5)
        .withCardInHand(1, "Involuntary Employment")
        .withCardOnBattlefield(2, "Lathril, Blade of the Elves", tapped = true)
        .withCardOnBattlefield(2, "Hill Giant")
        .withCardOnBattlefield(2, "Llanowar Elves")
        .withCardOnBattlefield(2, "Llanowar Elves")
        .withCardOnBattlefield(2, "Llanowar Elves")
        .withCardOnBattlefield(2, "Llanowar Elves")
        .build()
        .advanceToPriority(1, Step.PRECOMBAT_MAIN)

    /**
     * Game `f8b2915e`, turn 7, the AI's precombat main: Simian Simulacrum at home, four lands,
     * Sibling Rivalry in hand. The opponent's only creatures are a big ward creature and a 1/1
     * deathtoucher; Craw Wurm stands in for Amalia (the ward payment is beside the point) and
     * Typhoid Rats for Virulent Emissary.
     */
    private fun amalia(): TestGame = scenario()
        .withRngSeed(SCENARIO_SEED)
        .withPlayers()
        .withTurnNumber(7)
        .withActivePlayer(1)
        .withLifeTotal(1, 17)
        .withLifeTotal(2, 21)
        .withLandsOnBattlefield(1, "Mountain", 4)
        .withCardOnBattlefield(1, "Simian Simulacrum")
        .withCardInHand(1, "Sibling Rivalry")
        .withCardOnBattlefield(2, "Craw Wurm")
        .withCardOnBattlefield(2, "Typhoid Rats")
        .build()
        .advanceToPriority(1, Step.PRECOMBAT_MAIN)

    init {
        test("380b T16: Lathril stolen until end of turn attacks") {
            val game = lathril()
            val lathril = game.stealAndDeclare("Involuntary Employment", "Lathril, Blade of the Elves")

            withClue("the borrowed Lathril goes home at cleanup; attacking with it is free") {
                game.attackersChosenBy() shouldContain lathril
            }
            withClue("reproduction: read as ours, Lathril stayed home") {
                game.attackersChosenBy(WITHOUT) shouldNotContain lathril
            }
        }

        test("f8b2 T7: a stolen fatty attacks into the deathtoucher that would kill it") {
            val game = amalia()
            val wurm = game.stealAndDeclare("Sibling Rivalry", "Craw Wurm")

            withClue("if the Rats trade with the borrowed Wurm, the opponent loses the Wurm") {
                game.attackersChosenBy() shouldContain wurm
            }
            game.attackersChosenBy(WITHOUT) shouldNotContain wurm
        }

        test("380b T16: postcombat, a steal is refused — the borrowed creature has no fight left") {
            val game = lathril()
            game.advanceToPriority(1, Step.POSTCOMBAT_MAIN)

            game.stealVerdict("Involuntary Employment", "Lathril, Blade of the Elves") shouldBe TimingVerdict.NoWindow
            withClue("Involuntary Employment cast postcombat steals nothing that outlives the turn") {
                game.castsSteal(game.choice(), "Involuntary Employment") shouldBe false
            }
            withClue("reproduction: the leaf read the borrowed Lathril as a permanent gain") {
                game.castsSteal(game.choice(WITHOUT), "Involuntary Employment") shouldBe true
            }
        }

        test("precombat, the steal has its attack ahead and is the leaf's call") {
            val game = lathril()

            game.stealVerdict("Involuntary Employment", "Lathril, Blade of the Elves") shouldNotBe TimingVerdict.NoWindow
        }

        test("postcombat with a sacrifice outlet in play, the steal still buys something") {
            val game = scenario()
                .withRngSeed(SCENARIO_SEED)
                .withPlayers()
                .withActivePlayer(1)
                .withLandsOnBattlefield(1, "Mountain", 5)
                .withCardOnBattlefield(1, "Viscera Seer")
                .withCardInHand(1, "Involuntary Employment")
                .withCardOnBattlefield(2, "Craw Wurm")
                .build()
                .advanceToPriority(1, Step.POSTCOMBAT_MAIN)

            game.stealVerdict("Involuntary Employment", "Craw Wurm") shouldNotBe TimingVerdict.NoWindow
        }
    }
}
