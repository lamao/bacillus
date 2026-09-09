package com.invenit.bacillus.model.matrix

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

internal class TestDefaultDecisionMatrixFactory {

    private val factory = DefaultDecisionMatrixFactory()

    @ParameterizedTest(name = "initial()[{0}] grows and detours to the Produce checkpoint once big enough")
    @CsvSource(
        "0", "1", "8", "22",
    )
    fun testInitialGrowStateJumpsToProduceWhenSplitReady(index: Int) {
        val matrix = factory.initial()

        val result = matrix.evaluate(index, sensorValue = 90)   // above the split-ready threshold

        assertEquals(Action(Action.Category.Rest), result.action)
        assertEquals(23, result.nextIndex)
    }

    @ParameterizedTest(name = "initial()[{0}] keeps growing while still below split-ready size")
    @CsvSource(
        "0, 1", "8, 9", "22, 23",
    )
    fun testInitialGrowStateAdvancesWhileBelowSplitReady(index: Int, expectedNext: Int) {
        val matrix = factory.initial()

        val result = matrix.evaluate(index, sensorValue = 10)   // below the split-ready threshold

        assertEquals(Action(Action.Category.Rest), result.action)
        assertEquals(expectedNext, result.nextIndex)
    }

    @ParameterizedTest(name = "initial()[23] releases waste and always advances into the Split checkpoint, sensorValue={0}")
    @CsvSource(
        "10", "90",
    )
    fun testProduceCheckpointAlwaysAdvancesToSplit(sensorValue: Int) {
        val matrix = factory.initial()

        val result = matrix.evaluate(23, sensorValue)

        assertEquals(Action(Action.Category.Produce, Action.Mode.Release), result.action)
        assertEquals(24, result.nextIndex)
    }

    @ParameterizedTest(name = "initial()[24] attempts a split and always wraps back to growing, sensorValue={0}")
    @CsvSource(
        "10", "90",
    )
    fun testSplitCheckpointAlwaysWrapsToGrow(sensorValue: Int) {
        val matrix = factory.initial()

        val result = matrix.evaluate(24, sensorValue)

        assertEquals(Action(Action.Category.Split), result.action)
        assertEquals(0, result.nextIndex)
    }

}
