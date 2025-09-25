package com.invenit.bacillus.stage

import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 *
 * @author viacheslav.mishcheriakov
 * Created: 30.11.21
 */
class TestClearExhaustedItemsStage {

    private val stage: ClearExhaustedItemsStage = ClearExhaustedItemsStage()

    private lateinit var field: Field

    @BeforeTest
    fun before() {
        field = Field(10, 10)
    }

    @Test
    fun testRegularCase() {
        field.add(mineral(x = 0, y = 0, Substance.Red, 10))
        field.add(mineral(x = 1, y = 1, Substance.Blue, 0))
        field.add(organic(x = 2, y = 2, Substance.White, energy = 10, size = 100, age = 10))
        field.add(organic(x = 3, y = 3, Substance.Green, energy = 0, size = 200, age = 10))
        field.add(organic(x = 4, y = 4, Substance.Red, energy = 10, size = 30, age = Settings.MaxAge))
        field.add(organic(x = 5, y = 5, Substance.Blue, energy = 0, size = 0, age = 5))

        stage.execute(field)

        val expectedOrganics = setOf(
            organic(x = 2, y = 2, Substance.White, energy = 10, size = 100, age = 10)
        )
        assertEquals(expectedOrganics, field.organics.toSet())

        val expectedMinerals = setOf(
            mineral(x = 0, y = 0, Substance.Red, 10),
            mineral(x = 3, y = 3, Substance.Green, 200),
            mineral(x = 4, y = 4, Substance.Red, 30)
        )
        assertEquals(expectedMinerals, field.minerals.toSet())
    }

    private fun mineral(x: Int, y: Int, body: Substance, size: Int): Mineral = Mineral(
        Point(x, y),
        size,
        body
    )

    private fun organic(x: Int, y: Int, body: Substance, energy: Int, size: Int, age: Int): Organic {

        val result = Organic(
            Point(x, y),
            size,
            Point(0, 0),
            DNA(
                body,
                Substance.White,
                Substance.Red,
                Substance.Green,
                false
            )
        )
        result.energy = energy
        result.age = age
        return result
    }
}