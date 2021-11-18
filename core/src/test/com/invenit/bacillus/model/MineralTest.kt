package com.invenit.bacillus.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Created by vyacheslav.mischeryakov
 * Created 18.11.2021
 */
internal class MineralTest {

    private val mineral: Mineral = Mineral(
        Point(1, 1),
        100,
        Substance.Green
    )

    @Test
    fun testDrain() {
        mineral.drain(10)

        assertEquals(90, mineral.size)
    }
}