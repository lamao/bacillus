package com.invenit.bacillus.model

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
    fun testStopsAsSoonAsActionReturnsFalse() {
        var counter = 0
        field.iterateRadial(Point(5, 5), 3) { _, _ ->
            counter++
            counter < 5
        }
        assertEquals(5, counter, "Iteration should stop right after the 5th visited cell")
    }

    @Test
    fun testContinuesWhenActionAlwaysReturnsTrue() {
        assertEquals(48, countCellsVisited(Point(5, 5), 3))
    }

    @Test
    fun testVisitsOnlyInsideCellsWhenAnchorIsAtEdge() {
        var counter = 0
        field.iterateRadial(Point(0, 0), 3) { x, y ->
            assertTrue(field.isInside(x, y), "Visited cell [$x,$y] should be inside the field")
            counter++
            true
        }
        assertEquals(15, counter)
    }

    @Test
    fun testCapturedLocalMutableStateReflectsVisitedCells() {
        // Regression guard for the closure-capture allocation issue (#28):
        // a mutable local `var`/reference type accumulated across callback
        // invocations must reflect every visited cell, exactly as it did
        // before `iterateRadial` was marked `inline`.
        val visited = mutableSetOf<Point>()
        field.iterateRadial(Point(5, 5), 1) { x, y ->
            visited.add(Point(x, y))
            true
        }

        val expected = setOf(
            Point(4, 4), Point(5, 4), Point(6, 4),
            Point(4, 5), Point(6, 5),
            Point(4, 6), Point(5, 6), Point(6, 6),
        )
        assertEquals(expected, visited)
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

    // Field is 20x10. One row/column below the threshold on each side is
    // "near" (routes to the checked path); exactly at the threshold is the
    // last anchor still safe for the unchecked path.
    @ParameterizedTest
    @CsvSource(
        "10, 5, 2, false", // deep interior
        "2, 5, 2, false",  // exactly at the left threshold - not near
        "1, 5, 2, true",   // one inside the left threshold - near
        "17, 5, 2, false", // exactly at the right threshold - not near
        "18, 5, 2, true",  // one past the right threshold - near
        "10, 2, 2, false", // exactly at the top threshold - not near
        "10, 1, 2, true",  // one inside the top threshold - near
        "10, 7, 2, false", // exactly at the bottom threshold - not near
        "10, 8, 2, true",  // one past the bottom threshold - near
    )
    fun testIsNearSides(x: Int, y: Int, range: Int, expected: Boolean) {
        assertEquals(expected, field.isNearSides(Point(x, y), range))
    }

    @Test
    fun testIterateRadialSimpleThrowsWhenAnchorIsNearAnEdge() {
        // Documents why iterateRadial's isNearSides guard exists:
        // iterateRadialSimple performs no bounds checking of its own, so an
        // anchor too close to an edge hands a caller's action out-of-range
        // coordinates. It's the action's own field access (field[x, y], as
        // every real caller does) that then indexes the grid out of bounds -
        // iterateRadialSimple itself never touches the grid.
        assertFailsWith<IndexOutOfBoundsException> {
            field.iterateRadialSimple(Point(0, 0), 2) { x, y -> field[x, y] != null }
        }
    }

    @Test
    fun testSimpleAndNearSidesPathsVisitTheSameCellsInTheSameOrder() {
        // Field(20, 10) with anchor (10, 5) and range 2 is safe for both
        // paths (confirmed by testIsNearSides above) - they must agree.
        val anchor = Point(10, 5)
        assertFalse(field.isNearSides(anchor, 2), "Test anchor must be safe for iterateRadialSimple")

        val visitedBySimple = mutableListOf<Point>()
        field.iterateRadialSimple(anchor, 2) { x, y -> visitedBySimple.add(Point(x, y)); true }

        val visitedByNearSides = mutableListOf<Point>()
        field.iterateRadialNearSides(anchor, 2) { x, y -> visitedByNearSides.add(Point(x, y)); true }

        assertEquals(visitedByNearSides, visitedBySimple)
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
