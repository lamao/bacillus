package com.invenit.bacillus.ui

import com.invenit.bacillus.model.matrix.Action
import com.invenit.bacillus.model.matrix.Comparator
import com.invenit.bacillus.model.matrix.Instruction
import com.invenit.bacillus.model.matrix.Sensor
import org.junit.jupiter.api.Test
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

        assertEquals("Mv:seek\nF<2.00\n+4", instruction.toDisplayText())
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
}
