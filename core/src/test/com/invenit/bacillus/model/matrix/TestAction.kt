package com.invenit.bacillus.model.matrix

import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import kotlin.test.assertEquals

internal class TestAction {

    @ParameterizedTest
    @EnumSource(value = Action.Category::class, names = ["Move", "Produce"])
    fun testCategoriesWithRequiredMode(category: Action.Category) {
        assertThrows<IllegalArgumentException> {
            Action(category)
        }
    }

    @ParameterizedTest
    @EnumSource(value = Action.Category::class, names = ["Rest", "Split"])
    fun testRestAndSplitRejectMode(category: Action.Category) {
        assertThrows<IllegalArgumentException> {
            Action(category, Action.Mode.Random)
        }
    }

    @ParameterizedTest
    @CsvSource(
        "Move, Release",
        "Produce, TowardConsume"
    )
    fun testRejectAlienMode(category: Action.Category, mode: Action.Mode) {
        assertThrows<IllegalArgumentException> {
            Action(category, mode)
        }
    }

    @ParameterizedTest
    @EnumSource(value = Action.Category::class, names = ["Rest", "Split"])
    fun testCategoryWithoutModeConstructs(category: Action.Category) {
        val action = Action(category)

        assertEquals(category, action.category)
        assertEquals(null, action.mode)
    }

    @ParameterizedTest
    @CsvSource(
        "Move, AwayFromToxin",
        "Move, Hold",
        "Produce, Release",
        "Produce, Retain"
    )
    fun testCategoriesWithMode(category: Action.Category, mode: Action.Mode) {
        val action = Action(category, mode)

        assertEquals(category, action.category)
        assertEquals(mode, action.mode)
    }
}
