package com.invenit.bacillus.model

import com.invenit.bacillus.FieldException
import org.junit.jupiter.api.Assertions
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

/**
 * Created by vyacheslav.mischeryakov
 * Created 19.11.2021
 */
class FieldTest {

    private lateinit var field: Field

    @BeforeTest
    fun before() {
        field = Field(10, 10)
    }

    @Test
    fun testAddOrganics() {
        val organic = Organic(
            Point(1, 1),
            10,
            Point(1, 0),
            Substance.Green,
            Substance.Sun,
            Substance.White,
            false
        )
        field.add(organic)


        val expected = Organic(
            Point(1, 1),
            10,
            Point(1, 0),
            Substance.Green,
            Substance.Sun,
            Substance.White,
            false
        )

        assertEquals(expected, field[1, 1])
        assertEquals(1, field.organics.size)
        assertEquals(0, field.minerals.size)
    }

    @Test
    fun testAddMinerals() {
        val something = Mineral(
            Point(1, 1),
            10,
            Substance.Green,
        )
        field.add(something)


        val expected = Mineral(
            Point(1, 1),
            10,
            Substance.Green,
        )

        assertEquals(expected, field[1, 1])
        assertEquals(0, field.organics.size)
        assertEquals(1, field.minerals.size)
    }

    @Test
    fun testAddOnOccupiedCell() {
        field.add(
            Mineral(
                Point(1, 1),
                10,
                Substance.Green,
            )
        )

        val actual = Assertions.assertThrows(FieldException::class.java) {
            field.add(
                Mineral(
                    Point(1, 1),
                    15,
                    Substance.White
                )
            )
        }

        assertContains(actual.message, "occupied")
    }
}