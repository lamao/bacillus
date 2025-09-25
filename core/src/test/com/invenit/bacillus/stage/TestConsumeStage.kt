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

    private lateinit var stage: ConsumeStage

    @BeforeTest
    fun before() {
        stage = ConsumeStage()
    }

    @Test
    fun testConsumeSun() {
        val field = Field(10, 10)
        field.add(organic(1, 1))

        stage.execute(field)

        val cell = field[1, 1]
        assertTrue(cell is Organic)
        assertEquals(1000, cell.energy)
        assertEquals(1023, cell.size)
        assertEquals(2, cell.accumulatedWaste)
    }

    @Suppress("SameParameterValue")
    private fun organic(x: Int, y: Int) = Organic(
        Point(x, y),
        1000,
        Point.Zero,
        DNA(
            Substance.Green,
            Substance.Sun,
            Substance.Blue,
            Substance.Red,
            false
        )
    )

    @Test
    fun testConsumeMineralsWhenNothingToConsume() {
        TODO("Implement")
    }

    @Test
    fun testConsumeMinerals() {
        TODO("Implement")
    }
}