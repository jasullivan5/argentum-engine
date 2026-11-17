# Contributing to Argentum Engine

Argentum is a hobby project, but it's an open one. Contributions are welcome —
PRs, ideas, bug reports, forks, custom sets, agents trained against the gym.
This document is the short version of "what makes a contribution likely to get
merged" and "how I work on it myself, in case that's useful."

**If you've never contributed to a project like this before**, start with the
[contributor guide](https://magic.wingedsheep.com/contribute) instead — a
step-by-step walkthrough that assumes no prior programming experience, from
installing Git and Java through to opening your first card PR, with every term
explained and every step numbered. It covers Windows and macOS, and has a
troubleshooting section for the installs that usually go wrong. Come back here
once you're set up; this document assumes you already are.

Most day-to-day discussion happens in the [Discord](https://discord.gg/dy6eSRPWzu):

- `#dev` — contribution chat, "would you accept a PR for X?", review pings,
  the short version of this guide is pinned there.
- `#showcase` — forks, custom sets, agents, anything you've built on top.
- General channels for play, bug reports, and feature ideas.

If anything below is unclear, that's the fastest place to ask.

## TL;DR

- **PRs are welcome** — no need to DM first.
- **Fastest start: `just contribute`.** Paste a decklist (or pick a set), pick
  a Claude or GPT model, and an agent implements the missing cards —
  tested, reviewed and committed on a local branch you turn into one PR. See
  [Contributing with an AI agent](#contributing-with-an-ai-agent).
- **A small, careful PR is the easiest kind to merge.** One card, faithful
  to its Scryfall text, with a quick manual playthrough — and a scenario
  test if it does anything new or subtle — is exactly what I'm looking for.
- **Batching is fine when the cards reuse existing effects.** If every card
  in the PR is built from primitives already in `Effects.*` /
  `Patterns.*`, feel free to bundle several. But if a card introduces a
  *new* effect, please give it tests and keep it to one new-effect card per
  PR — those need closer review.
- **Ideas, suggestions, bug reports** — open an issue or post in Discord. No
  permission needed.
- **Forks, custom sets, AI agents against the gym** — go ahead, and please
  share what you make in `#showcase`.

## Contributing with an AI agent

AI-built contributions are welcome. Frontier models (current Claude and GPT
models, through Claude Code or Codex) implement cards well, and the repo ships
the workflow, so you don't have to invent one:

```bash
just card-list-status my-deck.txt   # what your deck is missing, read from your checkout
just contribute                     # guided: pick cards, agent, model, mode — and go
```

`just contribute` checks your tools (JDK 25+, `just`, `git`, `jq`, and Claude
Code or Codex) and asks three things: what to build (paste your decklist, or
pick a set), which model, and how to deliver it. Then it starts a loop that
works through the missing cards one unit at a time:

- **Triage** — each missing card is checked against its Scryfall oracle text
  and rulings: either it composes from existing SDK primitives, or it's blocked
  on a named missing capability (a *gap*).
- **Card batches** — up to 15 ready cards per unit, using the `add-card`
  skill: canonical printing placement, reprint rows, a scenario test when the
  card earns one.
- **Gaps** — one missing engine capability per unit, built with the
  `add-feature` skill, with engine tests and the SDK reference updated.
- **Every unit** is gated (`just loop-gate`), reviewed by fresh-context
  subagents (`review-changes`), and only then committed.

Without push access the work always goes to a **local branch**: every
commit stays on `loop-cards-<list>-local` and nothing is pushed. When the loop
reports it's done:

1. Skim the commits (`git log -p origin/main..loop-cards-<list>-local`).
2. Optionally play a few of the cards that do something new. `just ui-dev`
   plus the `generate-scenario` skill sets up a board.
3. Push the branch to your fork and open one PR. In the body, say which agent
   and model built it.

A big list can be split across several PRs. Any prefix of the branch's commits
is a coherent PR, because each commit passed the gate on its own.

**Before you start:** `main` is the source of truth. Work in flight is in the
[open PRs](https://github.com/wingedsheep/argentum-engine/pulls); with the
GitHub CLI signed in, the loop skips cards another open PR already adds. Large
lists or anything engine-shaped: say hi in `#dev` first so two people don't
build the same gap.

## What makes a good PR

Argentum is small enough that I read every PR personally. The things I look
for, roughly in order:

1. **Faithful to the card's rules text.** Compare against Scryfall (including
   rulings and oracle errata). Cards that "mostly work" but quietly skip a
   clause are the most common reason PRs get sent back.
2. **Composes existing primitives.** The engine has a deliberate set of
   building blocks — `Effects.*`, `Patterns.*`, conditions, dynamic
   amounts, target filters, replacement and triggered abilities. New
   single-purpose effects that duplicate something already in the SDK are the
   second most common reason PRs get sent back. Please read
   [`docs/architecture-principles.md`](docs/architecture-principles.md) before
   adding a new `Effect` type or executor.
3. **Tested where it matters.** A card built from primitives other cards
   already exercise doesn't need its own test — the card snapshots cover it.
   Write one when the card adds a new primitive, is the first to use an
   existing one, or leans on a subtle rules interaction (last-known
   information, replacement or layer ordering, linked exile). When you do,
   a scenario test that exercises the card on a real board is worth more
   than a unit test of the executor in isolation. The
   `generate-scenario` skill produces a starting point; a manual playthrough
   in the UI catches the rest.
4. **UX is considered.** A correctly-implemented card with no visible trigger,
   no obvious targeting prompt, or a missing log line is still a broken
   experience. Trace the player flow through the web client from both seats
   before declaring the card done — triggers visible on the stack, targeting
   prompts clear, log readable, opponent sees what they should.
5. **Scoped sensibly.** Bundling several cards in one PR is fine *if* each
   one is built entirely from primitives already in `Effects.*` /
   `Patterns.*` — those are quick to review together. If a card
   introduces a new effect, executor, condition, or other engine-level
   primitive, please keep it to **one new-effect card per PR** and include
   tests for the new primitive itself (not just the card that uses it).
   That way a regression in the new primitive can be caught and reverted
   without touching the unrelated cards.

## The pipeline I use for new cards

This is the workflow that has produced the cards already in the repo. You
don't have to follow it exactly, but the steps map well onto what reviewers
check. The [contributor guide](https://magic.wingedsheep.com/contribute) walks
this same pipeline at a much slower pace — Parts 17–19 cover it click by click
if the summary below moves too fast.

1. **Implement** — run the `add-card` skill with the card name and set code.
   It handles Scryfall lookup, oracle errata, set registration, and a starter
   scenario test when the card needs one.
2. **Generate a scenario and play it manually** — run the `generate-scenario`
   skill with a brief description of what you want to test (the interesting
   interaction, not just the happy path), then start the server and client
   (`just server` / `just client`), load the scenario, and click through it.
3. **Look at the UX from both seats.** Is the triggered ability visible on
   the stack with a readable label? Are targeting prompts unambiguous? Does
   the opponent see what they should (and not see what they shouldn't)? Does
   the log read sensibly?
4. **For more complex cards, run `review-changes` against the diff.** It
   catches most of the "this should compose an existing primitive" cases
   before they land in a PR. A quick re-read of
   [`docs/architecture-principles.md`](docs/architecture-principles.md) is
   the best way to notice when an implementation is fighting the engine
   rather than reusing it.

If a step in the pipeline turns up something the previous steps missed, that's
usually a signal that the card needs another pass before opening the PR — not
that it's ready to merge with a TODO.

## Things to avoid

These come up often enough to be worth calling out:

- **One-off effects.** If you find yourself writing a new `Effect` + executor
  that resembles `Destroy`, `Mill`, `Scry`, or any of the existing
  `Patterns.*` recipes, stop and see whether you can compose what's there
  instead. New effect types are sometimes the right answer, but the bar is
  high.
- **Bypassing the projection layer.** Battlefield filtering by type,
  subtype, color, keyword, or P/T must go through projected state —
  `predicateEvaluator.matches(state, projected, ...)` with
  `state.projectedState`, and `projected.isCreature(...)` instead of
  reading the card's own type line. The base state
  doesn't see continuous effects. This is in
  [`AGENTS.md`](AGENTS.md) and the architecture doc — it's the single most
  common correctness bug.
- **Silent mutations.** Every state change should emit a `GameEvent` so
  triggers and the client can react. If you find yourself wanting to update
  state without an event, that's usually a sign something is wrong.
- **AI output nobody stood behind.** Using AI is welcome — frontier models
  (current Claude and GPT models) are good enough to implement cards well,
  and `just contribute` gives them the same skills, gate and review the
  maintainers' own loops use. What doesn't get merged is output from weaker
  or unguided setups that skipped the gate and the review. Say which model
  built the PR, and skim the diff before you open it.

## Ideas, bugs, and discussion

You don't need permission to:

- File a bug report — GitHub issue, or post in the relevant Discord channel.
- Suggest a feature, mechanic, or refactor in `#dev`.
- Ask "would you accept a PR that does X?" in `#dev` — happy to give a
  yes/no/maybe before you spend time on it.

The Discord is genuinely the best place for back-and-forth; GitHub issues are
better for things that need a paper trail (reproducible bugs, concrete
proposals).

## Forks and downstream projects

Argentum is MIT licensed (see `LICENSE`). You are welcome to:

- Fork it and take it in a different direction.
- Build a custom set on top of `mtg-sdk` / `mtg-sets`.
- Train an agent against `gym` / `gym-server` / `gym-trainer`.
- Use any part of it as a learning resource for ECS, rules engines, or
  immutable game state.

If you build something cool, please share it in `#showcase` on Discord — it's
the most fun part of running this project.

## Code of conduct

Be decent. This is a hobby project, reviews happen on hobby-project
timescales, and the goal is for everyone involved to enjoy themselves.
