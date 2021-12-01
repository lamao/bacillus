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
        val organic = createOrganicWithEnergyAndSize(100, 100)

        val actualDrain = organic.drain(10)

        assertEquals(10, actualDrain)
        assertEquals(90, organic.size)
        assertEquals(90, organic.energy)

    }

    @Test
    fun testDrainTooMuch() {
        val organic = createOrganicWithEnergyAndSize(100, 100)

        val actualDrain = organic.drain(1000)

        assertEquals(100, actualDrain)
        assertEquals(0, organic.size)
        assertEquals(0, organic.energy)
    }

    @Test
    fun testDrainWhenEnergyNotFull() {
        val organic = createOrganicWithEnergyAndSize(95, 100)

        val actualDrain = organic.drain(10)

        assertEquals(10, actualDrain)
        assertEquals(90, organic.size)
        assertEquals(90, organic.energy)
    }

    @Test
    fun testDrainWhenEnergyMuchLower() {
        val organic = createOrganicWithEnergyAndSize(50, 100)

        organic.energy = 50

        val actualDrain = organic.drain(10)

        assertEquals(10, actualDrain)
        assertEquals(90, organic.size)
        assertEquals(50, organic.energy)
    }

    private fun createOrganicWithEnergyAndSize(energy: Int, size: Int): Organic {
        val organic = Organic(
            Point(1, 1),
            size,
            Point(1, 1),
            DNA(
                Substance.Green,
                Substance.Red,
                Substance.White,
                Substance.Red,
                true
            )
        )
        organic.energy = energy
        return organic
    }


    @Test
    fun testConsumeWhenEnergyIsVeryLow() {
        val organic = createOrganicWithEnergyAndSize(100, 1000)

        organic.consume(100)
        assertEquals(1000, organic.size)
        assertEquals(190, organic.energy)
        assertEquals(10, organic.accumulatedWaste)

        organic.consume(200)
        assertEquals(1000, organic.size)
        assertEquals(370, organic.energy)
        assertEquals(30, organic.accumulatedWaste)
    }

    @Test
    fun testConsumeWhenItsTimeToGrow() {
        val organic = createOrganicWithEnergyAndSize(950, 1000)

        organic.consume(100)
        assertEquals(1040, organic.size)
        assertEquals(1000, organic.energy)
        assertEquals(10, organic.accumulatedWaste)

        organic.consume(100)
        assertEquals(1090, organic.size)
        assertEquals(1040, organic.energy)
        assertEquals(20, organic.accumulatedWaste)

        organic.consume(50)
        assertEquals(1090, organic.size)
        assertEquals(1085, organic.energy)
        assertEquals(25, organic.accumulatedWaste)
    }
}