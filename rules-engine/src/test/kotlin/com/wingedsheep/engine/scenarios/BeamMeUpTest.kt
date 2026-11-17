package com.wingedsheep.engine.scenarios

import com.wingedsheep.engine.core.*
import com.wingedsheep.engine.handlers.EffectContext
import com.wingedsheep.engine.legalactions.LegalActionEnumerator
import com.wingedsheep.engine.state.components.battlefield.AttachedToComponent
import com.wingedsheep.engine.state.components.identity.OwnerComponent
import com.wingedsheep.engine.state.components.stack.ChosenTarget
import com.wingedsheep.engine.state.components.stack.SpellOnStackComponent
import com.wingedsheep.engine.support.GameTestDriver
import com.wingedsheep.engine.support.TestCards
import com.wingedsheep.sdk.core.*
import com.wingedsheep.sdk.dsl.*
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.model.EntityId
import com.wingedsheep.sdk.scripting.*
import com.wingedsheep.sdk.scripting.filters.unified.GroupFilter
import com.wingedsheep.sdk.scripting.targets.EffectTarget
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.types.shouldBeInstanceOf

/** Mechanic tests use inline cards, independent of the preview set's implementations. */
class BeamMeUpTest : FunSpec({
    val instant = card("Beam Test Instant") {
        manaCost = "{U}"
        typeLine = "Instant"
        beamMeUp("{2}{U}")
        spell { effect = Effects.DrawCards(1) }
    }
    val sorcery = card("Beam Test Sorcery") {
        manaCost = "{U}"
        typeLine = "Sorcery"
        beamMeUp("{U}")
        spell { effect = Effects.GainLife(1) }
    }
    val targeted = card("Beam Test Targeted") {
        manaCost = "{U}"
        typeLine = "Instant"
        beamMeUp("{U}")
        spell {
            val t = target(com.wingedsheep.sdk.scripting.filters.unified.TargetFilter.Creature)
            effect = Effects.ReturnToHand(t)
        }
    }
    val granter = card("Beam Test Granter") {
        manaCost = "{1}{U}{U}"
        typeLine = "Creature — Human"
        power = 2
        toughness = 2
        staticAbility {
            condition = Conditions.IsYourMainPhase
            ability = GraveyardCardsHaveCastKeyword(
                GraveyardCastKeyword.BeamMeUp(returnFilter = GameObjectFilter.Creature.notSourceItself()),
                GameObjectFilter.Any,
            )
        }
    }
    val lock = card("Beam Test Lock") {
        manaCost = "{U}"
        typeLine = "Enchantment — Aura"
        staticAbility { ability = GrantKeyword(AbilityFlag.CANT_BE_BEAMED_UP.name, GroupFilter.attachedCreature()) }
    }
    val flashbackAndBeam = card("Beam Test Flashback") {
        manaCost = "{U}"
        typeLine = "Sorcery"
        keywordAbility(KeywordAbility.flashback("{U}"))
        beamMeUp("{U}")
        spell { effect = Effects.GainLife(1) }
    }
    val counter = card("Beam Test Counter") {
        manaCost = "{U}"
        typeLine = "Instant"
        spell { target(com.wingedsheep.sdk.scripting.filters.unified.TargetFilter.SpellOnStack); effect = Effects.CounterSpell() }
    }
    val remand = card("Beam Test Return Spell") {
        manaCost = "{U}"
        typeLine = "Instant"
        spell { target(com.wingedsheep.sdk.scripting.filters.unified.TargetFilter.SpellOnStack); effect = Effects.ReturnSpellToOwnersHand() }
    }
    fun driver(): GameTestDriver = GameTestDriver().also {
        it.registerCards(TestCards.all + listOf(instant, sorcery, targeted, granter, lock, flashbackAndBeam, counter, remand))
        it.initMirrorMatch(Deck.of("Island" to 40), skipMulligans = true, startingPlayer = 0)
        it.passPriorityUntil(Step.PRECOMBAT_MAIN)
    }
    fun offers(d: GameTestDriver, player: EntityId) = LegalActionEnumerator.create(d.cardRegistry)
        .enumerate(d.state, player).filter { (it.action as? CastSpell)?.alternativeCostType == AlternativeCostType.BEAM_ME_UP }
    fun cast(d: GameTestDriver, player: EntityId, spell: EntityId, creatures: List<EntityId>, source: EntityId? = null, targets: List<ChosenTarget> = emptyList()) =
        d.submit(CastSpell(player, spell, useAlternativeCost = true, alternativeCostType = AlternativeCostType.BEAM_ME_UP,
            beamMeUpSource = source, additionalCostPayment = AdditionalCostPayment(bouncedPermanents = creatures),
            paymentStrategy = PaymentStrategy.FromPool, targets = targets))

    test("returns an untapped summoning sick creature as a cost and exiles after resolution") {
        val d = driver(); val p = d.activePlayer!!
        val c = d.putCreatureOnBattlefield(p, "Grizzly Bears")
        val s = d.putCardInGraveyard(p, instant.name)
        d.giveMana(p, Color.BLUE, 3)
        val offer = offers(d, p).single()
        offer.additionalCostInfo?.validBounceTargets.orEmpty() shouldContain c
        cast(d, p, s, listOf(c)).outcome shouldBe Outcome.Done
        d.getHand(p) shouldContain c
        d.state.getEntity(s)?.get<SpellOnStackComponent>()?.alternativeCost shouldBe AlternativeCostType.BEAM_ME_UP
        d.state.getEntity(s)?.get<com.wingedsheep.engine.state.components.identity.CardComponent>()?.manaValue shouldBe 1
        d.bothPass()
        d.getExile(p) shouldContain s
    }
    test("returning an opponent-owned creature you control pays the cost into its owner's hand") {
        val d = driver()
        val caster = d.activePlayer!!
        val owner = d.getOpponent(caster)
        val creature = d.putCreatureOnBattlefield(owner, "Grizzly Bears")
        val controlChange = d.services.effectExecutorRegistry.execute(
            d.state,
            Effects.GainControl(EffectTarget.SpecificEntity(creature)),
            EffectContext(sourceId = null, controllerId = caster)
        )
        controlChange.error shouldBe null
        d.replaceState(controlChange.state)
        d.state.projectedState.getController(creature) shouldBe caster
        d.state.getEntity(creature)?.get<OwnerComponent>()?.playerId shouldBe owner

        val spell = d.putCardInGraveyard(caster, instant.name)
        d.giveMana(caster, Color.BLUE, 3)
        offers(d, caster).single().additionalCostInfo?.validBounceTargets.orEmpty() shouldContain creature

        cast(d, caster, spell, listOf(creature)).outcome shouldBe Outcome.Done

        d.getHand(owner) shouldContain creature
        d.getHand(caster) shouldNotContain creature
        d.state.getBattlefield() shouldNotContain creature
        d.state.getEntity(spell)?.get<SpellOnStackComponent>()?.alternativeCost shouldBe AlternativeCostType.BEAM_ME_UP
    }
    test("no creature means no offer and a submitted cast is rejected without moving the spell") {
        val d = driver(); val p = d.activePlayer!!
        val s = d.putCardInGraveyard(p, instant.name); d.giveMana(p, Color.BLUE, 3)
        offers(d, p).isEmpty() shouldBe true
        cast(d, p, s, emptyList()).outcome.shouldBeInstanceOf<Outcome.Rejected>()
        d.getGraveyard(p) shouldContain s
    }
    test("rejects opponents creatures noncreatures and more than one creature") {
        for (kind in listOf("opponent", "land", "two")) {
            val d = driver(); val p = d.activePlayer!!; val other = d.state.turnOrder.first { it != p }
            val s = d.putCardInGraveyard(p, instant.name); d.giveMana(p, Color.BLUE, 3)
            val c = d.putCreatureOnBattlefield(if (kind == "opponent") other else p, "Grizzly Bears")
            val payment = when (kind) {
                "land" -> listOf(d.putPermanentOnBattlefield(p, "Island"))
                "two" -> listOf(c, d.putCreatureOnBattlefield(p, "Grizzly Bears"))
                else -> listOf(c)
            }
            cast(d, p, s, payment).outcome.shouldBeInstanceOf<Outcome.Rejected>()
            d.getGraveyard(p) shouldContain s
        }
    }
    test("normal hand casting pays printed mana and puts the spell in the graveyard") {
        val d = driver(); val p = d.activePlayer!!
        val s = d.putCardInHand(p, instant.name); d.giveMana(p, Color.BLUE, 1)
        d.castSpell(p, s).outcome shouldBe Outcome.Done
        d.bothPass()
        d.getGraveyard(p) shouldContain s
    }
    test("beam me up cannot be used from hand or an opponents graveyard") {
        val d = driver(); val p = d.activePlayer!!
        val c = d.putCreatureOnBattlefield(p, "Grizzly Bears"); d.giveMana(p, Color.BLUE, 10)
        val hand = d.putCardInHand(p, instant.name)
        cast(d, p, hand, listOf(c)).outcome.shouldBeInstanceOf<Outcome.Rejected>()
        val other = d.state.turnOrder.first { it != p }
        val grave = d.putCardInGraveyard(other, instant.name)
        cast(d, p, grave, listOf(c)).outcome.shouldBeInstanceOf<Outcome.Rejected>()
    }
    test("a creature returned as the only target causes a fizzle and the spell is exiled") {
        val d = driver(); val p = d.activePlayer!!
        val c = d.putCreatureOnBattlefield(p, "Grizzly Bears")
        val s = d.putCardInGraveyard(p, targeted.name); d.giveMana(p, Color.BLUE, 1)
        cast(d, p, s, listOf(c), targets = listOf(ChosenTarget.Permanent(c))).outcome shouldBe Outcome.Done
        d.bothPass()
        d.getExile(p) shouldContain s
    }
    test("Marooned style restriction removes the enchanted creature from offers and rejects forged payments") {
        val d = driver(); val p = d.activePlayer!!
        val c = d.putCreatureOnBattlefield(p, "Grizzly Bears")
        val aura = d.putPermanentOnBattlefield(p, lock.name)
        d.addComponent(aura, AttachedToComponent(c))
        val s = d.putCardInGraveyard(p, instant.name); d.giveMana(p, Color.BLUE, 3)
        offers(d, p).isEmpty() shouldBe true
        cast(d, p, s, listOf(c)).outcome.shouldBeInstanceOf<Outcome.Rejected>()
    }
    test("Scotty style grant excludes its source and only permits instants and sorceries") {
        val d = driver(); val p = d.activePlayer!!
        val source = d.putCreatureOnBattlefield(p, granter.name)
        val spell = d.putCardInGraveyard(p, "Lightning Bolt")
        d.putCardInGraveyard(p, "Grizzly Bears"); d.giveMana(p, Color.RED, 1)
        offers(d, p).isEmpty() shouldBe true
        val c = d.putCreatureOnBattlefield(p, "Grizzly Bears")
        val offer = offers(d, p).single()
        (offer.action as CastSpell).cardId shouldBe spell
        offer.additionalCostInfo?.validBounceTargets.orEmpty() shouldContain c
        offer.additionalCostInfo?.validBounceTargets.orEmpty() shouldNotContain source
        cast(d, p, spell, listOf(source), source = source, targets = listOf(ChosenTarget.Player(p))).outcome.shouldBeInstanceOf<Outcome.Rejected>()
        cast(d, p, spell, listOf(c), source = source, targets = listOf(ChosenTarget.Player(p))).outcome shouldBe Outcome.Done
    }
    test("a second grant permits returning the first source and exposes independent offers") {
        val d = driver(); val p = d.activePlayer!!
        val first = d.putCreatureOnBattlefield(p, granter.name)
        val second = d.putCreatureOnBattlefield(p, granter.name)
        d.putCardInGraveyard(p, sorcery.name); d.giveMana(p, Color.BLUE, 3)
        val actions = offers(d, p)
        actions.size shouldBe 3
        actions.mapNotNull { it.additionalCostInfo?.validBounceTargets }.any { first in it && second !in it } shouldBe true
        actions.mapNotNull { it.additionalCostInfo?.validBounceTargets }.any { second in it && first !in it } shouldBe true
    }
    test("countering or returning a beamed spell exiles it and never refunds the creature") {
        for (response in listOf(counter, remand)) {
            val d = driver(); val p = d.activePlayer!!; val opponent = d.state.turnOrder.first { it != p }
            val creature = d.putCreatureOnBattlefield(p, "Grizzly Bears")
            val spell = d.putCardInGraveyard(p, instant.name); d.giveMana(p, Color.BLUE, 3)
            val answer = d.putCardInHand(opponent, response.name); d.giveMana(opponent, Color.BLUE, 1)
            cast(d, p, spell, listOf(creature)).outcome shouldBe Outcome.Done
            d.passPriority(p)
            d.submit(CastSpell(opponent, answer, targets = listOf(ChosenTarget.Spell(spell)), paymentStrategy = PaymentStrategy.FromPool)).outcome shouldBe Outcome.Done
            d.bothPass()
            d.getExile(p) shouldContain spell
            d.getHand(p) shouldContain creature
            d.getHand(p) shouldNotContain spell
        }
    }
    test("sorceries obey timing while instants can beam in response") {
        val d = driver(); val p = d.activePlayer!!
        val creature = d.putCreatureOnBattlefield(p, "Grizzly Bears")
        val instantId = d.putCardInGraveyard(p, instant.name)
        val sorceryId = d.putCardInGraveyard(p, sorcery.name)
        d.giveMana(p, Color.BLUE, 10)
        val hand = d.putCardInHand(p, instant.name)
        d.castSpell(p, hand).outcome shouldBe Outcome.Done
        val ids = offers(d, p).map { (it.action as CastSpell).cardId }
        ids shouldContain instantId
        ids shouldNotContain sorceryId
        cast(d, p, sorceryId, listOf(creature)).outcome.shouldBeInstanceOf<Outcome.Rejected>()
        cast(d, p, instantId, listOf(creature)).outcome shouldBe Outcome.Done
    }
    test("Scotty grant is inactive outside its controllers main phases but printed beam me up remains") {
        val d = driver(); val p = d.activePlayer!!
        val source = d.putCreatureOnBattlefield(p, granter.name)
        val creature = d.putCreatureOnBattlefield(p, "Grizzly Bears")
        val granted = d.putCardInGraveyard(p, "Lightning Bolt")
        val printed = d.putCardInGraveyard(p, instant.name)
        d.passPriorityUntil(Step.BEGIN_COMBAT)
        d.giveMana(p, Color.BLUE, 3); d.giveMana(p, Color.RED, 1)
        val ids = offers(d, p).map { (it.action as CastSpell).cardId }
        ids shouldContain printed
        ids shouldNotContain granted
        cast(d, p, granted, listOf(creature), source = source, targets = listOf(ChosenTarget.Player(p))).outcome.shouldBeInstanceOf<Outcome.Rejected>()
    }
    test("granted offers name their granter so they read apart from the printed one") {
        val d = driver(); val p = d.activePlayer!!
        val source = d.putCreatureOnBattlefield(p, granter.name)
        d.putCreatureOnBattlefield(p, "Grizzly Bears")
        d.putCardInGraveyard(p, sorcery.name); d.giveMana(p, Color.BLUE, 1)
        val byOffer = offers(d, p).associate { (it.action as CastSpell).beamMeUpSource to it.description }
        byOffer[null] shouldBe "Cast Beam Test Sorcery (Beam me up {U})"
        byOffer[source] shouldBe "Cast Beam Test Sorcery (Beam me up {U} — from Beam Test Granter)"
    }
    test("an alternative cast that names no cost never pays beam me up without the return") {
        val d = driver(); val p = d.activePlayer!!
        val creature = d.putCreatureOnBattlefield(p, "Grizzly Bears")
        val spell = d.putCardInGraveyard(p, flashbackAndBeam.name); d.giveMana(p, Color.BLUE, 1)
        d.submit(CastSpell(p, spell, useAlternativeCost = true, paymentStrategy = PaymentStrategy.FromPool)).outcome shouldBe Outcome.Done
        d.state.getEntity(spell)?.get<SpellOnStackComponent>()?.alternativeCost shouldBe AlternativeCostType.FLASHBACK
        d.getPermanents(p) shouldContain creature
    }
    test("insufficient mana rejects the cast without returning a creature") {
        val d = driver(); val p = d.activePlayer!!
        val creature = d.putCreatureOnBattlefield(p, "Grizzly Bears")
        val spell = d.putCardInGraveyard(p, instant.name); d.giveMana(p, Color.BLUE, 1)
        cast(d, p, spell, listOf(creature)).outcome.shouldBeInstanceOf<Outcome.Rejected>()
        d.getPermanents(p) shouldContain creature
        d.getGraveyard(p) shouldContain spell
    }
})
