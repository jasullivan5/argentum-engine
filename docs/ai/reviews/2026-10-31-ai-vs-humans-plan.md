# AI review plan — 2026-10-31

Companion to [`2026-10-31-ai-vs-humans.md`](2026-10-31-ai-vs-humans.md) (findings). Game ids are short replay ids; A/B/C are the review batches.
Context: AI win rate vs humans 45–48% (weeks of Oct 5/12) → 28% (week of Oct 26); excluding top-3 humans 60% → 38%. Draft did not drop after #3967 (16% → 28% in sample). AI limited decks structurally match humans' (~14.7 creatures, 17 lands). User steer: losses are mostly play, not decks.
Code refs are repo paths as of `c3c7f58d77`.

## Diagnosis
Mostly play. Themes: (1) combat risk underpriced — crack-back is board-only, no margin, ignores opponent's visible removal/pumps/hand; #3252 scores the post-attack board "at our untap" (removes the lethal-on-board backstop), #3421 adds pressure credit, #3420 adds null-sim paths that submit unscored plans → prime suspect for the Oct 19+ decline (hypothesis; no arena numbers in those PRs). (2) Invisible play classes: crew/saddle always illegal as built, commander damage unread, pods attack lowest life, until-EOT effects credited with no combat use. (3) Passing with castable plays (unrooted: tie-to-pass, saturated rollouts, or decision-budget cutoff). (4) Mana-screw handling: keeps any 2–5 lander, surveil keeps spells, landcycles while holding land. (5) UNKNOWN polarity defaults to opponent-first targeting.

## Order
0. deck-crash · 1. crew-saddle · 2. crackback-holes · 3. crackback-stress · 4. race-regression-ab · 5. pod-defender-threat · 6. commander-damage · 7. pass-with-castables · 8. expiring-cast-window · 9. temporary-control · 10. mulligan-curve · 11. landcycle-holding-land · 12. surveil-clog · 13. polarity-unknown · 14. modal-choose-n · 15. colour-aware-land-value · 16. counter-pumps-in-combat · 17. momir-land-first · 18. decking-term · 19. draw-vs-deploy · 20. optional-payment-sources · then A1–A3 AI card-reading audit (corpus-wide) · then D2–D5 deckbuilding.

## Crash-level (deckbuilding exception)

- [ ] P0 deck-crash — "Matched" constructed deck built with 44 lands, 16 spells, 0 creatures
  deckbuilding / crash-level / A ea90c6f3
  root cause: ai/.../draftsim/DraftsimDeckBuilder.kt:412 `lands = landTarget + max(0, nonlandTarget - deckCards.size)`; ConstructedDeckGenerator.kt:174 checks only total size; game-server/.../ai/RandomDeckResolver.kt:186 matchedDeck accepts
  fix: draftsimBuild rejects build with nonland < ~80% target or creatures below floor (8/60) → null so RandomDeckResolver falls back; log pool size per colour
  test: ConstructedDeckGeneratorTest with thin MSH R/W-only pool → null/fallback (44 lands today)
  status: todo

## In-game play

- [ ] P1 crew-saddle — The AI never crews Vehicles or saddles Mounts
  ai / misplay-major / A 556300fa T8–13, A dfebd078 T17–36, A 6c717766 T11–12, B 8841 T17–22, C a28f7a90 T7–11
  root cause: rules-engine CrewEnumerator.kt:92 / SaddleEnumerator.kt:101 emit empty creature list; CrewVehicleHandler.kt:83 rejects empty; Strategist.kt:1745-1783 withAutomaticPayments only fills CastSpell/ActivateAbility → Strategist.kt:383-390 drops as illegal. Jev does it right (jev/JevActions.kt:41-42)
  fix: (a) CrewVehicle/SaddleMount branch in withAutomaticPayments using tapForPowerRequired/tapForPowerCreatures, reuse TapForTotalPower selection (Strategist.kt:1756), prefer summoning-sick / lowest value / smallest set; (b) only if needed: tie crew credit to attack plan, allow crewing on opponent's turn to block
  test: CrewVehicleAiTest from 556300fa T8 (Surgehacker Mech crew 4, Norika 3/2 + Spirited Companion 1/1) → crews and attacks; Saddle twin
  status: todo

- [ ] P1 crackback-holes — Unscored attack plans; follow-up credit when crack-back is lethal
  ai / misplay-major / B ee882769 T20 (+ family A 39e8aac6, C 8743af0c T34, C a28f7a90 T11)
  root cause: CombatAdvisor.kt:1830 `evaluateAttackPlan(seed) ?: return` submits unscored plan (more nulls since #3420, CombatResponses.kt ~109/147/148); :1825 baseline uses evaluator without crack-back; CombatSeed.kt:153 survives-every-blocker seeds unchecked; :1701 followUp not gated on surviving; :314 seed.lethal shortcut skips responses
  fix: on failed eval, no attack unless proven lethal; score baseline with scoreAttackOutcome; gate followUp on nextTurnDamage < myLife; route lethal shortcut through CombatResponses when anticipateCombatResponses
  test: ee88 T20 position in AttacksReadTheRaceTest → Yargle stays home
  status: todo

- [ ] P1 crackback-stress — Crack-back ignores opponent's visible responses, no margin
  ai / misplay-major / A 39e8aac6 T29, C 8743af0c T34, C b2e1e69a T13, C a28f7a90 T11, C 6d7c6893 T16, B e530 T21
  root cause: CombatMath.kt:722-735 estimateNextTurnDamage board-only; CombatAdvisor.kt:1674-1699 no margin; crackBackWithoutChumps 1681-1686; CombatResponses covers only this combat; :1654 atOurUntap (#3252) removes BoardFeatures.kt:1223 backstop
  fix: when life − crack-back ≤ small margin, stress: remove best untapped blocker if opponent has affordable removal/bounce activation; add best affordable on-board pump; hidden-hand margin by cards in hand + untapped mana; lethal → price as lethal. New AiProfile flag, promote after arena
  test: 39e8 T29 position in AnticipateCombatResponsesTest + PuzzleCatalog puzzle → no attack
  status: todo

- [ ] P1 race-regression-ab — Hypothesis: #3252/#3421/#3420 made attacks reckless (decline from ~Oct 19)
  ai / measurement / crack-back items + A 333b736f T13, C 1abf65ae T14; counter-check passivity C acea773b, B 62e7
  root cause (hypothesis): #3252 1112ccb808 untap-before-score (CombatAdvisor.kt:1654), ungated followUpValue (1701); #3421 d679e59dd9 pressureValue (1703) full credit one short of lethal, alphaStrikeStart (~1758); #3420 f54940baa9 null sims → 1830 hole
  fix: add ArenaAgent live-without-race and live-pre-oct16 (ai/src/test/.../arena/ArenaAgent.kt ~163; live-without-pressure/-responses exist); run `just arena <variant> live 300 FRA` each; if a revert wins with CI > 50%, turn flag off in LIVE (AiProfile.kt:2231-2235) until crackback fixes land, re-measure
  test: arena runs; 39e8 + ee88 positions under both profiles
  status: todo

- [ ] P1 pod-defender-threat — Multiplayer always attacks lowest-life opponent
  ai / misplay-major / C 47b94d02, c92b4495, 8743af0c (human attacked 0 times in all three)
  root cause: CombatSeed.kt:402-409 primaryDefender minByOrNull lifeTotal (ties turn order); :418-421 defenderFor same
  fix: score each reachable opponent by threat to us, board/resource lead vs table, killability (keep lethal preference), commander damage dealt; life only tie-break; keep minimalLethalInPods
  test: MultiplayerAttackDefenderTest from c92b T22 (Ulamog + Animar at 38 vs small board lower life) → attack X; `just arena-pod ffa4 live live-threat 300`
  status: todo

- [ ] P1 commander-damage — Blocks/danger checks never read commander damage
  ai / misplay-major / B 1cca8ef9 T13
  root cause: no commanderDamage under ai/src/main; CombatAdvisor.kt:492, :1168 isLifeInDanger, :1144 lethal read life only; engine has GameState.commanderDamageOf (GameState.kt:1758), Format.commanderDamageThreshold
  fix: effectiveLethal helper (life or commander threshold) used in the three checks + incomingNextTurnDamage
  test: CombatAdvisorTest 1cca T13 (threshold 16, Eshki at 10 recorded attacking for 6, AI 11 with Goblin token) → chump Eshki
  status: todo

- [ ] P1 pass-with-castables — Passes main with idle mana and castable creatures (investigate first)
  ai / misplay-major / B fccf5763 T12, B e8c9a92d T18, C c92b4495 T6, C 8743af0c T21/25, C b2e1e69a T9–17; folded: B 8841f442 T19–21 Treasures, A dfebd078 T21 land drop, A 25cb0ffe T8 Sorin
  root cause (unconfirmed): (a) tie-to-pass Strategist.kt:544 SCORE_EPSILON with staticWeight 0.75 (rollout/RolloutSettings.kt:93); (b) wall-clock budget budget/DecisionBudget.kt:174, Strategist.kt:415-428 "not searched — decision budget expired" (fits busy server / pods); (c) permanentCastIsNotCardLoss refund failing
  fix: PR1 insight-sink repro from fccf5763 replay T12 + server logging of not-searched count; then fix mechanism (b: static-screen all candidates before rollouts; a: break ties toward higher static score under big threat)
  test: Strategist decision test from fccf T12 → casts a creature
  status: todo

- [ ] P2 expiring-cast-window — Until-EOT shrink/tap on opposing creature with no combat use (spells + flash creatures)
  ai / misplay-major / B aa989771 T17 (lost), B 2d31a409 T14, C 6d7c6893 T6/T8 Glamermite
  root cause: knowledge/ExpiringGrantWindow.kt:151 requires activation != null; HoldPolicy.kt:199-213 lacks shrink/tap check for spells; AmbushWindow waiver HoldPolicy.kt:489-494
  fix: generalize nothingCanSpend + SHRINK reading to effect+targets, call from HoldPolicy.verdictFor for casts incl. modal modes; floor on tapping opposing creature on own turn with no planned attack or already tapped; hold flash tappers for opponent's beginning of combat
  test: aa98 T17 in RefuseUnspendableShrinkTest; Glamermite case in HoldInstantSpeedDeploysTest
  status: todo

- [ ] P2 temporary-control — Steals a creature until EOT, then doesn't attack
  ai / misplay-minor-major / B 380b3fe6 T16, C f8b2915e T7/T9
  root cause: knowledge/CardIntentAnalyzer.kt:378 tags every GainControlEffect REMOVAL; floating ChangeController EndOfTurn (GainControlExecutor.kt:87); CombatAdvisor.scoreAttackOutcome (1638) removes EOT effects only with settleExpiring
  fix: always score crack-back/post-combat with EOT floating effects removed; tag until-EOT control as combat payoff needing a planned attack
  test: CombatAdvisorTest from 380b T16 (Lathril temporary control, haste) → attacks
  status: todo

- [ ] P2 mulligan-curve — Keeps any 2–5 lander regardless of curve / play-draw
  ai / misplay-minor (big swing) / A 088bcff0 T1, C 844df659, C 849f7063; co-cause A dbb723ee, C b2e1e69a
  root cause: EngineAiPlayerController.kt:155 `keep = landCount in 2..5`
  fix: 2-lander only with ≥2 spells MV≤2, or on the draw with ≥1 MV≤2 and ≥2 MV≤3; require ≥1 spell castable from hand colours; keep existing floors
  test: EngineAiMulliganTest with 088b hand → mulligan
  status: todo

- [ ] P2 landcycle-holding-land — Landcycles its only threat while a land in hand fills the drop
  ai / misplay-major / C b1a69afb T7
  root cause: Strategist.kt:1341-1378 fetchedLandFollowUps credits fetched land whenever drop unused (1351)
  fix: skip follow-up when root hand holds a playable land
  test: b1a6 T7 in FetchedLandDropFollowUpTest → no TypecycleCard
  status: todo

- [ ] P2 surveil-clog — Scry/surveil keeps spells on top while land-short
  ai / misplay-minor (big swing) / B e8c9a92d T6/T8, C 844df659 T6
  root cause: DecisionResponder.kt:731-741 cardsBelowKeepThreshold scores each card alone via CardSelectionValue.spellScore (CardSelectionValue.kt:101)
  fix: with drawnLater and land shortfall, discount a spell by uncastable spells already in hand (extend #3249 dig to surveil/scry)
  test: e8c9 T6 in CastabilityAwareCardSelectionTest → binned
  status: todo

- [ ] P2 polarity-unknown — UNKNOWN polarity targets opponent first; missing classify entries
  ai / misplay-minor (systemic) / C 6d7c6893 T16 Sygg, B 290fcb53 T12/14/17 Dueling Arena, C a28f7a90 T11 Sandworm (untraced)
  root cause: TargetSelection.kt:110-140 UNKNOWN keeps removal ranking, rank 69-78 opposing creatures +10; Strategist.kt:1665-1667 simulates only top targetCandidates; knowledge/TargetPolarity.kt:283-330 lacks GrantTriggeredAbilityEffect (grantedTriggerPolarity :441/:475), targeted-player DrawCards, become-prepared
  fix: (a) add classify entries (granted trigger → grantedTriggerPolarity; draw on targeted player BENEFICIAL; prepared BENEFICIAL); (b) UNKNOWN slots interleave best own + best opposing candidates before take(targetCandidates)
  test: TargetPolarityGrantTriggerTest from 6d7c T16 + 290f Dueling Arena
  status: todo

- [ ] P3 modal-choose-n — Choose-two modal spells take the first N modes
  ai / misplay-minor / C 6d7c6893 T18 Ashling's Command
  root cause: TargetSelection.kt:434 `modal.modes.filter { it.available }.take(modal.chooseCount)`
  fix (add-feature, AI-only): expand modal actions into mode combinations (cap C(n,k) ≤ 10) with per-mode targets, leaf picks
  test: Ashling's Command decision test → draw mode on self or skipped
  status: todo

- [ ] P3 colour-aware-land-value — Sacrifices needed colour source
  ai / misplay-major / B e156a664 T16
  root cause: evaluation/BoardFeatures.kt:1401-1406 Tempo.landValue colour-blind
  fix: colour-access term via PipDemand.kt — losing last source for held card's pips costs that card; flag
  test: e156 T16 → no Room of Refuge sacrifice
  status: todo

- [ ] P3 counter-pumps-in-combat — Combat model ignores opposing +1/+1-counter pumps
  ai / misplay-minor / C 5a3077e2 T14
  root cause: knowledge/CombatReach.kt:72-80 visit list lacks AddCountersEffect/AddDynamicCountersEffect
  fix: treat +1/+1 counter adds as FIGHT
  test: Tethermage-style case in AnticipateCombatResponsesTest → no attack
  status: todo

- [ ] P3 momir-land-first — Momir activates before land drop
  ai / misplay-major (niche) / A a7fc150e T9/11/13, A 08cedd4d T9–15
  root cause: Strategist.kt:2286-2292 momirXCandidates current maxX only; landDropFollowUps (1241-1244) counts only newly castable spells
  fix: no Momir candidates while land drop unused and land in hand
  test: MomirBasicAiTest 5 lands + Island in hand → PlayLand first
  status: todo

- [ ] P3 decking-term — No library/decking term (Territorial Bruntar exiles own library)
  ai / misplay-major (niche) / A a7fc150e T13
  root cause: BoardFeatures has no library-size term; only draw-from-empty floor (Strategist.kt:1515)
  fix: steep penalty when own library < expected turns remaining; flag
  test: Bruntar + land position → no PlayLand
  status: todo

- [ ] P3 draw-vs-deploy — Draws instead of deploying blockers under heavy pressure
  ai / misplay-major / B e8aaf668 T13
  root cause (partial): ThreatAssessment −10 lethal-on-board (BoardFeatures.kt:1223) doesn't fire below lethal; discarded creature priced as just a card
  fix: charge two-turn incoming clock against main-phase leaves; reproduce first
  test: e8aa T13 → casts Tomik or Traxos
  status: todo

- [ ] P3 optional-payment-sources — Yes to "Pay {1}?" then empty payment
  ai / cosmetic / C eee158b2 T29/T30
  root cause (not traced to line): yes/no affordability counts requiresTappingAnotherPermanent sources the mana responder won't use
  fix: exclude those in DecisionResponder affordability check
  test: Wooden Sphere / Gene Pollinator decision test
  status: todo

## Corpus-wide: AI card-reading audit (after P0–P3)

Why not per-card AI tags: about 6 of the 20 play findings are the AI misreading a card. They are Sygg's granted
trigger, Dueling Arena's "prepare", Sandworm, Icy Reception, Glamermite, temporary control and Tethermage's
counter pump. Every one is a missing entry for an *effect type* in the AI's classifiers (`TargetPolarity`,
`CardIntentAnalyzer`, `CombatReach`), not a quirk of the card. One classifier fix covers every card that uses
that effect, including future ones. Per-card tags would restate the card's script, drift when cards change and
need hand upkeep across the whole corpus. `CardAdvisor` stays for real strategy exceptions (combo pieces,
Momir-style formats), not for basic card reading. The top losses (combat risk, crew, pods, commander damage,
passing, mulligans) are not card-specific, so no tagging scheme would touch them.

- [ ] A1 ai-reading-audit — Report: which cards the AI can't read
  category: ai tooling / no behaviour change
  fix: a test-side report (an `ai` test task, `just ai-reading-audit`) that walks every `CardDefinition` in the
  corpus and runs the AI's static readers over each spell, ability and trigger:
  - `TargetPolarity.classify` per target slot: flag UNKNOWN.
  - `CardIntentAnalyzer`: flag effects with no intent.
  - `CombatReach`: flag activated abilities that change P/T, keywords or blockers but aren't visible to combat.
  - `ExpiringGrantWindow` / `HoldPolicy`: flag until-end-of-turn effects with no timing reading.
  - Modal spells with `chooseCount > 1`; crew/saddle and other cost shapes the AI's payment filler can't fill.
  Output is grouped by *effect type*, with a card count and a few example cards per group. Rank groups by card
  count weighted by how often those cards appear in recent games, using the server's replay card stats.
  test: snapshot test over a small fixed card list, so the readers' coverage can't silently regress.
  status: todo

- [ ] A2 ai-reading-ratchet — Coverage number as a CI ratchet
  fix: record the fraction of cards read fully in a baseline file (like `sdk-surface-baseline.txt`). CI fails
  when coverage drops: a new card using an unread effect type, or a reader change that loses cases. Raising
  coverage updates the baseline in the same PR.
  status: todo

- [ ] A3 ai-reading-loop — Agents work the audit, one effect type per PR
  fix: a loop in the shape of `assay-loop` / `set-loop`. Each step:
  1. Take the top unread effect-type group from A1.
  2. Teach the classifier that effect (never a per-card special case).
  3. Add one AI decision test that uses an example card and fails before the fix.
  4. Re-run the audit and raise the A2 baseline.
  5. Open a PR labelled `[agent-loop: <model-id>]`.
  Pure classifier entries don't need an `ai/CHANGELOG.md` line. One that changes how the AI plays a common
  card does. Classifier PRs from this review (polarity-unknown, counter-pumps-in-combat,
  expiring-cast-window, temporary-control) seed the first groups.
  status: todo

## Deckbuilding (secondary)

- [ ] D2 pad-with-lands — Spell shortfall padded with basics (22 lands / 18 spells; 9–10-creature decks)
  deckbuilding / misplay-major / B 2d31a409, 8d182668, ee882769
  root cause: DraftsimDeckBuilder.kt:412; HeuristicDeckBuilder.kt ~119 landsNeeded
  fix: cap lands at target+1; fill from best castable off-archetype playables / light splash first; with or after deck-crash
  test: builder test, 19 castable spells in pair → ≤18 lands
  status: todo

- [ ] D3 offcolor-lands — Off-colour tapped duals / colourless utility lands replace basics
  deckbuilding / misplay-minor / B 7f03, 290f, aa98, 6db8, fccf, 8d18, ee88, 2d31, 315d; C 844d, b1a6, 5a30, 1c80, 849f
  root cause: HeuristicDeckBuilder.kt:506-514 landFitsColors (`produced.any`, vacuous colorIdentity.all); DraftsimDeckBuilder.kt:287/535/555 poolLands filter
  fix: produced ⊆ deckColors (or ≥2 on-colour), no conditional-tapped without condition; ≤1 colourless utility land with real payoff
  test: BG pool with Stingerquill Annex + 2 Dueling Arenas → excluded
  status: todo

- [ ] D4 commander-generator — 38 basics + unrelated singletons
  deckbuilding / misplay-major / C eee158b2, 47b94d02, c92b4495, 8743af0c (+ C17 pips)
  root cause: engine/deck/CommanderDeckGenerator.kt:44 raw rating + curve, LAND_RATIO 0.38 (:358), no quotas, pip-share basics
  fix: (a) quotas ~10 ramp, ~8 removal, draw, fixing for 3+ colours; (b) commander synergy
  test: CommanderDeckGeneratorTest quotas
  status: todo

- [ ] D5 constructed-generator — Generated Standard decks are generic piles
  deckbuilding / misplay-major / A 6c717766, 11676832, dbb723ee, 7086d6f7, 088bcff0; C f8b2915e
  root cause: ConstructedDeckGenerator.kt:143 draftsimBuild limited-rating shortlist + limited builder
  fix: split (a) prefer format precons/starters, (b) constructed curve + 4-of density, (c) archetype synergy
  test: deck-shape assertions
  status: todo

## Not pursued
- Pre-#4019 40-vs-60 mismatches (A c3fa, 25cb, dfeb, c94c, c8ae, 3f5d; C 3625, 41d7): fixed by #4019.
- Outclassed / unwinnable (A 9bdc Vintage storm; "human played well" games): nothing to fix.
- B 6db8 Kiora {8} dragon not predicted: low confidence, repro first.
- B e156 T14 tapped out vs holding Countersculpt: judgment call.
- B 62e7 / C acea passive with dominant board: low confidence; counter-metric in race A/B.
- C7 4 damage at 5/5; C9 declined free blocks; C13 must-be-blocked 4/2; C15 discarded Chandra; C11 Divining Duelist mode: single/untraced/low confidence.
- A Emrakul-controlled turn logged as AI: log-export tooling, file separately.
- DIVERGED replays (B e4f4, C b611): replay fidelity.

## Measurement
1. Per fix: logged-position decision test fails before, passes after; `just arena-puzzles` failing set = KNOWN_FAILURES; add 39e8 T29 and ee88 T20 as crack-back puzzles.
2. Arena A/B (`just arena A B 300 FRA`, BLB second): live-without-race / -pressure / -responses / live-pre-oct16 vs live; flag-gated fixes as live+flag vs live, promote at CI > 50%; `just arena-pod ffa4 live live-threat 300`. Also count "died the turn after its own attack".
3. Production telemetry: log "not searched — decision budget expired" counts and unscored seed plans.
4. Dashboard: weekly AI-vs-human excluding top 3, by format; CHANGELOG line per PR; target ~38% → 55–60% in draft/sealed after P1 batch, pod win share up.
