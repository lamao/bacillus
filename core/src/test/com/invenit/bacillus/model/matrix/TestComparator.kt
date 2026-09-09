package com.invenit.bacillus.model.matrix

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

internal class TestComparator {

    @ParameterizedTest(name = "{0} < {1} = {2}")
    @CsvSource(
        "1, 2, true",
        "2, 2, false",
        "3, 2, false",
    )
    fun testLessThan(value: Int, threshold: Int, expected: Boolean) {
        assertEquals(expected, Comparator.LessThan.test(value, threshold))
    }

    @ParameterizedTest(name = "{0} >= {1} = {2}")
    @CsvSource(
        "1, 2, false",
        "2, 2, true",
        "3, 2, true",
    )
    fun testGreaterThanOrEqual(value: Int, threshold: Int, expected: Boolean) {
        assertEquals(expected, Comparator.GreaterThanOrEqual.test(value, threshold))
    }
}
