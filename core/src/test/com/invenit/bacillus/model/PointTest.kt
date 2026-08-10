package com.invenit.bacillus.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class PointTest {

    @Test
    fun testZero() {
        assertEquals(Point(0, 0), Point.Zero)
    }

    @Test
    fun testPlus() {
        val result = Point(1, 2) + Point(3, 4)

        assertEquals(Point(4, 6), result)
    }

    @Test
    fun testPlusWithNegative() {
        val result = Point(1, 2) + Point(-3, -4)

        assertEquals(Point(-2, -2), result)
    }

    @Test
    fun testMinus() {
        val result = Point(5, 7) - Point(2, 3)

        assertEquals(Point(3, 4), result)
    }

    @Test
    fun testMinusWithNegativeResult() {
        val result = Point(2, 3) - Point(5, 7)

        assertEquals(Point(-3, -4), result)
    }

    @Test
    fun testDistanceSameCell() {
        val distance = Point(5, 5).distance(5, 5)

        assertEquals(0, distance)
    }

    @Test
    fun testDistanceAxisAligned() {
        val distance = Point(0, 0).distance(0, 3)

        assertEquals(3, distance)
    }

    @Test
    fun testDistanceIsChebyshev() {
        // Chebyshev distance: max of the coordinate deltas, not Euclidean/Manhattan
        val distance = Point(0, 0).distance(3, 1)

        assertEquals(3, distance)
    }

    @Test
    fun testDistanceIsSymmetric() {
        val forward = Point(2, 5).distance(-1, 1)
        val backward = Point(-1, 1).distance(2, 5)

        assertEquals(forward, backward)
    }

    @Test
    fun testDistanceWithNegativeCoordinates() {
        val distance = Point(-2, -2).distance(2, 2)

        assertEquals(4, distance)
    }

    @Test
    fun testDirectionToSameCell() {
        val direction = Point(4, 4).direction(4, 4)

        assertEquals(Point(0, 0), direction)
    }

    @Test
    fun testDirectionPositive() {
        val direction = Point(0, 0).direction(5, 5)

        assertEquals(Point(1, 1), direction)
    }

    @Test
    fun testDirectionNegative() {
        val direction = Point(5, 5).direction(0, 0)

        assertEquals(Point(-1, -1), direction)
    }

    @Test
    fun testDirectionIsNormalizedRegardlessOfMagnitude() {
        val direction = Point(0, 0).direction(1, 10)

        assertEquals(Point(1, 1), direction)
    }

    @Test
    fun testDirectionMixedSigns() {
        val direction = Point(0, 0).direction(-3, 7)

        assertEquals(Point(-1, 1), direction)
    }
}
