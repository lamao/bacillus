package com.invenit.bacillus.stage

import com.invenit.bacillus.model.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Created by vyacheslav.mischeryakov
 * Created 09.12.2021
 */

class TestToxinStage {

    private lateinit var stage: ToxinStage

    @BeforeTest
    fun before() {
        stage = ToxinStage()
    }

    @Test
    fun testWhenNoToxinsNearby() {
        val field = Field(10, 10)
        field.add(Mineral(Point(0, 0), 100, Substance.Red))
        field.add(Mineral(Point(1, 0), 100, Substance.Green))

        field.add(organic(1, 1, 1000, Substance.Blue))

        stage.execute(field)

        val cell = field[1, 1]
        assertTrue(cell is Organic)
        assertEquals(1000, cell.energy)
        assertEquals(1000, cell.size)

        assertEquals(100, field[0, 0]!!.size)
        assertEquals(100, field[1, 0]!!.size)
    }

    @Test
    fun testWhenItIsToxinsNearby() {
        val field = Field(10, 10)
        field.add(Mineral(Point(0, 0), 100, Substance.Blue))
        field.add(Mineral(Point(1, 0), 100, Substance.Green))
        field.add(Mineral(Point(3, 0), 100, Substance.Blue))

        field.add(organic(1, 1, 1000, Substance.Blue))

        stage.execute(field)

        val cell = field[1, 1]
        assertTrue(cell is Organic)
        assertEquals(850, cell.energy)
        assertEquals(1000, cell.size)

        assertEquals(100, field[0, 0]!!.size)
        assertEquals(100, field[1, 0]!!.size)
        assertEquals(100, field[3, 0]!!.size)

    }

    private fun organic(x: Int, y: Int, size: Int, toxin: Substance) = Organic(
        Point(x, y),
        size,
        Point.Zero,
        DNA(
            Substance.Green,
            Substance.Sun,
            Substance.Yellow,
            toxin,
            false
        )
    )
}