package com.invenit.bacillus.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

internal class TestPoint {

    @Test
    fun testZero() {
        assertEquals(Point(0, 0), Point.Zero)
    }

    @ParameterizedTest(name = "({0},{1}) + ({2},{3}) = ({4},{5})")
    @CsvSource(
        "1, 2, 3, 4, 4, 6",
        "1, 2, -3, -4, -2, -2",
    )
    fun testPlus(x1: Int, y1: Int, x2: Int, y2: Int, expectedX: Int, expectedY: Int) {
        val result = Point(x1, y1) + Point(x2, y2)

        assertEquals(Point(expectedX, expectedY), result)
    }

    @ParameterizedTest(name = "({0},{1}) - ({2},{3}) = ({4},{5})")
    @CsvSource(
        "5, 7, 2, 3, 3, 4",
        "2, 3, 5, 7, -3, -4",
    )
    fun testMinus(x1: Int, y1: Int, x2: Int, y2: Int, expectedX: Int, expectedY: Int) {
        val result = Point(x1, y1) - Point(x2, y2)

        assertEquals(Point(expectedX, expectedY), result)
    }

    @ParameterizedTest(name = "distance({0},{1}, {2},{3}) = {4}")
    @CsvSource(
        "5, 5, 5, 5, 0",       // same cell
        "0, 0, 0, 3, 3",       // axis aligned
        "0, 0, 3, 1, 3",       // Chebyshev: max of the deltas, not Euclidean/Manhattan
        "-2, -2, 2, 2, 4",     // negative coordinates
    )
    fun testDistance(x1: Int, y1: Int, x2: Int, y2: Int, expectedDistance: Int) {
        val distance = Point(x1, y1).distance(x2, y2)

        assertEquals(expectedDistance, distance)
    }

    @ParameterizedTest(name = "distance({0},{1}, {2},{3}) is symmetric")
    @CsvSource(
        "2, 5, -1, 1",
        "0, 0, 3, 4",
    )
    fun testDistanceIsSymmetric(x1: Int, y1: Int, x2: Int, y2: Int) {
        val forward = Point(x1, y1).distance(x2, y2)
        val backward = Point(x2, y2).distance(x1, y1)

        assertEquals(forward, backward)
    }

    @ParameterizedTest(name = "direction({0},{1}, {2},{3}) = ({4},{5})")
    @CsvSource(
        "4, 4, 4, 4, 0, 0",     // same cell
        "0, 0, 5, 5, 1, 1",     // positive
        "5, 5, 0, 0, -1, -1",   // negative
        "0, 0, 1, 10, 1, 1",    // normalized regardless of magnitude
        "0, 0, -3, 7, -1, 1",   // mixed signs
    )
    fun testDirection(x1: Int, y1: Int, x2: Int, y2: Int, expectedX: Int, expectedY: Int) {
        val direction = Point(x1, y1).direction(x2, y2)

        assertEquals(Point(expectedX, expectedY), direction)
    }
}
