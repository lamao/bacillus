package com.invenit.bacillus

import org.junit.jupiter.api.Test
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SettingsTest {

    private val originalTicDelaySeconds = Settings.TicDelaySeconds
    private val originalReproductionThreshold = Settings.ReproductionThreshold
    private val originalBiteYield = Settings.BiteYield

    @AfterTest
    fun after() {
        Settings.TicDelaySeconds = originalTicDelaySeconds
        Settings.ReproductionThreshold = originalReproductionThreshold
        Settings.BiteYield = originalBiteYield
    }

    @Test
    fun testToxinDamageFunctionAtDistanceOne() {
        val damage = Settings.toxinDamageFunction(100f, 1)

        assertEquals(100f, damage)
    }

    @Test
    fun testToxinDamageFunctionHalvesPerDistanceStep() {
        val atTwo = Settings.toxinDamageFunction(100f, 2)
        val atThree = Settings.toxinDamageFunction(100f, 3)

        assertEquals(50f, atTwo)
        assertEquals(25f, atThree)
    }

    @Test
    fun testToxinDamageFunctionWithZeroAmount() {
        val damage = Settings.toxinDamageFunction(0f, 3)

        assertEquals(0f, damage)
    }

    @Test
    fun testCorrectedMineralsYieldReturnsAmountUnchanged() {
        assertEquals(42f, Settings.correctedMineralsYield(42f, 1))
        assertEquals(42f, Settings.correctedMineralsYield(42f, 5))
    }

    @Test
    fun testSmoothAnimationWhenDelayAboveThreshold() {
        Settings.TicDelaySeconds = 0.3f

        assertTrue(Settings.SmoothAnimation)
    }

    @Test
    fun testSmoothAnimationWhenDelayAtThreshold() {
        Settings.TicDelaySeconds = 0.2f

        assertFalse(Settings.SmoothAnimation)
    }

    @Test
    fun testSmoothAnimationWhenDelayBelowThreshold() {
        Settings.TicDelaySeconds = 0.02f

        assertFalse(Settings.SmoothAnimation)
    }

    @Test
    fun testMaxSizeIsReproductionThresholdPlusBiteYield() {
        Settings.ReproductionThreshold = 2000
        Settings.BiteYield = 200

        assertEquals(2200, Settings.MaxSize)
    }

    @Test
    fun testMaxSizeTracksSettingChanges() {
        Settings.ReproductionThreshold = 500
        Settings.BiteYield = 50

        assertEquals(550, Settings.MaxSize)
    }
}
