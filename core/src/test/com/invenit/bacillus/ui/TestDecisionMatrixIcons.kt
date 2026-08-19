package com.invenit.bacillus.ui

import com.invenit.bacillus.model.matrix.Action
import com.invenit.bacillus.model.matrix.Comparator
import com.invenit.bacillus.model.matrix.Sensor
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import kotlin.test.assertEquals

internal class TestDecisionMatrixIcons {

    @ParameterizedTest
    @CsvSource(
        "Rest, , RestBars",
        "Move, TowardConsume, Seek",
        "Move, AwayFromToxin, Flee",
        "Move, TowardOpenSpace, Explore",
        "Move, Random, Random",
        "Move, Hold, Hold",
    )
    fun testActionToIcon(category: Action.Category, mode: Action.Mode?, expected: ActionIcon) {
        assertEquals(expected, Action(category, mode).toIcon())
    }

    @ParameterizedTest
    @CsvSource(
        "FoodDistance, Dot",
        "EnergyRatio, Diamond",
    )
    fun testSensorToGlyph(sensor: Sensor, expected: SensorGlyph) {
        assertEquals(expected, sensor.toGlyph())
    }

    @ParameterizedTest
    @CsvSource(
        "GreaterThanOrEqual, Up",
        "LessThan, Down",
    )
    fun testComparatorToChevron(comparator: Comparator, expected: ChevronDirection) {
        assertEquals(expected, comparator.toChevron())
    }

    @ParameterizedTest
    @CsvSource(
        "4, Forward",
        "-3, Backward",
        "0, Neutral",
    )
    fun testJumpOffsetToDirection(jumpOffset: Int, expected: JumpDirection) {
        assertEquals(expected, jumpOffset.toJumpDirection())
    }
}
