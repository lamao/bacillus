package com.invenit.bacillus.stage

import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.*
import com.invenit.bacillus.service.MutationService
import com.invenit.bacillus.service.RandomService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Created by vyacheslav.mischeryakov
 * Created 02.12.2021
 */
@ExtendWith(MockitoExtension::class)
class TestSplitStage {

    private lateinit var stage: SplitStage

    @Mock
    private lateinit var mockRandomService: RandomService

    @Mock
    private lateinit var mockMutationService: MutationService

    @BeforeTest
    fun before() {
        stage = SplitStage(mockRandomService, mockMutationService)

    }

    @Test
    fun testSuccessfulSplit() {
        val field = Field(10, 10)
        field.add(organic(1, 1, 5000, 5000))
        field.add(organic(0, 0, 100, 100))

        `when`(mockRandomService.random(-1, 1)).thenReturn(1, 1)
        `when`(mockMutationService.mutatedSize(Settings.DefaultSize)).thenReturn(Settings.DefaultSize)
        `when`(mockMutationService.mutatedDna(any())).thenReturn(
            DNA(
                Substance.Yellow,
                Substance.Sun,
                Substance.Blue,
                Substance.Red,
                true
            )
        )
        stage.execute(field)

        val offspring = field[2, 2]
        assertNotNull(offspring)

        val expectedOffSpring = Organic(
            position = Point(2, 2),
            direction = Field.NoDirection,
            size = 750,
            dna = DNA(
                Substance.Yellow,
                Substance.Sun,
                Substance.Blue,
                Substance.Red,
                true
            )
        )

        assertEquals(expectedOffSpring, offspring)

        val parent = field[1, 1]
        assertNotNull(parent)
        assertTrue(parent is Organic)
        assertEquals(4250, parent.energy)
        assertEquals(4250, parent.size)

    }

    @Test
    fun testFailedSplitBecauseOffspingLocationIsOccupied() {
        val field = Field(10, 10)
        field.add(organic(1, 1, 5000, 5000))
        field.add(organic(2, 2, 100, 100))

        `when`(mockRandomService.random(-1, 1)).thenReturn(1, 1)
        `when`(mockMutationService.mutatedSize(Settings.DefaultSize)).thenReturn(750)
        stage.execute(field)

        val somethingInDesiredLocation = field[2, 2]
        assertNotNull(somethingInDesiredLocation)
        assertEquals(100, somethingInDesiredLocation.size)

        val parent = field[1, 1]
        assertNotNull(parent)
        assertTrue(parent is Organic)
        assertEquals(4625, parent.energy)
        assertEquals(5000, parent.size)

    }

    @Test
    fun testFailedSplitBecauseOffspringLocationIsOutside() {
        val field = Field(10, 10)
        field.add(organic(0, 0, 5000, 5000))

        `when`(mockRandomService.random(-1, 1)).thenReturn(-1, -1)
        `when`(mockMutationService.mutatedSize(Settings.DefaultSize)).thenReturn(750)
        stage.execute(field)

        val parent = field[0, 0]
        assertNotNull(parent)
        assertTrue(parent is Organic)
        assertEquals(4625, parent.energy)
        assertEquals(5000, parent.size)

    }

    private fun organic(x: Int, y: Int, energy: Int, size: Int): Organic {
        val result = Organic(
            Point(x, y),
            size,
            Point.Zero,
            DNA(
                Substance.Green,
                Substance.Sun,
                Substance.Blue,
                Substance.Red,
                true
            )
        )
        result.energy = energy
        return result
    }
}