package com.invenit.bacillus

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TestSettings {

    private val originalTicsPerSecond = Settings.TicsPerSecond
    private val originalReproductionThreshold = Settings.ReproductionThreshold
    private val originalBiteYield = Settings.BiteYield

    @AfterTest
    fun after() {
        Settings.TicsPerSecond = originalTicsPerSecond
        Settings.ReproductionThreshold = originalReproductionThreshold
        Settings.BiteYield = originalBiteYield
        Settings.pause = false
        Settings.Debug.displayGrid = false
        Settings.Debug.displaySourcePosition = false
    }

    @ParameterizedTest(name = "toxinDamageFunction({0}, {1}) = {2}")
    @CsvSource(
        "100, 1, 100",  // no falloff at distance 1
        "100, 2, 50",   // halves per distance step
        "100, 3, 25",
        "0, 3, 0",      // zero amount
    )
    fun testToxinDamageFunction(amount: Float, distance: Int, expectedDamage: Float) {
        val damage = Settings.toxinDamageFunction(amount, distance)

        assertEquals(expectedDamage, damage)
    }

    @ParameterizedTest(name = "correctedMineralsYield({0}, {1}) = {0}")
    @CsvSource(
        "42, 1",
        "42, 5",
    )
    fun testCorrectedMineralsYieldReturnsAmountUnchanged(amount: Float, distance: Int) {
        assertEquals(amount, Settings.correctedMineralsYield(amount, distance))
    }

    @ParameterizedTest(name = "SmoothAnimation at TicsPerSecond={0} is {1}")
    @CsvSource(
        "0, false",     // paused
        "3.33, true",   // below threshold
        "5, false",     // at threshold
        "50, false",    // above threshold
    )
    fun testSmoothAnimation(ticsPerSecond: Float, expected: Boolean) {
        Settings.TicsPerSecond = ticsPerSecond

        assertEquals(expected, Settings.SmoothAnimation)
    }

    @ParameterizedTest(name = "MaxSize with ReproductionThreshold={0}, BiteYield={1} = {2}")
    @CsvSource(
        "2000, 200, 2200",
        "500, 50, 550",
    )
    fun testMaxSizeIsReproductionThresholdPlusBiteYield(
        reproductionThreshold: Int,
        biteYield: Int,
        expectedMaxSize: Int,
    ) {
        Settings.ReproductionThreshold = reproductionThreshold
        Settings.BiteYield = biteYield

        assertEquals(expectedMaxSize, Settings.MaxSize)
    }

    @Test
    fun testPauseDefaultsToFalseAndIsMutable() {
        assertFalse(Settings.pause)

        Settings.pause = true

        assertTrue(Settings.pause)
    }

    @Test
    fun testDebugFlagsDefaultToFalseAndAreMutable() {
        assertFalse(Settings.Debug.displayGrid)
        assertFalse(Settings.Debug.displaySourcePosition)

        Settings.Debug.displayGrid = true
        Settings.Debug.displaySourcePosition = true

        assertTrue(Settings.Debug.displayGrid)
        assertTrue(Settings.Debug.displaySourcePosition)
    }
}
