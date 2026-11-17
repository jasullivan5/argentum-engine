package com.wingedsheep.gameserver.deck

import com.wingedsheep.engine.registry.CardRegistry
import com.wingedsheep.sdk.core.DeckFormat
import com.wingedsheep.sdk.core.ManaCost
import com.wingedsheep.sdk.core.Subtype
import com.wingedsheep.sdk.model.CardDefinition
import com.wingedsheep.sdk.model.Deck
import com.wingedsheep.sdk.model.CardEntry
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

/**
 * Deck construction for the two commander-less 100-card singleton formats: Highlander, and
 * Canadian Highlander with its points list and no sideboard.
 */
class DeckValidatorHighlanderTest : FunSpec({

    val highlanderFormats = setOf(DeckFormat.VINTAGE, DeckFormat.HIGHLANDER, DeckFormat.CANADIAN_HIGHLANDER)

    fun card(name: String, formats: Set<DeckFormat> = highlanderFormats) = CardDefinition.creature(
        name = name,
        manaCost = ManaCost.parse("{2}"),
        subtypes = setOf(Subtype("Construct")),
        power = 2,
        toughness = 2,
    ).copy(legalFormats = formats)

    val fillerNames = (1..120).map { "Highlander Filler $it" }
    val registry = CardRegistry().apply {
        fillerNames.forEach { register(card(it)) }
        // Real names, so the points list prices them: Sol Ring 3, Mana Crypt 5, Black Lotus 7.
        register(card("Sol Ring"))
        register(card("Mana Crypt"))
        register(card("Black Lotus"))
        register(card("Modern Only Card", formats = setOf(DeckFormat.MODERN)))
    }
    val validator = DeckValidator(registry)

    fun singletonDeck(size: Int, vararg extra: String): Map<String, Int> =
        (fillerNames.take(size - extra.size) + extra).associateWith { 1 }

    test("Highlander accepts 100 singleton cards and more than 100") {
        validator.validate(singletonDeck(100), DeckFormat.HIGHLANDER).errors.shouldBeEmpty()
        validator.validate(singletonDeck(110), DeckFormat.HIGHLANDER).errors.shouldBeEmpty()
    }

    test("Highlander rejects fewer than 100 cards") {
        val result = validator.validate(singletonDeck(99), DeckFormat.HIGHLANDER)
        result.errors.map { it.code } shouldContain "TOO_FEW_CARDS"
    }

    test("Highlander is singleton") {
        val deck = singletonDeck(99) + ("Highlander Filler 1" to 2)
        val result = validator.validate(deck, DeckFormat.HIGHLANDER)
        result.errors.map { it.code } shouldContain "TOO_MANY_COPIES"
    }

    test("Highlander rejects a card outside the Vintage pool") {
        val result = validator.validate(singletonDeck(100, "Modern Only Card"), DeckFormat.HIGHLANDER)
        result.errors.map { it.code } shouldContain "NOT_LEGAL_IN_FORMAT"
    }

    test("Highlander has no points list") {
        val result = validator.validate(singletonDeck(100, "Black Lotus", "Mana Crypt"), DeckFormat.HIGHLANDER)
        result.errors.shouldBeEmpty()
        result.points.shouldBeNull()
    }

    test("Canadian Highlander tallies points and accepts a deck at the cap") {
        // 7 + 3 = 10.
        val result = validator.validate(singletonDeck(100, "Black Lotus", "Sol Ring"), DeckFormat.CANADIAN_HIGHLANDER)
        result.errors.shouldBeEmpty()
        result.points?.total shouldBe 10
        result.points?.cap shouldBe 10
        result.points?.byCard shouldBe mapOf("Black Lotus" to 7, "Sol Ring" to 3)
    }

    test("Canadian Highlander rejects a deck over the cap") {
        // 7 + 5 = 12.
        val result = validator.validate(singletonDeck(100, "Black Lotus", "Mana Crypt"), DeckFormat.CANADIAN_HIGHLANDER)
        result.errors.map { it.code } shouldBe listOf("POINTS_OVER_CAP")
        result.points?.total shouldBe 12
    }

    test("Canadian Highlander reports a zero tally for a deck with no pointed cards") {
        val result = validator.validate(singletonDeck(100), DeckFormat.CANADIAN_HIGHLANDER)
        result.points?.total shouldBe 0
        result.points?.byCard shouldBe emptyMap()
    }

    test("Canadian Highlander has no sideboard") {
        val deck = Deck(
            cards = singletonDeck(100).keys.toList(),
            sideboard = listOf(CardEntry("Highlander Filler 110")),
        )
        validator.validate(deck, DeckFormat.CANADIAN_HIGHLANDER).errors.map { it.code } shouldContain
            "SIDEBOARD_NOT_ALLOWED"
        validator.validate(deck, DeckFormat.HIGHLANDER).errors.shouldBeEmpty()
    }
})
