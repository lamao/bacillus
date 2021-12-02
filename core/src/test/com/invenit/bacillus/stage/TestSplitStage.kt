package com.invenit.bacillus.stage

import com.invenit.bacillus.model.*
import com.invenit.bacillus.service.RandomService
import com.invenit.bacillus.service.RandomServiceImpl
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.BeforeTest
import kotlin.test.assertNotNull

/**
 * Created by vyacheslav.mischeryakov
 * Created 02.12.2021
 */
@ExtendWith(MockitoExtension::class)
class TestSplitStage {

    private lateinit var stage : SplitStage

    @Mock
    private lateinit var mockRandomService : RandomService

    @BeforeTest
    fun before() {
        stage = SplitStage(mockRandomService)

    }

    @Test
    fun testSuccessfulSplit() {
        val field = Field(10, 10)
        field.add(organic(1, 1, 5000, 5000))

        Mockito.`when`(mockRandomService.random(-1, 1)).thenReturn(1, 1)
        stage.execute(field)

        val offspring = field[2, 2]
        assertNotNull(offspring)

        TODO("Complete")
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