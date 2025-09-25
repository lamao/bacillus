package com.invenit.bacillus.stage

import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.*
import org.junit.jupiter.api.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 *
 * @author viacheslav.mishcheriakov
 * Created: 30.11.21
 */
class TestMoveStage {

    private val stage = MoveStage()

    private lateinit var field: Field

    @BeforeTest
    fun before() {
        field = Field(10, 10)
    }

    @Test
    fun testNoDirection() {
        val mobile = organic(Point(0, 0), Point.Zero, true)
        val stationary = organic(Point(1, 1), Point.Zero, false)
        field.add(mobile)
        field.add(stationary)

        stage.execute(field)

        assertEquals(Point(0, 0), mobile.position)
        assertEquals(mobile, field[0, 0])
        assertEquals(Point(1, 1), stationary.position)
        assertEquals(stationary, field[1, 1])
    }

    @Test
    fun testMoveToFreePosition() {
        val cell = organic(Point(1, 2), Point(1, 1), true)
        field.add(cell)

        stage.execute(field)

        assertEquals(Point(2, 3), cell.position)
        assertNull(field[1, 2])
        assertEquals(cell, field[2, 3])
    }

    @Test
    fun testMoveToNonEatableSubstance() {
        val cell = Organic(
            Point(1, 2),
            100,
            Point(-1, 1),
            DNA(
                Substance.Yellow,
                Substance.Green,
                Substance.White,
                Substance.Red,
                true
            )
        )
        val food = Mineral(
            Point(0, 3),
            100,
            Substance.Blue
        )
        field.add(cell)
        field.add(food)

        stage.execute(field)

        assertEquals(Point(1, 2), cell.position)
        assertEquals(cell, field[1, 2])
        assertEquals(Point(0, 3), food.position)
        assertEquals(food, field[0, 3])

        assertEquals(100, food.size)
        assertEquals(100, cell.size)
    }

    @Test
    fun testMoveToPositionWithFood() {
        val cell = Organic(
            Point(1, 2),
            100,
            Point(-1, 1),
            DNA(
                Substance.Green,
                Substance.Yellow,
                Substance.White,
                Substance.Red,
                true
            )
        )
        val food = Mineral(
            Point(0, 3),
            50,
            Substance.Yellow
        )
        field.add(cell)
        field.add(food)

        stage.execute(field)

        assertEquals(Point(1, 2), cell.position)
        assertEquals(cell, field[1, 2])
        assertEquals(Point(0, 3), food.position)
        assertEquals(food, field[0, 3])

        assertEquals(0, food.size)
        assertEquals(145, cell.size)
        assertEquals(5, cell.accumulatedWaste)
    }

    @Test
    fun testWhenCanEatItSelfAndDoesNotMove() {
        val cell = Organic(
            Point(1, 1),
            100,
            Point.Zero,
            DNA(
                Substance.Green,
                Substance.Green,
                Substance.White,
                Substance.Red,
                true
            )
        )
        field.add(cell)

        stage.execute(field)

        assertEquals(Point(1, 1), cell.position)
        assertEquals(cell, field[1, 1])

        assertEquals(100, cell.size)
        assertEquals(0, cell.accumulatedWaste)
    }

    private fun organic(position: Point, direction: Point, canMove: Boolean): Organic {
        return Organic(
            position,
            Settings.BiteYield / 2,
            direction,
            DNA(
                Substance.Yellow,
                Substance.Green,
                Substance.White,
                Substance.Red,
                canMove
            )
        )
    }
}