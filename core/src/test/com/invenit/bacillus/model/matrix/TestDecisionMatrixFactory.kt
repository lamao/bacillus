package com.invenit.bacillus.model.matrix

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

internal class TestDecisionMatrixFactory {

    @ParameterizedTest(name = "initial()[{0}] hunts and drops to rest once low energy fires")
    @CsvSource(
        "0", "1", "8", "17",
    )
    fun testInitialHuntStateJumpsToRestOnLowEnergy(index: Int) {
        val matrix = DecisionMatrixFactory.initial()

        val result = matrix.evaluate(index, sensorValue = 0.1)   // below the low-energy threshold

        assertEquals(Action(Action.Category.Move, Action.Mode.TowardConsume), result.action)
        assertEquals(18, result.nextIndex)
    }

    @ParameterizedTest(name = "initial()[{0}] keeps hunting while energy holds")
    @CsvSource(
        "0, 1", "8, 9", "17, 18",
    )
    fun testInitialHuntStateAdvancesWhileEnergyHolds(index: Int, expectedNext: Int) {
        val matrix = DecisionMatrixFactory.initial()

        val result = matrix.evaluate(index, sensorValue = 0.9)   // above the low-energy threshold

        assertEquals(Action(Action.Category.Move, Action.Mode.TowardConsume), result.action)
        assertEquals(expectedNext, result.nextIndex)
    }

    @ParameterizedTest(name = "initial()[{0}] rests and jumps back to hunting once energy recovers")
    @CsvSource(
        "18", "20", "24",
    )
    fun testInitialRestStateJumpsToHuntOnRecoveredEnergy(index: Int) {
        val matrix = DecisionMatrixFactory.initial()

        val result = matrix.evaluate(index, sensorValue = 0.9)   // above the recovered-energy threshold

        assertEquals(Action(Action.Category.Rest), result.action)
        assertEquals(0, result.nextIndex)
    }

    @ParameterizedTest(name = "initial()[{0}] keeps resting while energy stays low")
    @CsvSource(
        "18, 19", "20, 21", "24, 0",
    )
    fun testInitialRestStateAdvancesWhileEnergyStaysLow(index: Int, expectedNext: Int) {
        val matrix = DecisionMatrixFactory.initial()

        val result = matrix.evaluate(index, sensorValue = 0.1)   // below the recovered-energy threshold

        assertEquals(Action(Action.Category.Rest), result.action)
        assertEquals(expectedNext, result.nextIndex)
    }

}
