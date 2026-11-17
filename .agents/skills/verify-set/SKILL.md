---
name: verify-set
description: Prove a Magic set is actually finished — card-for-card complete, field-for-field faithful to Scryfall, scripts that match their oracle text, tokens that resolve the set's own art, behaviourally sound — then archive its backlog. Builds the Scryfall dump and `<Set>CardFieldVerificationTest`, fans the per-card DSL and token review out to subagents, fixes what they surface, and writes the completion report. Use when asked to "verify set X is done", "field-verify a set against Scryfall", "check a set's cards match their oracle text", "check a set's token art", "is set X really complete", or when a set's backlog hits N/N and needs closing out.
argument-hint: <SET name or code>
---

# Verify a set is finished

`cards.md` reading `286 / 286` is a claim, not a proof. It says every name has *a* `CardDefinition` — not
that the definitions carry the right power, the right color identity, the right oracle text, or that the
cards work when played. This skill closes that gap and then archives the backlog.


## The six claims

Finished means all six hold. Do them in order — each is cheap relative to the next, and a failure early
saves the later work.

| # | Claim | Proven by | Stage |
|---|---|---|---|
| 1 | Every card that should exist, exists | `scripts/card-status --set <CODE>`, `just check-backlog*` | [1](#stage-1--completeness) |
| 2 | Every compiled `CardDefinition` matches Scryfall field for field | `<Set>CardFieldVerificationTest` | [2](#stage-2--field-fidelity) |
| 3 | The cards behave as printed when played | scenario tests + a self-play pass | [3](#stage-3--behaviour) |
| 4 | Every card's *script* says what its oracle text says | `assay-differential` + a per-card review | [4](#stage-4---dsl) |
| 5 | Every token the set mints resolves *this set's* art | `token-art-gaps`, `TokenArtCoverageTest` | [5](#stage-5---tokens) |
| 6 | The repo says so | snapshot re-bless, backlog archived, report written | [6](#stage-6--land-it) |

Claim 2 is the one this skill exists for, and the one no other gate in the repo covers.
`CardDefinitionSnapshotTest` pins what we compiled against *what we compiled last time*; it is blind to
Scryfall. `CardLintTest` checks internal hygiene. Nothing else compares a card to the printed card.

Claims 4 and 5 are the ones that don't fit in one context: they're per-card judgement across 200–290
cards. **Read [`per-card-review.md`](per-card-review.md) before starting either** — it holds the subagent
fan-out protocol, the reviewer prompts with their fixed verdict blocks, and both checklists.

## You are the orchestrator

A set is 200–290 cards. Read serially in one session, the useful signal is gone by card 40 and the session
is compacting by card 80 — and a compacted reviewer gets quietly worse without telling you. So for every
per-card stage: **dispatch, aggregate, record. Never open a card file yourself.**

- Subagents read the cards; you hold only their verdict blocks. Insist on a fixed `Return only:` shape so a
  batch costs you four lines, not four hundred.
- Batch 8–12 cards per reviewer, grouped by mechanic or cycle rather than alphabetically — a reviewer
  holding a whole cycle catches the one card that renders the shared ability differently.
- Run batches concurrently; they're read-only and independent.
- Review and fix are separate dispatches. A reviewer that also edits rationalises its own findings.
- Keep a scratch ledger, one line per batch. Across a pass this long it's your memory; the conversation
  isn't.

The test of whether you're doing it right: a 280-card set costs your context roughly what a 20-card set
costs. If you're about to open a `.kt` file to "just check one thing", that's a subagent's job. Same
doctrine as `set-loop` —
[How the driving session stays flat](../../../docs/agent-loops/set-implementation-loop.md#how-the-driving-session-stays-flat).

## Stage 0 — the dump

Everything downstream reads one committed artifact: a full Scryfall dump of the set.

```bash
CODE=msh                     # lowercase set code
SLUG=marvel-super-heroes     # the backlog/sets/<slug> directory
curl -s "https://api.scryfall.com/cards/search?order=set&unique=prints&q=e%3A$CODE" \
  -H 'User-Agent: ArgentumEngine/1.0 (card data verification)' > /tmp/p1.json
# follow `next_page` until it's absent, concatenating `data` into one {"data":[...]} object
```

- **`unique=prints`, not `unique=cards`.** One card can have several printings inside a single set
  (showcase, borderless, Beginner Box); the per-card row Scryfall serves is an arbitrary one of them.
  Reprint rows and variant art are matched by collector number, which only `unique=prints` gives you.
- **Do not commit** This changes, so this is a snapshot for this test.
- **Do not reuse `~/.cache/scryfall/set-<code>.json`.** `scripts/card-status` writes it, but it keeps only
  names plus a flattened per-card subset — no `type_line`, no `mana_cost`, no P/T, no per-face objects. It
  cannot support Stage 2.

`scripts/card-status` also decides *which* cards are in scope: it partitions on Scryfall `booster` into
draft cards and extras, and drops `coverage/card-exclusions.json` entries (ante, dexterity) out
of the denominator. Take its numbers as the set's definition of done, not the raw dump size.

## Stage 1 — completeness

```bash
scripts/card-status --set <SET code> --list        # implemented vs missing, missing names listed
just check-backlog                          # cards.md headers match actual [x] counts
just check-backlog-implementations           # every [ ] is genuinely unimplemented
```

Then the two lists `card-status` does *not* diff, both of which a modern set has and none of the five
existing harnesses ever checked:

```bash
grep -n 'printings\|basicLands' mtg-sets/*/src/main/kotlin/**/definitions/$CODE/*Set.kt
```

- **`MtgSet.printings`** — reprint and variant rows. These are `Printing` vals, not `CardDefinition`s, so
  they are absent from `set.cards` and invisible to every gate in Stage 2 unless you add the reprint block
  from the template. Their `collectorNumber`, `artist`, `imageUri`, `rarity` are per-printing data that
  can be wrong independently of the canonical card.
- **`MtgSet.basicLands`** — also outside `set.cards`. `BasicLandArtOrderTest` covers art ordering;
  presence and collector numbers are yours to check.

If the set is genuinely short of cards, stop — that's `add-card` / `set-loop` work, not verification.
Report the gap and don't proceed to Stage 2 on a partial set.

## Stage 2 — field fidelity

**Read [`field-verification.md`](field-verification.md)** — it holds the test template, the loader, the
DFC face-pairing, and the taxonomy of what the five prior runs actually found. Do not write this test from
memory; the false-positive handling is the hard part and it's all in there.

The shape:

1. Add `mtg-sets/src/test/kotlin/com/wingedsheep/mtg/sets/<Xxx>CardFieldVerificationTest.kt` from the
   template, with the set code, dump path and class name substituted. 
2. Run it. It collects *every* discrepancy and asserts the list is empty, so one run gives you the whole
   worklist rather than the first failure.
3. Triage each line into **fix the card**, **fix the SDK**, or **normalize the comparison** — the taxonomy
   table tells you which, and it matters: three of the eleven classes are the harness being wrong, not the
   card.
4. Fix, re-run to zero, re-bless the snapshot (Stage 6).
5. Remove the `CardFieldVerificationTest.kt`. It's just part of this test. Should not be commited.

Field discrepancies are their own commit, separate from the harness that found them — that's what
`tla-verify` and `tmt-field-verification` both did, and it keeps the card diff reviewable without the
40k-line dump in the way.

## Stage 3 — behaviour

Fields being right does not make a card work.

1. **Important cards must have scenario test.** , all special cards and
   cards with tricky behaviour. `<CardName>ScenarioTest.kt` — `AGENTS.md`'s 
2. **Play the set.** Follow [`docs/gym-self-play-testing.md`](../../../docs/gym-self-play-testing.md):
   `just gym-server`, then build an Explicit deck that concentrates the set's cards and drive the step
   loop for both seats. This is what surfaces the class of bug a scenario test can't — a card that
   produces no legal action, a trigger that never fires, a state that can't legally exist.
3. **Route what you find.** A card-shaped fix is `add-card` territory. A missing engine capability is
   `add-feature`.


## Stage 4 - DSL

Stage 2 proved the card's *text* matches Scryfall. This stage asks the harder question: does the
`cardDef { }` **do** what that text says? A card can carry a byte-perfect `oracleText` and a script that
implements three of its four clauses — Stage 2 passes it, the snapshot passes it, and nothing else looks.

Fan this out. It's ~200–290 cards of judgement and it does not fit in one context —
**[`per-card-review.md`](per-card-review.md)** holds the batching, the reviewer prompt and its verdict
block. Run the machines first; they shrink what humans have to read:

```bash
just assay-differential --set <CODE>    # read EVERY divergent row
just assay-gate --set <CODE>            # declines are coverage, not failures
just test-class CardLintTest            # dataflow hygiene
just test-class FacadeBoundaryTest      # facades, not raw constructors
```

`assay-differential` is the one that matters. It is Argentum Assay's *independent* reading of each oracle
text set against the card a human wrote from the same text — which is precisely this stage's question,
decided by a parser instead of a reviewer. Its own recipe says to classify every `DIVERGENT` row as parser
bug / card bug / known fold and read all of them; its history is that it keeps finding real bugs in
hand-written cards (the batch-trigger band exposed five at once).

Its limit is honest and important: the differential only sees the class of card Assay's grammar reads
*whole*, so on a modern set most cards decline and are simply not covered. A green differential is a floor,
not the pass. The per-card review is the pass.

What the reviewers are looking for, in rough order of how often it's wrong: a clause in the oracle text
with nothing in the script implementing it; targeting quantifiers (`target` vs "each" vs "any number of");
optional vs mandatory (`you may`); trigger event, zone, and intervening-if vs on-resolution condition;
durations (end of turn / end of combat / until your next turn); last-known-information reads on dies
triggers; and a one-off effect written where a `Patterns.*` composition already exists.

## Stage 5 - Tokens

Nothing about a wrong token fails. A token with no resolvable art doesn't render blank — it falls through
to a Scryfall **name guess** (`cards/named?exact=<name>`) and shows whatever printing comes back. That's
how Arahbo's Foundations Cat ended up showing Dominaria Remastered art: nothing was missing, so nothing
failed. Tokens are therefore the part of a finished set most likely to be quietly wrong.

```bash
just token-art-gaps                       # -> backlog/token-art-gaps.md; must list nothing for your set
just test-class TokenArtCoverageTest      # the floor: nothing renders via a Scryfall name-guess
```

Art resolves in three steps — an explicit `imageUri` on the `CreateToken` effect, then **this set's rows
for the set the creating card was printed in** (hand-authored `MtgSet.tokenArt` ahead of synced
`tokens.json`), then the generic by-creature-type fallback. Step 2 is the correct-creator rule, and it's
why art belongs on the set and not the card: keying on the creating card's printing is what makes a reprint
mint the *reprint's* token.

The two checks above are not the same strength. `TokenArtCoverageTest` only proves nothing falls to a name
guess; **`token-art-gaps` proves the property you actually want** — that no token of yours is showing
generic stand-in art instead of its own set's. Read the rows for your set code; the file is global and
generated, so don't hand-edit it.

[`per-card-review.md`](per-card-review.md) has the full checklist: enumerating token sites with
`TokenCreationSites.of(card)` rather than grepping for `CreateToken`, why `imageUri` must be the Scryfall
`normal` URL and not `art_crop`, when a set needs hand-authored rows at all (mostly pre-2001), and the
explicit-`imageUri` anti-pattern that both gates deliberately skip.

## Stage 6 — land it

```bash
just rebless-cards                       # Stage 2's card fixes moved the golden — expected
git diff mtg-sets/src/test/resources/snapshots/cards/<CODE>.json
```

Only your set's cards should have moved. **An unrelated card in the diff means you changed shared SDK
behaviour** — stop and investigate (`verify` skill, "Expected: a card-snapshot diff").

Gate by what the diff reaches, per the [`verify`](../verify/SKILL.md) skill's table — card-only fixes plus
a new `mtg-sets` test is `just test`; anything that touched `rules-engine` is `just test-rules`.

Then close the backlog out:

Read and update all files in backlogs/sets/<slug>

Ensure that the Set file is no longer incomplete. 
`com.wingedsheep.mtg.sets.definitions.<setcode>.<SetName>Set.kt` should not have this: `override val incomplete = true`

Announce it: add a `kind: 'set'` entry to the top of
`web-client/src/components/whatsNew/announcements.ts` (skip if the set already has one) — `setCode`, a
`body` that pitches what the set is like to play, and `tryIf`. The PR that drops the flag is the one
that adds it.


```bash
git mv backlog/sets/<slug> backlog/archived/sets/<slug>      # dump included
# cards.md header: "**Implemented:** N / N" + a Status line saying the set is complete
just check-backlog
```

Keep `<slug>-engine-gaps.md` alongside it if the set left anything genuinely infeasible — TLA's archive
does. An "engine gap" is a documented decision, not an excuse: say what's missing and why.

## The report

A set is verified when you can state all six claims with the evidence attached. Write it into the PR body:

```
Set:              MSH — Marvel Super Heroes
Completeness:     276/276 draft cards, 0 missing, N reprint rows, 20 basic lands  (scripts/card-status)
Field fidelity:   276 cards × 13 fields vs Scryfall — 0 discrepancies  (MshCardFieldVerificationTest)
                  <N> fixed on the way: <one line per class from the taxonomy>
Behaviour:        <N> scenario tests, <M> cards without one (<reason>); self-play: <what you played, what broke>
DSL fidelity:     276 cards reviewed in <B> batches — <N> divergent, all fixed
                  assay-differential: <D> divergent of <C> cards read whole (<rest> declined, not covered)
Tokens:           <T> tokens from <N> cards — all resolve MSH art (token-art-gaps: none; TokenArtCoverageTest green)
Waived:           <card, field, why, and where the follow-up is tracked>  — or "none"
Gate:             just test — green  (does not cover: <what it doesn't>)
```

**State what the gate does not cover.** "0 discrepancies" means: against the dump, on those thirteen
fields, for the cards in `set.cards`. It says nothing about rulings, legalities, or whether the card plays
correctly. Claiming more than that is worse than claiming nothing.

The same applies to the two fanned-out stages, and it's easier to overclaim there because the numbers look
absolute. A DSL pass is *N reviewers' judgement*, not a proof — report how many cards each batch held and
how many `assay-differential` actually read whole versus declined. A clean `token-art-gaps` means no token
falls back to generic art; it does not mean the art is the one printed on the card you meant.

## Traps

- **Zero discrepancies on a modern set is a result to distrust before you trust it.** ARN legitimately came
  back clean (78 cards, all vanilla-ish, hand-checked). A 280-card set coming back clean on the first run
  more often means the harness matched nothing — check the reported card count in the test's own output
  against `card-status` before believing it.
- **The waiver set is not a way to reach green.** `WAIVED` is keyed to an exact (card, field) pair so a
  *new* discrepancy still fails. Every entry needs a reason and a tracked follow-up in the report. A
  waiver on `power` is almost certainly a real bug wearing a disguise.
- **`checkFace` on a single-faced card is the whole object.** Don't special-case; the template's `faces()`
  returns `listOf(this)` and the same code path covers both.
- **Don't fix a card to match a false positive.** Image cache-busters, `*+N` P/T render order, and the
  back-face mana cost are the harness's problem. See the taxonomy.
- **A card's own `metadata { imageUri = … }` is card art and correct.** Only an `imageUri` argument *inside
  a token effect* is the anti-pattern. A loose grep for `imageUri` in a set's card files hits every card and
  says nothing.
- **`assay-differential` declining a card is not a pass.** Declines are coverage, not verdicts. A set whose
  cards mostly decline got almost no signal from the differential, however green the run looked.
- **Don't let a reviewer subagent fix what it found.** It will rationalise. Collect verdicts, triage
  yourself, dispatch fixes separately.
- **Verification is not a licence to touch other people's work.** A failure in a card or module outside
  your set is another agent's in-flight change — report and stop (`AGENTS.md` → Hard rules).
