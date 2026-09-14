package com.invenit.bacillus.ui

import com.invenit.bacillus.model.matrix.Action
import com.invenit.bacillus.model.matrix.Comparator
import com.invenit.bacillus.model.matrix.Sensor
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

internal class TestDecisionMatrixIcons {

    @ParameterizedTest
    @CsvSource(
        "Rest, , RestBars",
        "Split, , Split",
        "Move, TowardConsume, Seek",
        "Move, AwayFromToxin, Flee",
        "Move, TowardOpenSpace, Explore",
        "Move, Random, Random",
        "Move, Hold, Hold",
        "Produce, Release, Release",
        "Produce, Retain, Retain",
    )
    fun testActionToIcon(category: Action.Category, mode: Action.Mode?, expected: ActionIcon) {
        assertEquals(expected, Action(category, mode).toIcon())
    }

    @ParameterizedTest
    @CsvSource(
        "FoodDistance, Dot",
        "EnergyRatio, Diamond",
        "ToxinDistance, Triangle",
        "SizeRatio, InvertedTriangle",
        "Age, Cross",
        "Crowding, Cluster",
        "Random, Spark",
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

    @ParameterizedTest
    @CsvSource(
        "RestBars",
        "Seek",
        "Flee",
        "Explore",
        "Random",
        "Hold",
        "Release",
        "Retain",
        "Split",
    )
    fun testEveryActionIconHasABadgeColor(icon: ActionIcon) {
        assertEquals(true, icon.badgeColor().a > 0f)
    }

    @Test
    fun testActionIconsHaveDistinctBadgeColors() {
        val colors = ActionIcon.entries.map { it.badgeColor() }
        assertEquals(ActionIcon.entries.size, colors.distinct().size)
    }

    @ParameterizedTest
    @CsvSource(
        "FoodDistance",
        "ToxinDistance",
        "EnergyRatio",
        "SizeRatio",
        "Age",
        "Crowding",
        "Random",
    )
    fun testEverySensorHasABadgeColor(sensor: Sensor) {
        assertEquals(true, sensor.badgeColor().a > 0f)
    }
}
