# Engine AI vs humans — findings (2026-10-31)

## Scope

- **Sample:** 300 finished AI-vs-human games from production, Oct 28–31. Re-simulated on `origin/main` (`c3c7f58d77`): 276 exact, 11 unverified, 16 diverged.
- **Read in full:** 60 AI losses, in three batches:
  - **A:** Quick Game Standard (and 2 Momir)
  - **B:** draft, sealed, Commander sealed
  - **C:** mixed, including three 4-player Commander games
- **Also used:** 90 days of the server's AI-vs-humans stats (`/api/stats/admin/ai-vs-human`).
- **Fix plan:** [`2026-10-31-ai-vs-humans-plan.md`](2026-10-31-ai-vs-humans-plan.md). Raw game logs and per-batch evidence stay out of the repo because they name players.

## The numbers

| Week of | All humans | Excluding top 3 humans |
|---|---|---|
| Sep 28 | 18% | 57% |
| Oct 5 | 48% | 60% |
| Oct 12 | 45% | 47% |
| Oct 19 | 38% | 49% |
| Oct 26 | 28% | 38% |

- **The decline is real, not just a few grinders.** Leaving out the three busiest humans, the AI still fell from about 60% to 38%.
- **Quick Game Standard, excluding the top 3, fell steadily:** 69% → 59% → 48% → 37%.
- **Draft picking is not the cause.** In this sample the AI's draft win rate was 16% before the Draftsim picker deployed (#3967, Oct 30) and 28% after.
- **The AI's limited decks are the same shape as the humans':**

  | Format | Seat | Creatures | Lands |
  |---|---|---|---|
  | Draft | AI | 14.7 | 17.1 |
  | Draft | Humans | 12.3 | 17.7 |
  | Sealed | AI | 14.5 | 16.9 |
  | Sealed | Humans | 11.5 | 16.9 |

## Conclusion

**The AI loses mainly because of how it plays.** Most deck-mismatch losses in the sample (40-card sealed pools against tuned 60-card lists) came from builds before #4019, which already fixed that.

The losses still open to a fix fall into five play themes:

1. **Combat risk is priced too cheaply.** The counter-attack ("crack-back") is estimated from the visible board only, with no margin. It ignores the opponent's activated removal, pumps and hand.

   Three changes from Oct 16–18 made attacking score better:
   - #3252: the board after an attack is scored "at our untap", which turns off the evaluator's lethal-on-board backstop.
   - #3421: credit for pressing a winning position.
   - #3420: modelling opponent responses, which adds code paths where the simulation fails and the attack goes through unscored.

   The decline starts right after these changes. None of the three PRs cites a measurement. **This is the leading hypothesis and needs an AI-vs-AI test to confirm.**
2. **Kinds of play the AI never does:** crewing Vehicles, saddling Mounts, tracking commander damage, choosing whom to attack in multiplayer. It also casts end-of-turn effects with no combat to use them.
3. **It passes with castable plays and spare mana.** The cause isn't found yet.
4. **Mana-screw handling:** mulligans, surveil and landcycling.
5. **Targeting when it can't tell an effect's side** defaults to the opponent.

---

## Findings

Each finding lists: severity (critical / major / minor), the games it showed up in, the evidence, and the root cause. Unless noted, the root cause was confirmed in code. Log ids are short game ids. `T` is the turn number. Code paths are as of `c3c7f58d77`.

### 1. Combat: attacks into lethal counter-attacks

**1a. Attack plans that are never scored, and credit for an attack the AI won't survive to follow up**
- **Severity:** major. Lost the game.
- **Games:** B `ee882769` T20. Same pattern in A `39e8aac6`, C `8743af0c` T34, C `a28f7a90` T11.
- **Evidence:** at 9 life, the AI sent Yargle 3/9, its only real blocker, at a minus-only Jace token. The human swung back with 10 power and the game ended at −1. The board alone shows that counter-attack was lethal.
- **Root cause:**
  - `CombatAdvisor.kt:1830`: `evaluateAttackPlan(seed) ?: return` submits the heuristic plan unscored whenever the simulation returns null, which happens more often since #3420.
  - `CombatAdvisor.kt:1825`: the "don't attack" baseline is scored by a different evaluator, so the comparison isn't like for like.
  - `CombatAdvisor.kt:1701`: the follow-up credit isn't gated on surviving the counter-attack.
  - `CombatAdvisor.kt:314`: the "seed is lethal" shortcut skips the response model.

**1b. The counter-attack estimate ignores visible responses and has no margin**
- **Severity:** major. Seen 7 times; decided at least 3 games.
- **Games:** A `39e8aac6` T29, C `8743af0c` T34 (39 → −6), C `b2e1e69a` T13 (13 → 4), C `a28f7a90` T11, C `6d7c6893` T16, B `e530` T21.
- **Evidence (39e8):** at 3 life, the AI attacked with two creatures to deal 3 damage. The opponent used Living Library on its board to remove a blocker, and Ruric Thar 7/7 trample killed the AI.
- **Root cause:**
  - `CombatMath.kt:722-735` (`estimateNextTurnDamage`) reads only the board.
  - `CombatAdvisor.kt:1674-1699` uses that estimate with no margin.
  - `CombatAdvisor.kt:1654` (`atOurUntap`, from #3252) removes the evaluator's backstop.

**1c. Is the Oct 16–18 race logic a regression? (hypothesis)**
- **Supporting cases:** the games above, plus A `333b736f` T13 (a 1/1 token attacked into a 4/4 deathtouch and a 5/4) and C `1abf65ae` T14 (a lone 3/3 attacked into five blockers while behind).
- **Counter-evidence:** C `acea773b` and B `62e7` show the opposite problem, passivity.
- **Next step:** AI-vs-AI A/B matches (`just arena`) of current play against `live-without-race`, `live-without-pressure`, `live-without-responses` and a pre-Oct-16 profile.

**1d. The combat model ignores opposing +1/+1-counter pump abilities**
- **Severity:** minor.
- **Game:** C `5a3077e2` T14.
- **Evidence:** a 3/2 attacked into Tenured Tethermage, which grew to 5/5 by tapping artifacts.
- **Root cause:** `knowledge/CombatReach.kt:72-80` doesn't list `AddCountersEffect`.

### 2. Kinds of play the AI never does

**2a. It never crews Vehicles or saddles Mounts**
- **Severity:** major. 5 games.
- **Games:** A `556300fa` T8–13 (lost a race at exactly 0 life with a 5/5 menace Mech uncrewed), A `dfebd078` T17–36, A `6c717766`, B `8841`, C `a28f7a90`.
- **Evidence:** "Crew X" appears in `passes; had:` at every priority stop. There is not one AI crew action in 60 games.
- **Root cause:**
  - `CrewEnumerator.kt:92` and `SaddleEnumerator.kt:101` offer the action with no creatures selected.
  - `CrewVehicleHandler.kt:83` rejects an empty crew.
  - The engine AI never fills in the creatures: `Strategist.withAutomaticPayments` (1745-1783) only fills spells and abilities, so the action is dropped as illegal (`Strategist.kt:383-390`).
  - The Jev AI already does this correctly (`jev/JevActions.kt:41`).

**2b. Multiplayer: it always attacks the lowest-life opponent**
- **Severity:** major. 3 of 3 four-player games lost.
- **Games:** C `47b94d02`, `c92b4495`, `8743af0c`.
- **Evidence:** the three AI seats attacked only each other. The human was attacked 0 times in all three games and finished on 38–41 life, having built Ulamog / Animar / Omnath / an 18/17 Sunscorch Regent unopposed.
- **Root cause:** `CombatSeed.kt:402-409` (`primaryDefender`) and `:418-421` (`defenderFor`) pick `minByOrNull { lifeTotal }`, with ties broken by turn order. Nothing considers threat.

**2c. Commander damage is never considered**
- **Severity:** major. Lost the game.
- **Game:** B `1cca8ef9` T13.
- **Evidence:** the AI died at 11 life to 16 commander damage from Eshki Dragonclaw with three untapped creatures and no blocks. Chump-blocking with a 1/1 token would have kept it at 15.
- **Root cause:** nothing under `ai/src/main` reads commander damage. `CombatAdvisor.kt:492`, `:1144` and `:1168` check life only.

**2d. Until-end-of-turn effects cast with no combat to use them**
- **Severity:** major. One game lost.
- **Games:**
  - B `aa989771` T17: Icy Reception (−5/−0) on the opposing Dragon in the AI's own main phase, with no attack. The AI died to that Dragon next turn.
  - B `2d31a409` T14: Icy Reception cast in the AI's own upkeep.
  - C `6d7c6893` T6/T8: Glamermite (flash, "tap target creature") cast on the AI's own turn with no attackers, the second time onto an already-tapped creature.
- **Root cause:**
  - The "nothing can use this shrink" check exists only for activated abilities: `knowledge/ExpiringGrantWindow.kt:151` requires `activation != null`.
  - The spell path, `HoldPolicy.kt:199-213`, has no such check.
  - The ambush waiver at `HoldPolicy.kt:489-494` lets flash creatures through.

**2e. It steals a creature until end of turn, then doesn't attack with it**
- **Severity:** minor to major.
- **Games:** B `380b3fe6` T16 (Involuntary Employment on Lathril), C `f8b2915e` T7/T9 (Sibling Rivalry on Amalia twice, paying ward life each time).
- **Root cause:**
  - `CardIntentAnalyzer.kt:378` tags every `GainControlEffect` as removal.
  - `CombatAdvisor.scoreAttackOutcome` (1638) only removes end-of-turn effects when `settleExpiring` is set, so the stolen creature is protected as if it were the AI's own.

### 3. Passing with plays on offer

**3a. It passes the main phase with idle mana and castable creatures**
- **Severity:** major. 7 games, plus related cases.
- **Games:**
  - B `fccf5763` T12: 11 life, empty board, 5 lands, four castable creatures. It passed twice and took 6.
  - B `e8c9a92d` T18
  - C `c92b4495` T6
  - C `8743af0c` T21/T25
  - C `b2e1e69a` T9–17: its commander, Loot, was never cast.
- **Related:** B `8841` (10 Treasures unused while holding castable spells), A `dfebd078` T21 (skipped a land drop), A `25cb0ffe` T8 (sat on a castable Sorin while being swarmed).
- **Root cause (not confirmed).** Candidates:
  - (a) Ties go to passing (`Strategist.kt:544`, `SCORE_EPSILON`).
  - (b) The per-decision time limit runs out on a busy server, so candidates go "not searched" (`budget/DecisionBudget.kt:174`, `Strategist.kt:415-428`). This fits the 4-player games.
  - (c) The refund that stops a permanent spell counting as a lost card (`permanentCastIsNotCardLoss`) doesn't apply.
- **Next step:** reproduce `fccf` T12 with the AI's decision-insight log (insight sink), and add server logging of how many candidates go "not searched".

**3b. It draws cards instead of deploying blockers under heavy pressure**
- **Severity:** major.
- **Game:** B `e8aaf668` T13.
- **Evidence:** at 14 life facing 13 power on an empty board, it flashed back Twinned Vision, discarding Tomik, instead of casting Tomik and Traxos. It went to 1 life.
- **Root cause (partial):** the threat term (`BoardFeatures.kt:1223`) only fires when the opponent's attack is already lethal.

### 4. Mana and land handling

**4a. It keeps any hand with 2–5 lands**
- **Severity:** minor, but a big swing.
- **Games:** A `088bcff0` (2 lands with only 5–6 drops; stuck on 3 lands until T11), C `844df659`, C `849f7063`. Also a co-cause in A `dbb723ee` and C `b2e1e69a`.
- **Root cause:** `EngineAiPlayerController.kt:155` keeps any hand with `landCount in 2..5`, ignoring the curve and whether it's on the play or the draw.

**4b. Surveil keeps spells on top while stuck on lands**
- **Severity:** minor, but a big swing.
- **Games:** B `e8c9a92d` T6/T8, C `844df659` T6.
- **Root cause:** `DecisionResponder.kt:731-741` scores each looked-at card on its own and never considers a hand already full of spells it can't cast.

**4c. It landcycles its only threat while already holding a land**
- **Severity:** major.
- **Game:** C `b1a69afb` T7.
- **Evidence:** it cycled Apex Witchstalker with a Mountain in hand, then flooded and died holding two Swamps.
- **Root cause:** `Strategist.kt:1341-1378` (`fetchedLandFollowUps`) credits the fetched land as this turn's land drop even when a land already in hand would fill it.

**4d. It sacrifices the land producing a colour it still needs**
- **Severity:** major.
- **Game:** B `e156a664` T16.
- **Evidence:** it sacrificed Room of Refuge, its second blue source, for +1/+1 counters, leaving Countersculpt ({U}{U}) uncastable. It took 10 damage.
- **Root cause:** `BoardFeatures.kt:1401-1406` (`Tempo.landValue`) ignores colours.

**4e. Momir Basic: it activates the avatar before playing its land**
- **Severity:** major (niche format).
- **Games:** A `a7fc150e`, A `08cedd4d`.
- **Evidence:** every turn its creature came out one mana value lower than it could have.
- **Root cause:** `Strategist.kt:2286-2292` and the land-drop credit at `Strategist.kt:1241-1244`.

**4f. No sense of decking itself**
- **Severity:** major (niche).
- **Game:** A `a7fc150e` T13.
- **Evidence:** it played a land with Territorial Bruntar out. The landfall trigger exiled its whole all-land Momir library, and it lost by decking at 20 life.
- **Root cause:** the evaluator has no term for library size.

### 5. Targeting

**5a. When it can't tell an effect's side, it targets the opponent first**
- **Severity:** minor, but affects many cards.
- **Games:**
  - C `6d7c6893` T16: Sygg's "gains '…draw a card'" given to the opponent's creature.
  - B `290fcb53` T12/T14/T17: Hexhaven Dueling Arena's "becomes prepared" aimed three times at opponent creatures that have no prepare spell.
  - C `a28f7a90` T11: Sandworm destroyed the AI's own Plains (cause not traced).
- **Root cause:**
  - `TargetSelection.kt:110-140` falls back to removal ranking when it can't tell.
  - `Strategist.kt:1665-1667` only simulates the top candidates.
  - `TargetPolarity.kt:283-330` has no entry for a granted triggered ability, "target player draws", or "becomes prepared".

**5b. "Choose two" modal spells take the first two modes**
- **Severity:** minor.
- **Game:** C `6d7c6893` T18.
- **Evidence:** Ashling's Command was cast with "target player draws two" aimed at the opponent.
- **Root cause:** `TargetSelection.kt:434`, `modes.take(chooseCount)`. Mode combinations are never compared.

**5c. It says yes to an optional "Pay {1}?" and then pays nothing**
- **Severity:** cosmetic.
- **Game:** C `eee158b2` T29/T30 (Wooden Sphere).
- **Root cause:** the yes/no affordability check counts mana sources the mana responder won't use. Not traced to a line.

### 6. Deckbuilding (secondary)

**6a. A generated "matched" deck had 44 lands, 16 spells and 0 creatures (critical: it can't win)**
- **Game:** A `ea90c6f3`.
- **Root cause:**
  - `DraftsimDeckBuilder.kt:412` fills any spell shortfall with basics.
  - `ConstructedDeckGenerator.kt:174` checks only the total card count.
  - `RandomDeckResolver.kt:186` accepts the result.

**6b. A short spell count is padded with lands**
- **Severity:** major.
- **Games:** B `2d31a409` (22 lands, 18 spells; flooded), B `8d18` and `ee88` (9 creatures).
- **Root cause:** the same line, plus `HeuristicDeckBuilder.kt` around line 119.

**6c. Off-colour tapped duals and colourless utility lands replace basics**
- **Severity:** minor. About 14 decks.
- **Root cause:** `HeuristicDeckBuilder.kt:506-514` (`landFitsColors`) accepts a land if it produces *any* of the deck's colours. A colourless land always passes, because its empty colour identity counts as a match.

**6d. Generated Commander decks are 38 basics plus unrelated singletons**
- **Severity:** major.
- **Games:** C `eee158b2`, `47b94d02`, `c92b4495`, `8743af0c`.
- **Root cause:** `CommanderDeckGenerator.kt` picks on raw rating and curve. It has no ramp, removal or fixing quotas, and splits basics by pip share.

**6e. Generated Standard decks are generic piles against tuned lists**
- **Severity:** major.
- **Games:** A `6c717766`, `11676832`, `dbb723ee`, `7086d6f7`, `088bcff0`; C `f8b2915e`.
- **Root cause:** `ConstructedDeckGenerator.kt:143` shortlists by limited ratings and builds with the limited deck builder.

### 7. Not pursued

| Item | Reason |
|---|---|
| 40-card vs 60-card mismatches on builds before #4019 (8 games) | Already fixed by #4019 |
| Outclassed or unwinnable games: a Vintage storm deck, and games where the human simply played well | Nothing to fix |
| Kiora's {8} Dragon not predicted in combat (B `6db8`) | Low confidence; needs a reproduction first |
| Tapping out instead of holding a counterspell (B `e156` T14) | Judgment call |
| Passive with a dominant board (B `62e7`, C `acea773b`) | Low confidence; kept as a counter-check for the regression test |
| 4 damage at a 5/5 (C7); declined free blocks (C9); must-be-blocked 4/2 didn't attack (C13); discarded Chandra (C15); Divining Duelist mode choice (C11) | One occurrence each, not traced |
| A turn the human controlled via Emrakul logged as the AI's | Log-export tooling issue; doesn't affect play |
| Diverged replays (B `e4f4`, C `b611`) | Replay fidelity; out of scope |

## Loss reasons (60 games, primary reason)

| Reason | Count |
|---|---|
| Outclassed / human played well in a fair format | ~22 |
| AI deck weaker or mismatched (8 are fixed by #4019) | ~19 |
| Decisive AI misplay | ~12 |
| Mana screw or flood | ~7 |

The misplay count is a lower bound. Combat risk, passing and mana handling also made many of the "outclassed" and screw games worse without deciding them on their own.
