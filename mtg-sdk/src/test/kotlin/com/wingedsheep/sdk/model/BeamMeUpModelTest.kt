package com.wingedsheep.sdk.model

import com.wingedsheep.sdk.core.Keyword
import com.wingedsheep.sdk.scripting.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class BeamMeUpModelTest : FunSpec({
    test("beam me up keyword round trips") {
        val ability = KeywordAbility.beamMeUp("{2}{U}")
        Json.decodeFromString<KeywordAbility>(Json.encodeToString(ability)) shouldBe ability
        ability.keyword shouldBe Keyword.BEAM_ME_UP
    }
    test("a beam me up grant with a source-relative return filter round trips") {
        val grant: StaticAbility = GraveyardCardsHaveCastKeyword(
            GraveyardCastKeyword.BeamMeUp(returnFilter = GameObjectFilter.Creature.notSourceItself()),
            GameObjectFilter.Any,
        )
        Json.decodeFromString<StaticAbility>(Json.encodeToString(grant)) shouldBe grant
        grant.description shouldBe "Each card in your graveyard has beam me up"
    }
})
