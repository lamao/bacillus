package com.invenit.bacillus.stage

import com.invenit.bacillus.model.*
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
class TestConsumeStage {

    private lateinit var stage: ConsumeStep

    @BeforeTest
    fun before() {
        stage = ConsumeStep()
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
                Substance.Red,
                false
            )
        ))

        stage.execute(field)

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
                Substance.Red,
                false
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

        stage.execute(field)

        // energy is not 60 because of production performance which reduces consuming efficiency
        assertEquals( 59, cell.energy,"Energy should increase. Current: ${cell.energy}")
        assertEquals(90, food.size, "Food size should decrease. Current: ${food.size}")
    }

    @Test
    fun testMobileDoesNotConsumeMinerals() {
        val cell = Organic(
            Point(1, 1),
            100,
            Point.Zero,
            DNA(
                Substance.Green,
                Substance.Yellow,
                Substance.White,
                Substance.Red,
                true
            )
        )
        val food = Mineral(
            Point(1, 2),
            100,
            Substance.Yellow
        )
        val field = Field(10, 10)
        field.add(cell)
        field.add(food)

        stage.execute(field)

        assertEquals(100, cell.energy, "Energy should not change for mobile cell")
        assertEquals(100, food.size, "Food size should not change for mobile cell")
    }
}