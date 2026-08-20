package com.invenit.bacillus.stage

import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.*
import com.invenit.bacillus.model.matrix.*
import com.invenit.bacillus.service.RandomService
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * #1 §6, task 5 (#10). Covers the act-then-test rule (chosen action stamped,
 * currentState advances/jumps) and the Move(TowardConsume) direction lookup
 * this task ports over from the retired LookUpStep.
 */
@ExtendWith(MockitoExtension::class)
class TestDecideStep {

    @Mock
    private lateinit var mockRandomService: RandomService

    private lateinit var step: DecideStep

    private val filler = Instruction(
        action = Action(Action.Category.Rest),
        sensor = Sensor.EnergyRatio,
        comparator = Comparator.GreaterThanOrEqual,
        threshold = 0.0,
        jumpOffset = 0
    )

    @BeforeTest
    fun before() {
        step = DecideStep(mockRandomService)
    }

    @Test
    fun testMoveTowardConsumeWithNoSuitableFoodAndNoMovingSelected() {
        `when`(mockRandomService.random(-1, 1)).thenReturn(0, 0)
        val cell = organic(Point(1, 1), moveTowardConsumeMatrix())
        val mineral = Mineral(Point(2, 1), 100, Substance.Red)
        val field = Field(3, 3)
        field.add(cell)
        field.add(mineral)

        step.execute(field)

        assertEquals(Field.NoDirection, cell.direction)
        assertEquals(Action(Action.Category.Move, Action.Mode.TowardConsume), cell.chosenAction)
    }

    @Test
    fun testMoveTowardConsumeWithNoSuitableFoodAndRandomMovingSelected() {
        `when`(mockRandomService.random(-1, 1)).thenReturn(1, -1)
        val cell = organic(Point(1, 1), moveTowardConsumeMatrix())
        val mineral = Mineral(Point(2, 1), 100, Substance.Red)
        val field = Field(3, 3)
        field.add(cell)
        field.add(mineral)

        step.execute(field)

        assertEquals(Point(1, -1), cell.direction)
    }

    @Test
    fun testMoveTowardConsumeWithSingleFoodCellFound() {
        val cell = organic(Point(1, 1), moveTowardConsumeMatrix())
        val food = Mineral(Point(2, 1), 100, Substance.Yellow)
        val field = Field(3, 3)
        field.add(cell)
        field.add(food)

        step.execute(field)

        assertEquals(Point(1, 0), cell.direction)
    }

    @Test
    fun testMoveTowardConsumeWithMultipleFoodCellsFoundPicksLargest() {
        val cell = organic(Point(1, 1), moveTowardConsumeMatrix())
        val smallerFood = Mineral(Point(0, 1), 50, Substance.Yellow)
        val largerFood = Mineral(Point(2, 2), 100, Substance.Yellow)
        val nonFood = Mineral(Point(1, 2), 200, Substance.Blue)
        val field = Field(3, 3)
        field.add(cell)
        field.add(smallerFood)
        field.add(largerFood)
        field.add(nonFood)

        step.execute(field)

        assertEquals(Point(1, 1), cell.direction)
    }

    @Test
    fun testCapturesPositionBeforeThisTicAsPreviousPosition() {
        val cell = organic(Point(1, 1), DecisionMatrix.default())
        val field = Field(3, 3)
        field.add(cell)

        step.execute(field)
        assertEquals(Point(1, 1), cell.previousPosition)

        // Simulate MoveStep relocating the cell later in the same tic.
        field.relocate(cell, Point(2, 1))
        step.execute(field)

        assertEquals(Point(2, 1), cell.previousPosition)
    }

    @Test
    fun testRestActionSetsNoDirectionRegardlessOfPreviousDirection() {
        val cell = organic(Point(1, 1), DecisionMatrix.default())
        cell.direction = Point(1, 0)
        val field = Field(3, 3)
        field.add(cell)

        step.execute(field)

        assertEquals(Field.NoDirection, cell.direction)
        assertEquals(Action(Action.Category.Rest), cell.chosenAction)
    }

    @Test
    fun testConditionNotMetAdvancesCurrentStateByOne() {
        val matrix = matrixWith(
            0, Instruction(
                action = Action(Action.Category.Rest),
                sensor = Sensor.EnergyRatio,
                comparator = Comparator.LessThan,
                threshold = 0.0,
                jumpOffset = 10
            )
        )
        val cell = organic(Point(1, 1), matrix)
        val field = Field(3, 3)
        field.add(cell)

        step.execute(field)

        assertEquals(1, cell.currentState)
    }

    @Test
    fun testConditionMetJumpsCurrentStateByOffset() {
        val matrix = matrixWith(
            0, Instruction(
                action = Action(Action.Category.Rest),
                sensor = Sensor.EnergyRatio,
                comparator = Comparator.GreaterThanOrEqual,
                threshold = 0.0,
                jumpOffset = 10
            )
        )
        val cell = organic(Point(1, 1), matrix)
        val field = Field(3, 3)
        field.add(cell)

        step.execute(field)

        assertEquals(10, cell.currentState)
    }

    @Test
    fun testFoodDistanceSensorReflectsNearestMatchingFood() {
        val matrix = matrixWith(
            0, Instruction(
                action = Action(Action.Category.Rest),
                sensor = Sensor.FoodDistance,
                comparator = Comparator.LessThan,
                threshold = 2.0,
                jumpOffset = 5
            )
        )
        val cell = organic(Point(1, 1), matrix)
        val food = Mineral(Point(2, 1), 50, Substance.Yellow)
        val field = Field(3, 3)
        field.add(cell)
        field.add(food)

        step.execute(field)

        // distance 1 < threshold 2 -> condition met -> jump
        assertEquals(5, cell.currentState)
    }

    @Test
    fun testFoodDistanceSensorWhenNothingInRangeReadsAsFarAway() {
        val matrix = matrixWith(
            0, Instruction(
                action = Action(Action.Category.Rest),
                sensor = Sensor.FoodDistance,
                comparator = Comparator.LessThan,
                threshold = (Settings.VisionRange + 1).toDouble(),
                jumpOffset = 5
            )
        )
        val cell = organic(Point(1, 1), matrix)
        val field = Field(3, 3)
        field.add(cell)

        step.execute(field)

        // sentinel (VisionRange + 1) is never < an equal threshold -> advance
        assertEquals(1, cell.currentState)
    }

    private fun matrixWith(index: Int, instruction: Instruction): DecisionMatrix {
        val instructions = MutableList(DecisionMatrix.SIZE) { filler }
        instructions[index] = instruction
        return DecisionMatrix(instructions)
    }

    private fun moveTowardConsumeMatrix(): DecisionMatrix = matrixWith(
        0, Instruction(
            action = Action(Action.Category.Move, Action.Mode.TowardConsume),
            sensor = Sensor.EnergyRatio,
            comparator = Comparator.GreaterThanOrEqual,
            threshold = 0.0,
            jumpOffset = 0
        )
    )

    private fun organic(position: Point, decisionMatrix: DecisionMatrix): Organic {
        return Organic(
            position,
            100,
            Field.NoDirection,
            DNA(
                Substance.Green,
                Substance.Yellow,
                Substance.White,
                Substance.Red,
                decisionMatrix
            )
        )
    }
}
