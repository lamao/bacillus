package com.invenit.bacillus.service

import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.DNA
import com.invenit.bacillus.model.Organic
import com.invenit.bacillus.model.Point
import com.invenit.bacillus.model.Substance
import com.invenit.bacillus.model.matrix.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
internal class TestCreatureFactoryImpl {

    private lateinit var factory: CreatureFactory
    @Mock
    private lateinit var mockDecisionMatrixFactory: DecisionMatrixFactory

    @BeforeTest
    fun before() {
        `when`(mockDecisionMatrixFactory.initial()).thenReturn(DecisionMatrix.default())
        factory = CreatureFactoryImpl(mockDecisionMatrixFactory)
    }

    @Test
    fun testDefaultLastDNA() {
        val expectedDNA = DNA(Substance.Green, Substance.Sun, Substance.White, Substance.Red, DecisionMatrix.default())
        assertEquals(expectedDNA, factory.lastDNA)
    }

    @Test
    fun testDefaultLastSize() {
        assertEquals(Settings.DefaultSize, factory.lastSize)
    }

    @Test
    fun testCreateOrganicUsesGivenPositionAndLastDNAAndSize() {
        val organic = factory.createOrganic(Point(3, 4))

        assertEquals(
            Organic(Point(3, 4), Settings.DefaultSize, Point.Zero, factory.lastDNA),
            organic
        )
    }

    @Test
    fun testCreateOrganicPicksUpChangedLastDNAAndSize() {
        val newMatrix = DecisionMatrix(List(DecisionMatrix.SIZE) {
            Instruction(
                action = Action(Action.Category.Move, Action.Mode.TowardConsume),
                sensor = Sensor.EnergyRatio,
                comparator = Comparator.LessThan,
                threshold = 10.0,
                jumpOffset = 1
            )
        })

        val newDNA = DNA(Substance.Blue, Substance.Yellow, Substance.Red, Substance.Green, newMatrix)
        factory.lastDNA = newDNA
        factory.lastSize = 500

        val organic = factory.createOrganic(Point(0, 0))

        assertEquals(newDNA, organic.dna)
        assertEquals(500, organic.size)
    }

    @Test
    fun testCreateOrganicReturnsIndependentInstances() {
        val first = factory.createOrganic(Point(0, 0))
        val second = factory.createOrganic(Point(1, 1))

        first.size = 999

        assertEquals(Settings.DefaultSize, second.size)
    }
}
