package com.invenit.bacillus.service

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class TestRandomServiceImpl {

    private val service = RandomServiceImpl()

    @Test
    fun testRandomIntIsWithinBounds() {
        val value = service.random(3, 3)

        assertEquals(3, value)
    }

    @Test
    fun testRandomFloatIsWithinUnitRange() {
        val value = service.random()

        assertTrue(value in 0f..1f, "Expected $value to be within [0, 1]")
    }

    @Test
    fun testRandomBooleanDoesNotThrow() {
        service.randomBoolean()
    }
}
