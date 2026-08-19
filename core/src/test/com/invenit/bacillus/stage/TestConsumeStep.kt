package com.invenit.bacillus.stage

import com.invenit.bacillus.model.*
import com.invenit.bacillus.model.matrix.Action
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 * @author viacheslav.mishcheriakov
 * Created: 13.12.21
 */
@ExtendWith(MockitoExtension::class)
class TestConsumeStep {

    private lateinit var step: ConsumeStep

    @BeforeTest
    fun before() {
        step = ConsumeStep()
    }

    @Test
    fun testConsumeSun() {
        val field = Field(10, 10)
        field.add(Organic(
            Point(1, 1),
            1000,
            Point.Zero,
            DNA(
                Substance.Green,
                Substance.Sun,
                Substance.Blue,
                Substance.Red
            )
        ))

        step.execute(field)

        val cell = field[1, 1]
        assertTrue(cell is Organic)
        assertEquals(1000, cell.energy)
        assertEquals(1023, cell.size)
        assertEquals(2, cell.accumulatedWaste)
    }

    @Test
    fun testConsumeMinerals() {
        val cell = Organic(
            Point(1, 1),
            100,
            Point.Zero,
            DNA(
                Substance.Green,
                Substance.Yellow,
                Substance.White,
                Substance.Red
            )
        )
        // Set energy lower than size to allow consumption to show up in energy gain
        cell.energy = 50
        
        val food = Mineral(
            Point(1, 2),
            100,
            Substance.Yellow
        )
        val field = Field(10, 10)
        field.add(cell)
        field.add(food)

        step.execute(field)

        // energy is not 60 because of production performance which reduces consuming efficiency
        assertEquals( 59, cell.energy,"Energy should increase. Current: ${cell.energy}")
        assertEquals(90, food.size, "Food size should decrease. Current: ${food.size}")
    }

    @Test
    fun testCellThatChoseMoveDoesNotConsumeMinerals() {
        val cell = Organic(
            Point(1, 1),
            100,
            Point.Zero,
            DNA(
                Substance.Green,
                Substance.Yellow,
                Substance.White,
                Substance.Red
            )
        )
        cell.chosenAction = Action(Action.Category.Move, Action.Mode.TowardConsume)
        val food = Mineral(
            Point(1, 2),
            100,
            Substance.Yellow
        )
        val field = Field(10, 10)
        field.add(cell)
        field.add(food)

        step.execute(field)

        assertEquals(100, cell.energy, "Energy should not change when the chosen action is Move")
        assertEquals(100, food.size, "Food size should not change when the chosen action is Move")
    }
}