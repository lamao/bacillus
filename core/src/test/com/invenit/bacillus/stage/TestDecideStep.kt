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
    fun testMoveHoldSetsNoDirection() {
        val cell = organic(Point(1, 1), matrixWithAction(Action(Action.Category.Move, Action.Mode.Hold)))
        cell.direction = Point(1, 0)
        val field = Field(3, 3)
        field.add(cell)

        step.execute(field)

        assertEquals(Field.NoDirection, cell.direction)
        assertEquals(Action(Action.Category.Move, Action.Mode.Hold), cell.chosenAction)
    }

    @Test
    fun testMoveRandomModeFallsBackToRandomDirection() {
        `when`(mockRandomService.random(-1, 1)).thenReturn(1, 0)
        val cell = organic(Point(1, 1), matrixWithAction(Action(Action.Category.Move, Action.Mode.Random)))
        val field = Field(3, 3)
        field.add(cell)

        step.execute(field)

        assertEquals(Point(1, 0), cell.direction)
    }

    @Test
    fun testRandomDirectionReturnsNoDirectionWhenItWouldLeaveTheField() {
        `when`(mockRandomService.random(-1, 1)).thenReturn(-1, -1)
        val cell = organic(Point(0, 0), moveTowardConsumeMatrix())
        val mineral = Mineral(Point(1, 0), 100, Substance.Red)
        val field = Field(3, 3)
        field.add(cell)
        field.add(mineral)

        step.execute(field)

        assertEquals(Field.NoDirection, cell.direction)
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

    @Test
    fun testToxinDistanceSensorReflectsNearestMatchingToxin() {
        val matrix = matrixWith(
            0, Instruction(
                action = Action(Action.Category.Rest),
                sensor = Sensor.ToxinDistance,
                comparator = Comparator.LessThan,
                threshold = 2.0,
                jumpOffset = 5
            )
        )
        val cell = organic(Point(1, 1), matrix)
        val toxin = Mineral(Point(2, 1), 50, Substance.Red)
        val field = Field(3, 3)
        field.add(cell)
        field.add(toxin)

        step.execute(field)

        // distance 1 < threshold 2 -> condition met -> jump
        assertEquals(5, cell.currentState)
    }

    @Test
    fun testToxinDistanceSensorWhenNothingInRangeReadsAsFarAway() {
        val matrix = matrixWith(
            0, Instruction(
                action = Action(Action.Category.Rest),
                sensor = Sensor.ToxinDistance,
                comparator = Comparator.LessThan,
                threshold = (Settings.ToxinRange + 1).toDouble(),
                jumpOffset = 5
            )
        )
        val cell = organic(Point(1, 1), matrix)
        val field = Field(3, 3)
        field.add(cell)

        step.execute(field)

        // sentinel (ToxinRange + 1) is never < an equal threshold -> advance
        assertEquals(1, cell.currentState)
    }

    @Test
    fun testSizeRatioSensorAboveThresholdJumps() {
        val matrix = matrixWith(
            0, Instruction(
                action = Action(Action.Category.Rest),
                sensor = Sensor.SizeRatio,
                comparator = Comparator.GreaterThanOrEqual,
                threshold = 0.5,
                jumpOffset = 3
            )
        )
        val cell = organic(Point(1, 1), matrix, size = Settings.MaxSize)
        val field = Field(3, 3)
        field.add(cell)

        step.execute(field)

        assertEquals(3, cell.currentState)
    }

    @Test
    fun testSizeRatioSensorBelowThresholdAdvances() {
        val matrix = matrixWith(
            0, Instruction(
                action = Action(Action.Category.Rest),
                sensor = Sensor.SizeRatio,
                comparator = Comparator.GreaterThanOrEqual,
                threshold = 0.5,
                jumpOffset = 3
            )
        )
        val cell = organic(Point(1, 1), matrix, size = 10)
        val field = Field(3, 3)
        field.add(cell)

        step.execute(field)

        assertEquals(1, cell.currentState)
    }

    @Test
    fun testAgeSensorReflectsAgeOverMaxAge() {
        val matrix = matrixWith(
            0, Instruction(
                action = Action(Action.Category.Rest),
                sensor = Sensor.Age,
                comparator = Comparator.GreaterThanOrEqual,
                threshold = 0.5,
                jumpOffset = 3
            )
        )
        val cell = organic(Point(1, 1), matrix)
        cell.age = (Settings.MaxAge * 0.6).toInt()
        val field = Field(3, 3)
        field.add(cell)

        step.execute(field)

        assertEquals(3, cell.currentState)
    }

    @Test
    fun testAgeSensorBelowThresholdAdvances() {
        val matrix = matrixWith(
            0, Instruction(
                action = Action(Action.Category.Rest),
                sensor = Sensor.Age,
                comparator = Comparator.GreaterThanOrEqual,
                threshold = 0.5,
                jumpOffset = 3
            )
        )
        val cell = organic(Point(1, 1), matrix)
        cell.age = (Settings.MaxAge * 0.1).toInt()
        val field = Field(3, 3)
        field.add(cell)

        step.execute(field)

        assertEquals(1, cell.currentState)
    }

    @Test
    fun testCrowdingSensorCountsOnlyOrganicsWithinVisionRange() {
        val matrix = matrixWith(
            0, Instruction(
                action = Action(Action.Category.Rest),
                sensor = Sensor.Crowding,
                comparator = Comparator.GreaterThanOrEqual,
                threshold = 2.0,
                jumpOffset = 4
            )
        )
        val cell = organic(Point(1, 1), matrix)
        val neighborOrganic1 = organic(Point(0, 1), DecisionMatrix.default())
        val neighborOrganic2 = organic(Point(2, 1), DecisionMatrix.default())
        val neighborMineral = Mineral(Point(1, 0), 50, Substance.Blue)
        val field = Field(3, 3)
        field.add(cell)
        field.add(neighborOrganic1)
        field.add(neighborOrganic2)
        field.add(neighborMineral)

        step.execute(field)

        // 2 organics within range >= threshold 2 -> jump; the mineral doesn't count
        assertEquals(4, cell.currentState)
    }

    @Test
    fun testRandomSensorUsesFreshDrawFromRandomService() {
        `when`(mockRandomService.random()).thenReturn(0.5f)
        val matrix = matrixWith(
            0, Instruction(
                action = Action(Action.Category.Rest),
                sensor = Sensor.Random,
                comparator = Comparator.GreaterThanOrEqual,
                threshold = 0.5,
                jumpOffset = 6
            )
        )
        val cell = organic(Point(1, 1), matrix)
        val field = Field(3, 3)
        field.add(cell)

        step.execute(field)

        assertEquals(6, cell.currentState)
    }

    @Test
    fun testMoveAwayFromToxinWithToxinNearby() {
        val cell = organic(Point(1, 1), matrixWithAction(Action(Action.Category.Move, Action.Mode.AwayFromToxin)))
        val toxin = Mineral(Point(2, 1), 100, Substance.Red)
        val field = Field(3, 3)
        field.add(cell)
        field.add(toxin)

        step.execute(field)

        assertEquals(Point(-1, 0), cell.direction)
    }

    @Test
    fun testMoveAwayFromToxinWithNoToxinNearbyFallsBackToRandomDirection() {
        `when`(mockRandomService.random(-1, 1)).thenReturn(1, 0)
        val cell = organic(Point(1, 1), matrixWithAction(Action(Action.Category.Move, Action.Mode.AwayFromToxin)))
        val field = Field(3, 3)
        field.add(cell)

        step.execute(field)

        assertEquals(Point(1, 0), cell.direction)
    }

    @Test
    fun testMoveTowardOpenSpaceStepsAwayFromCrowdCentroid() {
        val cell = organic(Point(1, 1), matrixWithAction(Action(Action.Category.Move, Action.Mode.TowardOpenSpace)))
        val crowdMember = Mineral(Point(2, 1), 100, Substance.Blue)
        val field = Field(3, 3)
        field.add(cell)
        field.add(crowdMember)

        step.execute(field)

        assertEquals(Point(-1, 0), cell.direction)
    }

    @Test
    fun testMoveTowardOpenSpaceWithNothingNearbyFallsBackToRandomDirection() {
        `when`(mockRandomService.random(-1, 1)).thenReturn(1, 0)
        val cell = organic(Point(1, 1), matrixWithAction(Action(Action.Category.Move, Action.Mode.TowardOpenSpace)))
        val field = Field(3, 3)
        field.add(cell)

        step.execute(field)

        assertEquals(Point(1, 0), cell.direction)
    }

    @Test
    fun testMoveTowardOpenSpaceWithSymmetricCrowdFallsBackToRandomDirection() {
        `when`(mockRandomService.random(-1, 1)).thenReturn(1, 0)
        val cell = organic(Point(1, 1), matrixWithAction(Action(Action.Category.Move, Action.Mode.TowardOpenSpace)))
        val left = Mineral(Point(0, 1), 100, Substance.Blue)
        val right = Mineral(Point(2, 1), 100, Substance.Blue)
        val field = Field(3, 3)
        field.add(cell)
        field.add(left)
        field.add(right)

        step.execute(field)

        assertEquals(Point(1, 0), cell.direction)
    }

    @Test
    fun testSplitActionSetsNoDirection() {
        val cell = organic(Point(1, 1), matrixWithAction(Action(Action.Category.Split)))
        cell.direction = Point(1, 0)
        val field = Field(3, 3)
        field.add(cell)

        step.execute(field)

        assertEquals(Field.NoDirection, cell.direction)
        assertEquals(Action(Action.Category.Split), cell.chosenAction)
    }

    @Test
    fun testProduceReleaseActionSetsNoDirection() {
        val cell = organic(Point(1, 1), matrixWithAction(Action(Action.Category.Produce, Action.Mode.Release)))
        cell.direction = Point(1, 0)
        val field = Field(3, 3)
        field.add(cell)

        step.execute(field)

        assertEquals(Field.NoDirection, cell.direction)
        assertEquals(Action(Action.Category.Produce, Action.Mode.Release), cell.chosenAction)
    }

    private fun matrixWith(index: Int, instruction: Instruction): DecisionMatrix {
        val instructions = MutableList(DecisionMatrix.SIZE) { filler }
        instructions[index] = instruction
        return DecisionMatrix(instructions)
    }

    private fun moveTowardConsumeMatrix(): DecisionMatrix = matrixWithAction(
        Action(Action.Category.Move, Action.Mode.TowardConsume)
    )

    private fun matrixWithAction(action: Action): DecisionMatrix = matrixWith(
        0, Instruction(
            action = action,
            sensor = Sensor.EnergyRatio,
            comparator = Comparator.GreaterThanOrEqual,
            threshold = 0.0,
            jumpOffset = 0
        )
    )

    private fun organic(position: Point, decisionMatrix: DecisionMatrix, size: Int = 100): Organic {
        return Organic(
            position,
            size,
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
