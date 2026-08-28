package com.invenit.bacillus.util

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 *
 * @author viacheslav.mishcheriakov
 * Created 28.08.2026
 */
class TestTicCounter {

    val counter = TicCounter()

    @Test
    fun reset() {
        counter.update(0.1f, 2)
        counter.reset()
        assertEquals(0f, counter.accumulatedTime, 0.001f)

        counter.update(0.1f, 2)
        assertEquals(0.1f, counter.accumulatedTime, 0.001f)
        counter.reset()
        assertEquals(0f, counter.accumulatedTime, 0.001f)
    }

    @Test
    fun update() {
        assertEquals(0f, counter.accumulatedTime, 0.001f)

        assertEquals(1, counter.update(0.1f, 10))
        assertEquals(0f, counter.accumulatedTime, 0.001f)

        assertEquals(10, counter.update(0.2f, 50))
        assertEquals(0f, counter.accumulatedTime, 0.001f)

        assertEquals(0, counter.update(0.1f, 5))
        assertEquals(0.1f, counter.accumulatedTime, 0.001f)
        assertEquals(1, counter.update(0.2f, 5))
        assertEquals(0.1f, counter.accumulatedTime, 0.001f)
        assertEquals(1, counter.update(0.3f, 5)) // capping of max frame delay
        assertEquals(0.15f, counter.accumulatedTime, 0.001f)
        assertEquals(2, counter.update(0.25f, 5))
        assertEquals(0f, counter.accumulatedTime, 0.001f)

    }

}