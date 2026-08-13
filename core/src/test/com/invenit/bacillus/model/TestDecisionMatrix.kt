package com.invenit.bacillus.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

internal class TestDecisionMatrix {

    private val filler = Instruction(
        action = Action(Action.Category.Rest),
        sensor = Sensor.EnergyRatio,
        comparator = Comparator.GreaterThanOrEqual,
        threshold = 0.0,
        jumpOffset = 0
    )

    private fun matrixWith(index: Int, instruction: Instruction): DecisionMatrix {
        val instructions = MutableList(DecisionMatrix.SIZE) { filler }
        instructions[index] = instruction
        return DecisionMatrix(instructions)
    }

    @Test
    fun testConstructionRejectsWrongSize() {
        assertThrows<IllegalArgumentException> {
            DecisionMatrix(List(DecisionMatrix.SIZE - 1) { filler })
        }
    }

    @Test
    fun testConstructionAcceptsExactSize() {
        val matrix = DecisionMatrix(List(DecisionMatrix.SIZE) { filler })

        assertEquals(filler, matrix[0])
        assertEquals(filler, matrix[DecisionMatrix.SIZE - 1])
    }

    @ParameterizedTest(name = "get({0}) reads state 3")
    @CsvSource(
        "3",     // no wrap
        "28",    // wraps forward past the end (28 mod 25 = 3)
        "-22",   // wraps backward past the start (-22 mod 25 = 3)
    )
    fun testGetWrapsIndex(index: Int) {
        val marker = filler.copy(threshold = 42.0)
        val matrix = matrixWith(3, marker)

        assertEquals(marker, matrix[index])
    }

    @ParameterizedTest(name = "next({0}) = {1}")
    @CsvSource(
        "0, 1",
        "10, 11",
        "24, 0",   // implicit +1 wraps forward past the last state
    )
    fun testNext(index: Int, expected: Int) {
        val matrix = DecisionMatrix(List(DecisionMatrix.SIZE) { filler })

        assertEquals(expected, matrix.next(index))
    }

    @ParameterizedTest(name = "jump({0}, {1}) = {2}")
    @CsvSource(
        "0, 4, 4",      // plain jump
        "0, 30, 5",     // offset larger than the matrix wraps forward
        "0, -1, 24",    // negative offset wraps backward past the start
        "24, 2, 1",     // jump from the last state wraps forward
        "10, -15, 20",  // large negative offset wraps backward
    )
    fun testJump(index: Int, offset: Int, expected: Int) {
        val matrix = DecisionMatrix(List(DecisionMatrix.SIZE) { filler })

        assertEquals(expected, matrix.jump(index, offset))
    }

    @Test
    fun testEvaluateMoveInstructionConditionMetJumps() {
        val move = Instruction(
            action = Action(Action.Category.Move, Action.Mode.TowardConsume),
            sensor = Sensor.FoodDistance,
            comparator = Comparator.LessThan,
            threshold = 2.0,
            jumpOffset = 4
        )
        val matrix = matrixWith(0, move)

        val result = matrix.evaluate(0, sensorValue = 1.0)

        assertEquals(move.action, result.action)
        assertEquals(4, result.nextIndex)
    }

    @Test
    fun testEvaluateMoveInstructionConditionNotMetAdvances() {
        val move = Instruction(
            action = Action(Action.Category.Move, Action.Mode.TowardConsume),
            sensor = Sensor.FoodDistance,
            comparator = Comparator.LessThan,
            threshold = 2.0,
            jumpOffset = 4
        )
        val matrix = matrixWith(0, move)

        val result = matrix.evaluate(0, sensorValue = 5.0)

        assertEquals(move.action, result.action)
        assertEquals(1, result.nextIndex)
    }

    @Test
    fun testEvaluateRestInstructionConditionMetJumpsAndWraps() {
        val rest = Instruction(
            action = Action(Action.Category.Rest),
            sensor = Sensor.EnergyRatio,
            comparator = Comparator.GreaterThanOrEqual,
            threshold = 0.5,
            jumpOffset = 10
        )
        val matrix = matrixWith(20, rest)

        val result = matrix.evaluate(20, sensorValue = 0.9)

        assertEquals(rest.action, result.action)
        assertEquals(5, result.nextIndex)   // (20 + 10) mod 25
    }

    @Test
    fun testEvaluateRestInstructionConditionNotMetAdvances() {
        val rest = Instruction(
            action = Action(Action.Category.Rest),
            sensor = Sensor.EnergyRatio,
            comparator = Comparator.GreaterThanOrEqual,
            threshold = 0.5,
            jumpOffset = 10
        )
        val matrix = matrixWith(20, rest)

        val result = matrix.evaluate(20, sensorValue = 0.1)

        assertEquals(rest.action, result.action)
        assertEquals(21, result.nextIndex)
    }
}
