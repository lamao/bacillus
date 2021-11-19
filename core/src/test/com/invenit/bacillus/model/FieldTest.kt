package com.invenit.bacillus.model

import com.invenit.bacillus.FieldException
import org.junit.jupiter.api.Assertions
import kotlin.test.*

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
    fun testAdd() {
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

    @Test
    fun testRemove() {
        field.add(
            Mineral(
                Point(1, 1),
                10,
                Substance.Green
            )
        )
        field.add(
            Organic(
                Point(0, 0),
                5,
                Point(0, 0),
                Substance.Green,
                Substance.White,
                Substance.Red,
                false
            )
        )

        field.remove(Point(3, 3))
        assertEquals(1, field.organics.size)
        assertEquals(1, field.minerals.size)
        assertNotNull(field[0, 0])
        assertNotNull(field[1, 1])

        field.remove(Point(0, 0))
        assertEquals(0, field.organics.size)
        assertEquals(1, field.minerals.size)
        assertNull(field[0, 0])
        assertNotNull(field[1, 1])


        field.remove(Point(1, 1))
        assertEquals(0, field.organics.size)
        assertEquals(0, field.minerals.size)
        assertNull(field[0, 0])
        assertNull(field[1, 1])

    }
}