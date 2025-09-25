package com.invenit.bacillus.stage

import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Created by viacheslav.mishcheriakov
 * Created 29.11.2021
 */
class TestAdjustCountersStage {

    private val stage = AdjustCountersStage()

    @Test
    fun testOrganics() {
        val field = Field(10, 10)

        val organic1 = organic(x = 0, y = 0, energy = 100, age = 150)

        val organic2 = organic(x = 0, y = 1, energy = 200, age = 300)

        field.add(organic1)
        field.add(organic2)

        stage.execute(field)

        assertEquals(100 - Settings.PermanentConsumption, organic1.energy)
        assertEquals(151, organic1.age)
        assertEquals(200 - Settings.PermanentConsumption, organic2.energy)
        assertEquals(301, organic2.age)
    }

    @Test
    fun testMinerals() {
        val field = Field(10, 10)
        val mineral1 = mineral(x = 0, y = 0, size = 100)
        val mineral2 = mineral(x = 1, y = 1, size = 200)

        field.add(mineral1)
        field.add(mineral2)

        stage.execute(field)

        assertEquals(100 - Settings.MineralDegradation, mineral1.size)
        assertEquals(200 - Settings.MineralDegradation, mineral2.size)
    }

    private fun mineral(x: Int, y: Int, size: Int) = Mineral(
        Point(x, y),
        size,
        Substance.Red
    )


    @Suppress("SameParameterValue")
    private fun organic(x: Int, y: Int, energy: Int, age: Int): Organic {
        val result = Organic(
            Point(x, y),
            10,
            Point(0, 0),
            DNA(
                Substance.Sun,
                Substance.White,
                Substance.Yellow,
                Substance.Red,
                true
            )
        )

        result.energy = energy
        result.age = age

        return result
    }

}