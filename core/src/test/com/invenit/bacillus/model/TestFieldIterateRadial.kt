package com.invenit.bacillus.model

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Created by viacheslav.mishcheriakov
 * Created 22.11.2021
 */
class TestFieldIterateRadial {

    lateinit var field: Field

    @BeforeTest
    fun before() {
        field = Field(20, 10)
    }

    @Test
    fun testCornerPositions() {
        assertEquals(8, countCellsVisited(Point(0, 0), 2))
        assertEquals(8, countCellsVisited(Point(0, 9), 2))
        assertEquals(8, countCellsVisited(Point(19, 9), 2))
        assertEquals(8, countCellsVisited(Point(19, 0), 2))

        assertEquals(11, countCellsVisited(Point(1, 0), 2))
        assertEquals(11, countCellsVisited(Point(0, 8), 2))
        assertEquals(14, countCellsVisited(Point(17, 9), 2))
        assertEquals(11, countCellsVisited(Point(19, 1), 2))
    }

    @Test
    fun testMiddlePositions() {
        assertEquals(8, countCellsVisited(Point(5, 5), 1))
        assertEquals(24, countCellsVisited(Point(5, 5), 2))
        assertEquals(48, countCellsVisited(Point(5, 5), 3))
    }

    @Test
    fun testStopsIteratingImmediatelyWhenActionReturnsFalseInBottomRow() {
        val visited = mutableListOf<Point>()

        field.iterateRadial(Point(5, 5), 2) { x, y ->
            visited.add(Point(x, y))
            // Bottom-left corner of the range-1 ring, visited by the 3rd
            // (bottom row) inner loop. If the early return there didn't work,
            // the whole range-2 ring would still get visited afterward.
            Point(x, y) != Point(4, 4)
        }

        assertEquals(Point(4, 4), visited.last())
    }

    @Test
    fun testStopsIteratingImmediatelyWhenActionReturnsFalseInLeftColumn() {
        val visited = mutableListOf<Point>()

        field.iterateRadial(Point(5, 5), 2) { x, y ->
            visited.add(Point(x, y))
            // Left-middle cell of the range-1 ring, visited by the 4th
            // (left column) inner loop — the last cell of that ring.
            Point(x, y) != Point(4, 5)
        }

        assertEquals(Point(4, 5), visited.last())
    }

    private fun countCellsVisited(point: Point, range: Int): Int {
        var counter = 0
        field.iterateRadial(point, range) { _, _ ->
            counter++
            true
        }
        return counter
    }

}
