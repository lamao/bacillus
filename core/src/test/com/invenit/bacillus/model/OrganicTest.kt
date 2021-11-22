package com.invenit.bacillus.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Created by vyacheslav.mischeryakov
 * Created 18.11.2021
 */
internal class OrganicTest {

    @Test
    fun testDrainFromMax() {
        val organic = createOrganicForDrain(100, 100)

        organic.drain(10)

        assertEquals(90, organic.size)
        assertEquals(90, organic.energy)
    }

    @Test
    fun testDrainWhenEnergyNotFull() {
        val organic = createOrganicForDrain(95, 100)

        organic.drain(10)

        assertEquals(90, organic.size)
        assertEquals(90, organic.energy)
    }

    @Test
    fun testDrainWhenEnergyMuchLower() {
        val organic = createOrganicForDrain(50, 100)

        organic.energy = 50

        organic.drain(10)

        assertEquals(90, organic.size)
        assertEquals(50, organic.energy)
    }

    private fun createOrganicForDrain(energy: Int, size: Int): Organic {
        val organic = Organic(
            Point(1, 1),
            size,
            Point(1, 1),
            DNA(
                Substance.Green,
            Substance.Red,
            Substance.White,
            true
            )
        )
        organic.energy = energy
        return organic
    }
}