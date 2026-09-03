package com.invenit.bacillus.ui

import com.invenit.bacillus.model.matrix.Action
import com.invenit.bacillus.model.matrix.Comparator
import com.invenit.bacillus.model.matrix.Instruction
import com.invenit.bacillus.model.matrix.Sensor
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

internal class TestDecisionMatrixFormatting {

    @Test
    fun testMoveInstructionDisplayText() {
        val instruction = Instruction(
            action = Action(Action.Category.Move, Action.Mode.TowardConsume),
            sensor = Sensor.FoodDistance,
            comparator = Comparator.LessThan,
            threshold = 2.0,
            jumpOffset = 4
        )

        assertEquals("Move:seek\nF<2.00\n+4", instruction.toDisplayText())
    }

    @Test
    fun testRestInstructionDisplayText() {
        val instruction = Instruction(
            action = Action(Action.Category.Rest),
            sensor = Sensor.EnergyRatio,
            comparator = Comparator.GreaterThanOrEqual,
            threshold = 0.6,
            jumpOffset = -3
        )

        assertEquals("Rest\nE>=0.60\n-3", instruction.toDisplayText())
    }

    @Test
    fun testZeroJumpOffsetIsShownAsPositive() {
        val instruction = Instruction(
            action = Action(Action.Category.Rest),
            sensor = Sensor.EnergyRatio,
            comparator = Comparator.GreaterThanOrEqual,
            threshold = 0.0,
            jumpOffset = 0
        )

        assertEquals("Rest\nE>=0.00\n+0", instruction.toDisplayText())
    }

    @Test
    fun testSplitInstructionDisplayText() {
        val instruction = Instruction(
            action = Action(Action.Category.Split),
            sensor = Sensor.SizeRatio,
            comparator = Comparator.GreaterThanOrEqual,
            threshold = 0.9,
            jumpOffset = 1
        )

        assertEquals("Split\nS>=0.90\n+1", instruction.toDisplayText())
    }

    @Test
    fun testProduceInstructionDisplayText() {
        val instruction = Instruction(
            action = Action(Action.Category.Produce, Action.Mode.Release),
            sensor = Sensor.ToxinDistance,
            comparator = Comparator.LessThan,
            threshold = 1.0,
            jumpOffset = -2
        )

        assertEquals("Produce:rel\nT<1.00\n-2", instruction.toDisplayText())
    }

    @ParameterizedTest
    @CsvSource(
        "FoodDistance, F",
        "ToxinDistance, T",
        "EnergyRatio, E",
        "SizeRatio, S",
        "Age, A",
        "Crowding, C",
        "Random, R",
    )
    fun testSensorAbbreviations(sensor: Sensor, expectedAbbreviation: String) {
        val instruction = Instruction(
            action = Action(Action.Category.Rest),
            sensor = sensor,
            comparator = Comparator.GreaterThanOrEqual,
            threshold = 0.0,
            jumpOffset = 0
        )

        assertEquals("Rest\n${expectedAbbreviation}>=0.00\n+0", instruction.toDisplayText())
    }

    @ParameterizedTest
    @CsvSource(
        "Move, TowardConsume, seek",
        "Move, AwayFromToxin, flee",
        "Move, TowardOpenSpace, open",
        "Move, Random, rand",
        "Move, Hold, hold",
        "Produce, Release, rel",
        "Produce, Hoard, hoard",
    )
    fun testModeAbbreviations(category: Action.Category, mode: Action.Mode, expectedAbbreviation: String) {
        val instruction = Instruction(
            action = Action(category, mode),
            sensor = Sensor.EnergyRatio,
            comparator = Comparator.GreaterThanOrEqual,
            threshold = 0.0,
            jumpOffset = 0
        )

        assertEquals("$category:$expectedAbbreviation\nE>=0.00\n+0", instruction.toDisplayText())
    }
}
