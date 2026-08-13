package com.invenit.bacillus.model.matrix

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

internal class TestComparator {

    @ParameterizedTest(name = "{0} < {1} = {2}")
    @CsvSource(
        "1.0, 2.0, true",
        "2.0, 2.0, false",
        "3.0, 2.0, false",
    )
    fun testLessThan(value: Double, threshold: Double, expected: Boolean) {
        assertEquals(expected, Comparator.LessThan.test(value, threshold))
    }

    @ParameterizedTest(name = "{0} >= {1} = {2}")
    @CsvSource(
        "1.0, 2.0, false",
        "2.0, 2.0, true",
        "3.0, 2.0, true",
    )
    fun testGreaterThanOrEqual(value: Double, threshold: Double, expected: Boolean) {
        assertEquals(expected, Comparator.GreaterThanOrEqual.test(value, threshold))
    }
}
