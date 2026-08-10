package com.invenit.bacillus.service

import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.DNA
import com.invenit.bacillus.model.Organic
import com.invenit.bacillus.model.Point
import com.invenit.bacillus.model.Substance
import org.junit.jupiter.api.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

internal class TestCreatureFactoryImpl {

    private lateinit var factory: CreatureFactory

    @BeforeTest
    fun before() {
        factory = CreatureFactoryImpl()
    }

    @Test
    fun testDefaultLastDNA() {
        assertEquals(
            DNA(Substance.Green, Substance.Sun, Substance.White, Substance.Red, false),
            factory.lastDNA
        )
    }

    @Test
    fun testDefaultLastSize() {
        assertEquals(Settings.DefaultSize, factory.lastSize)
    }

    @Test
    fun testCreateOrganicUsesGivenPosition() {
        val organic = factory.createOrganic(Point(3, 4))

        assertEquals(Point(3, 4), organic.position)
    }

    @Test
    fun testCreateOrganicUsesLastDNAAndSize() {
        val organic = factory.createOrganic(Point(0, 0))

        assertEquals(
            Organic(Point(0, 0), Settings.DefaultSize, Point.Zero, factory.lastDNA),
            organic
        )
    }

    @Test
    fun testCreateOrganicHasNoDirection() {
        val organic = factory.createOrganic(Point(1, 1))

        assertEquals(Point.Zero, organic.direction)
    }

    @Test
    fun testCreateOrganicPicksUpChangedLastDNA() {
        val newDNA = DNA(Substance.Blue, Substance.Yellow, Substance.Red, Substance.Green, true)
        factory.lastDNA = newDNA

        val organic = factory.createOrganic(Point(0, 0))

        assertEquals(newDNA, organic.dna)
    }

    @Test
    fun testCreateOrganicPicksUpChangedLastSize() {
        factory.lastSize = 500

        val organic = factory.createOrganic(Point(0, 0))

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
