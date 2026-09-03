package com.invenit.bacillus.model.matrix

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import kotlin.test.assertEquals

internal class TestAction {

    @Test
    fun testMoveRequiresMode() {
        assertThrows<IllegalArgumentException> {
            Action(Action.Category.Move)
        }
    }

    @Test
    fun testProduceRequiresMode() {
        assertThrows<IllegalArgumentException> {
            Action(Action.Category.Produce)
        }
    }

    @ParameterizedTest
    @EnumSource(value = Action.Category::class, names = ["Rest", "Split"])
    fun testRestAndSplitRejectMode(category: Action.Category) {
        assertThrows<IllegalArgumentException> {
            Action(category, Action.Mode.Random)
        }
    }

    @Test
    fun testMoveRejectsProduceMode() {
        assertThrows<IllegalArgumentException> {
            Action(Action.Category.Move, Action.Mode.Release)
        }
    }

    @Test
    fun testProduceRejectsMoveMode() {
        assertThrows<IllegalArgumentException> {
            Action(Action.Category.Produce, Action.Mode.TowardConsume)
        }
    }

    @Test
    fun testMoveWithMode() {
        val action = Action(Action.Category.Move, Action.Mode.TowardConsume)

        assertEquals(Action.Category.Move, action.category)
        assertEquals(Action.Mode.TowardConsume, action.mode)
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
        "Release",
        "Hoard",
    )
    fun testProduceWithMode(mode: Action.Mode) {
        val action = Action(Action.Category.Produce, mode)

        assertEquals(Action.Category.Produce, action.category)
        assertEquals(mode, action.mode)
    }
}
