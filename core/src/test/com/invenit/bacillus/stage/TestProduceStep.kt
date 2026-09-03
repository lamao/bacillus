package com.invenit.bacillus.stage

import com.invenit.bacillus.model.DNA
import com.invenit.bacillus.model.Field
import com.invenit.bacillus.model.Mineral
import com.invenit.bacillus.model.Organic
import com.invenit.bacillus.model.Point
import com.invenit.bacillus.model.Substance
import com.invenit.bacillus.model.matrix.Action
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 *
 * @author viacheslav.mishcheriakov
 * Created: 13.12.21
 */
@ExtendWith(MockitoExtension::class)
class TestProduceStep {

    private lateinit var step: ProduceStep
    private lateinit var field: Field

    @BeforeTest
    fun before() {
        step = ProduceStep()
        field = Field(5, 5)
    }

    @Test
    fun testRegularProductionWhenNoAccumulatedWaste() {
        val cell = organic(2, 2, 100, Substance.Yellow)
        field.add(cell)
        step.execute(field)

        for (x in 1..3) {
            for (y in 1..3) {
                if (cell.position != Point(x, y)) {
                    assertNull(field[x, y])
                }
            }
        }
    }

    @Test
    fun testRegularProductionNoMineralNearby() {
        val cell = organic(2, 2, 100, Substance.Yellow)
        cell.accumulatedWaste = 100
        val mineral = Mineral(Point(1, 3), 100, Substance.Green)
        field.add(cell)
        field.add(mineral)

        step.execute(field)

        assertEquals(0, cell.accumulatedWaste)
        assertNotNull(field[2, 3])
        assertEquals(100, field[2, 3]!!.size)
        assertNotNull(field[1, 3])
        assertEquals(100, field[1, 3]!!.size)
    }

    @Test
    fun testRegularProductionSameMineralNearby() {
        val cell = organic(2, 2, 100, Substance.Yellow)
        cell.accumulatedWaste = 100
        val mineral1 = Mineral(Point(3, 3), 100, Substance.Yellow)
        val mineral2 = Mineral(Point(2, 1), 50, Substance.Yellow)
        field.add(cell)
        field.add(mineral1)
        field.add(mineral2)

        step.execute(field)

        assertEquals(0, cell.accumulatedWaste)
        assertNotNull(field[3, 3])
        assertEquals(200, field[3, 3]!!.size)
        assertNotNull(field[2, 1])
        assertEquals(50, field[2, 1]!!.size)
    }

    @Test
    fun testWhenNoFreeSpaceLeft() {
        val cell = organic(2, 2, 150, Substance.Yellow)
        cell.accumulatedWaste = 100

        field.add(cell)
        for (x in cell.position.x - 1..cell.position.x + 1) {
            for (y in cell.position.y - 1..cell.position.y + 1) {
                if (cell.position != Point(x, y)) {
                    field.add(Mineral(Point(x, y), 100, Substance.Green))
                }
            }
        }

        step.execute(field)

        assertEquals(0, cell.accumulatedWaste)
        assertEquals(50, cell.energy)

        for (x in cell.position.x - 1..cell.position.x + 1) {
            for (y in cell.position.y - 1..cell.position.y + 1) {
                if (cell.position != Point(x, y)) {
                    assertEquals(100, field[x, y]!!.size)
                }
            }
        }

    }

    @Test
    fun testDoesNotProduceWhenChosenActionIsNotProduceReleaseEvenWithAccumulatedWaste() {
        val cell = organic(2, 2, 100, Substance.Yellow)
        cell.chosenAction = Action(Action.Category.Rest)
        cell.accumulatedWaste = 100
        field.add(cell)

        step.execute(field)

        assertEquals(100, cell.accumulatedWaste)
        for (x in 1..3) {
            for (y in 1..3) {
                if (cell.position != Point(x, y)) {
                    assertNull(field[x, y])
                }
            }
        }
    }

    @Test
    fun testDoesNotProduceWhenChosenActionIsProduceHoard() {
        val cell = organic(2, 2, 100, Substance.Yellow)
        cell.chosenAction = Action(Action.Category.Produce, Action.Mode.Hoard)
        cell.accumulatedWaste = 100
        field.add(cell)

        step.execute(field)

        assertEquals(100, cell.accumulatedWaste)
        for (x in 1..3) {
            for (y in 1..3) {
                if (cell.position != Point(x, y)) {
                    assertNull(field[x, y])
                }
            }
        }
    }

    fun organic(x: Int, y: Int, size: Int = 100, produce: Substance) =
        Organic(
            Point(x, y),
            size,
            Point.Zero,
            DNA(
                Substance.Green,
                Substance.Sun,
                produce,
                Substance.Red
            )
        ).also { it.chosenAction = Action(Action.Category.Produce, Action.Mode.Release) }

}