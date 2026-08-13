package com.invenit.bacillus.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class TestAction {

    @Test
    fun testMoveRequiresMode() {
        assertThrows<IllegalArgumentException> {
            Action(Action.Category.Move)
        }
    }

    @Test
    fun testRestRejectsMode() {
        assertThrows<IllegalArgumentException> {
            Action(Action.Category.Rest, Action.Mode.Random)
        }
    }

    @Test
    fun testMoveWithMode() {
        val action = Action(Action.Category.Move, Action.Mode.TowardConsume)

        assertEquals(Action.Category.Move, action.category)
        assertEquals(Action.Mode.TowardConsume, action.mode)
    }

    @Test
    fun testRestWithoutMode() {
        val action = Action(Action.Category.Rest)

        assertEquals(Action.Category.Rest, action.category)
        assertEquals(null, action.mode)
    }
}
