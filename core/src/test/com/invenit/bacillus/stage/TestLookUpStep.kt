package com.invenit.bacillus.stage

import com.invenit.bacillus.model.*
import com.invenit.bacillus.service.RandomService
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 *
 * @author viacheslav.mishcheriakov
 * Created: 13.12.21
 */
@ExtendWith(MockitoExtension::class)
class TestLookUpStep {

    @Mock
    private lateinit var mockRandomService: RandomService

    private lateinit var step: LookUpStep

    @BeforeTest
    fun before() {
        step = LookUpStep(mockRandomService)
    }

    @Test
    fun testWhenNoSuitableFoodAndNoMovingSelected() {
        `when`(mockRandomService.random(-1, 1)).thenReturn(0, 0)
        val cell = organic(Point(1, 1))
        val mineral = Mineral(Point(2, 1), 100, Substance.Red)
        val field = Field(3, 3)
        field.add(cell)
        field.add(mineral)

        step.execute(field)

        assertEquals(Field.NoDirection, cell.direction)
        assertEquals(100, cell.energy)
    }

    @Test
    fun testWhenNoSuitableFoodAndMovingIsSelected() {
        `when`(mockRandomService.random(-1, 1)).thenReturn(1, -1)
        val cell = organic(Point(1, 1))
        val mineral = Mineral(Point(2, 1), 100, Substance.Red)
        val field = Field(3, 3)
        field.add(cell)
        field.add(mineral)

        step.execute(field)

        assertEquals(Point(1, -1), cell.direction)
    }

    @Test
    fun testWhenSingleFoodCellFound() {
        val cell = organic(Point(1, 1))
        val food = Mineral(Point(2, 1), 100, Substance.Yellow)
        val field = Field(3, 3)
        field.add(cell)
        field.add(food)

        step.execute(field)

        assertEquals(Point(1, 0), cell.direction)
    }

    @Test
    fun testWhenMultipleFoodCellsFound() {
        val cell = organic(Point(1, 1))
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

    private fun organic(position: Point): Organic {
        return Organic(
            position,
            100,
            Field.NoDirection,
            DNA(
                Substance.Green,
                Substance.Yellow,
                Substance.White,
                Substance.Red,
                true
            )
        )
    }

}
