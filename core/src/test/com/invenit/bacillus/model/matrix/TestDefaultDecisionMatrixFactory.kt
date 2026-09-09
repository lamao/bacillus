package com.invenit.bacillus.model.matrix

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

internal class TestDefaultDecisionMatrixFactory {

    private val factory = DefaultDecisionMatrixFactory()

    @ParameterizedTest(name = "initial()[{0}] hunts and detours to the Produce checkpoint once low energy fires")
    @CsvSource(
        "0", "1", "8", "15",
    )
    fun testInitialHuntStateJumpsToProduceOnLowEnergy(index: Int) {
        val matrix = factory.initial()

        val result = matrix.evaluate(index, sensorValue = 0.1)   // below the low-energy threshold

        assertEquals(Action(Action.Category.Move, Action.Mode.TowardConsume), result.action)
        assertEquals(16, result.nextIndex)
    }

    @ParameterizedTest(name = "initial()[{0}] keeps hunting while energy holds")
    @CsvSource(
        "0, 1", "8, 9", "15, 16",
    )
    fun testInitialHuntStateAdvancesWhileEnergyHolds(index: Int, expectedNext: Int) {
        val matrix = factory.initial()

        val result = matrix.evaluate(index, sensorValue = 0.9)   // above the low-energy threshold

        assertEquals(Action(Action.Category.Move, Action.Mode.TowardConsume), result.action)
        assertEquals(expectedNext, result.nextIndex)
    }

    @ParameterizedTest(name = "initial()[16] releases waste and always advances into resting, sensorValue={0}")
    @CsvSource(
        "0.1", "0.9",
    )
    fun testProduceCheckpointAlwaysAdvancesToRest(sensorValue: Double) {
        val matrix = factory.initial()

        val result = matrix.evaluate(16, sensorValue)

        assertEquals(Action(Action.Category.Produce, Action.Mode.Release), result.action)
        assertEquals(17, result.nextIndex)
    }

    @ParameterizedTest(name = "initial()[{0}] rests and detours to the Split checkpoint once energy recovers")
    @CsvSource(
        "17", "19", "23",
    )
    fun testInitialRestStateJumpsToSplitOnRecoveredEnergy(index: Int) {
        val matrix = factory.initial()

        val result = matrix.evaluate(index, sensorValue = 0.9)   // above the recovered-energy threshold

        assertEquals(Action(Action.Category.Rest), result.action)
        assertEquals(24, result.nextIndex)
    }

    @ParameterizedTest(name = "initial()[{0}] keeps resting while energy stays low")
    @CsvSource(
        "17, 18", "19, 20", "23, 24",
    )
    fun testInitialRestStateAdvancesWhileEnergyStaysLow(index: Int, expectedNext: Int) {
        val matrix = factory.initial()

        val result = matrix.evaluate(index, sensorValue = 0.1)   // below the recovered-energy threshold

        assertEquals(Action(Action.Category.Rest), result.action)
        assertEquals(expectedNext, result.nextIndex)
    }

    @ParameterizedTest(name = "initial()[24] attempts a split and always wraps back to hunting, sensorValue={0}")
    @CsvSource(
        "0.1", "0.9",
    )
    fun testSplitCheckpointAlwaysWrapsToHunt(sensorValue: Double) {
        val matrix = factory.initial()

        val result = matrix.evaluate(24, sensorValue)

        assertEquals(Action(Action.Category.Split), result.action)
        assertEquals(0, result.nextIndex)
    }

}
