package com.invenit.bacillus.service

import com.invenit.bacillus.model.DNA
import com.invenit.bacillus.model.Substance
import com.invenit.bacillus.model.matrix.Action
import com.invenit.bacillus.model.matrix.Comparator
import com.invenit.bacillus.model.matrix.DecisionMatrix
import com.invenit.bacillus.model.matrix.Instruction
import com.invenit.bacillus.model.matrix.Sensor
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Created by viacheslav.mishcheriakov
 * Created 08.12.2021
 *
 * Covers the two-stage mutation roll (#12): whether a mutation happens at
 * all (Settings.MutationRate, unchanged from the original trait-only
 * mutation), then whether it targets the Decision Matrix or a trait
 * (Settings.DmMutationRatio). DM tests rely on the second `random()` call
 * being below the default 0.8 ratio; trait tests need it above.
 *
 * Each mutation also bumps DNA.dmMutationCount/traitMutationCount - a
 * per-lineage "distance from the founder genome" carried on the genome
 * itself, not tracked globally by the service.
 */
@ExtendWith(MockitoExtension::class)
class TestMutationServiceImpl {

    @Mock
    private lateinit var mockRandomService: RandomService

    private lateinit var mutationService: MutationService

    private val filler = Instruction(
        action = Action(Action.Category.Rest),
        sensor = Sensor.EnergyRatio,
        comparator = Comparator.GreaterThanOrEqual,
        threshold = 0.0,
        jumpOffset = 0
    )

    @BeforeTest
    fun before() {
        mutationService = MutationServiceImpl(mockRandomService)
    }

    private fun matrixWith(index: Int, instruction: Instruction): DecisionMatrix {
        val instructions = MutableList(DecisionMatrix.SIZE) { filler }
        instructions[index] = instruction
        return DecisionMatrix(instructions)
    }

    @Test
    fun testNoMutation() {
        val original = DNA(
            body = Substance.Blue,
            consume = Substance.Green,
            produce = Substance.Yellow,
            toxin = Substance.White
        )

        whenever(mockRandomService.random()).thenReturn(1.0f)

        val mutated = mutationService.mutatedDna(original)

        assertEquals(original, mutated)
        assertEquals(0, mutated.dmMutationCount)
        assertEquals(0, mutated.traitMutationCount)
    }

    @Test
    fun testMutatedDnaBody() {
        val original = DNA(
            body = Substance.Blue,
            consume = Substance.Sun,
            produce = Substance.Green,
            toxin = Substance.White
        )

        whenever(mockRandomService.random()).thenReturn(0.0f, 1.0f)
        whenever(mockRandomService.random(0, DNA.Trait.count() - 1)).thenReturn(0)
        whenever(mockRandomService.random(1, Substance.values().size - 1))
            .thenReturn(Substance.Red.ordinal)

        val mutated = mutationService.mutatedDna(original)

        assertEquals(original.copy(body = Substance.Red, traitMutationCount = 1), mutated)
        assertEquals(0, mutated.dmMutationCount)
    }

    @Test
    fun testMutatedDnaConsume() {
        val original = DNA(
            body = Substance.Blue,
            consume = Substance.Blue,
            produce = Substance.Green,
            toxin = Substance.White
        )
        whenever(mockRandomService.random()).thenReturn(0.0f, 1.0f)
        whenever(mockRandomService.random(0, DNA.Trait.count() - 1)).thenReturn(1)
        whenever(mockRandomService.random(0, Substance.values().size - 1))
            .thenReturn(Substance.Sun.ordinal)

        val mutated = mutationService.mutatedDna(original)

        assertEquals(original.copy(consume = Substance.Sun, traitMutationCount = 1), mutated)
    }

    @Test
    fun testMutatedDnaProduce() {
        val original = DNA(
            body = Substance.Blue,
            consume = Substance.Green,
            produce = Substance.Blue,
            toxin = Substance.White
        )
        whenever(mockRandomService.random()).thenReturn(0.0f, 1.0f)
        whenever(mockRandomService.random(0, DNA.Trait.count() - 1)).thenReturn(2)
        whenever(mockRandomService.random(1, Substance.values().size - 1))
            .thenReturn(Substance.Red.ordinal)

        val mutated = mutationService.mutatedDna(original)

        assertEquals(original.copy(produce = Substance.Red, traitMutationCount = 1), mutated)
    }

    @Test
    fun testMutatedDnaToxin() {
        val original = DNA(
            body = Substance.Blue,
            consume = Substance.Green,
            produce = Substance.Yellow,
            toxin = Substance.Blue
        )
        whenever(mockRandomService.random()).thenReturn(0.0f, 1.0f)
        whenever(mockRandomService.random(0, DNA.Trait.count() - 1)).thenReturn(3)
        whenever(mockRandomService.random(1, Substance.values().size - 1))
            .thenReturn(Substance.White.ordinal)

        val mutated = mutationService.mutatedDna(original)

        assertEquals(original.copy(toxin = Substance.White, traitMutationCount = 1), mutated)
    }

    @Test
    fun testMutatedSizeStaysWithinAQuarterOfOriginal() {
        val originalSize = 1000

        val mutatedSize = mutationService.mutatedSize(originalSize)

        assertTrue(
            mutatedSize in (originalSize - originalSize / 4)..(originalSize + originalSize / 4),
            "Expected $mutatedSize to be within a quarter of $originalSize"
        )
    }

    @Test
    fun testDmMutationRerollAction() {
        val original = Instruction(
            action = Action(Action.Category.Rest),
            sensor = Sensor.EnergyRatio,
            comparator = Comparator.GreaterThanOrEqual,
            threshold = 0.5,
            jumpOffset = 3
        )
        val dna = DNA(Substance.Blue, Substance.Green, Substance.Yellow, Substance.White, matrixWith(5, original))

        whenever(mockRandomService.random()).thenReturn(0.0f)   // mutate, DM-selected
        whenever(mockRandomService.random(0, DecisionMatrix.SIZE - 1)).thenReturn(5)
        whenever(mockRandomService.random(0, 4)).thenReturn(0)  // DmOperator.RerollAction
        // Produce (index 2, 2 modes) rather than Move (5 modes) - Move's mode
        // range (0,4) would collide with the operator pick's (0,4) above and
        // silently override this stub.
        whenever(mockRandomService.random(0, Action.Category.entries.size - 1)).thenReturn(2)  // Produce
        whenever(mockRandomService.random(0, Action.Category.Produce.modes.size - 1)).thenReturn(0)  // Release

        val mutated = mutationService.mutatedDna(dna)

        val expected = original.copy(action = Action(Action.Category.Produce, Action.Mode.Release))
        assertEquals(expected, mutated.decisionMatrix[5])
        assertEquals(1, mutated.dmMutationCount)
        assertEquals(0, mutated.traitMutationCount)
    }

    @Test
    fun testDmMutationRerollModeKeepsCategory() {
        val original = Instruction(
            action = Action(Action.Category.Produce, Action.Mode.Release),
            sensor = Sensor.EnergyRatio,
            comparator = Comparator.GreaterThanOrEqual,
            threshold = 0.5,
            jumpOffset = 3
        )
        val dna = DNA(Substance.Blue, Substance.Green, Substance.Yellow, Substance.White, matrixWith(5, original))

        whenever(mockRandomService.random()).thenReturn(0.0f)
        whenever(mockRandomService.random(0, DecisionMatrix.SIZE - 1)).thenReturn(5)
        whenever(mockRandomService.random(0, 4)).thenReturn(1)  // DmOperator.RerollMode
        whenever(mockRandomService.random(0, Action.Category.Produce.modes.size - 1)).thenReturn(1)  // Retain

        val mutated = mutationService.mutatedDna(dna)

        val expected = original.copy(action = Action(Action.Category.Produce, Action.Mode.Retain))
        assertEquals(expected, mutated.decisionMatrix[5])
        assertEquals(1, mutated.dmMutationCount)
    }

    @Test
    fun testDmMutationRerollModeOnCategoryWithoutModesIsNoOp() {
        val original = Instruction(
            action = Action(Action.Category.Rest),
            sensor = Sensor.EnergyRatio,
            comparator = Comparator.GreaterThanOrEqual,
            threshold = 0.5,
            jumpOffset = 3
        )
        val dna = DNA(Substance.Blue, Substance.Green, Substance.Yellow, Substance.White, matrixWith(5, original))

        whenever(mockRandomService.random()).thenReturn(0.0f)
        whenever(mockRandomService.random(0, DecisionMatrix.SIZE - 1)).thenReturn(5)
        whenever(mockRandomService.random(0, 4)).thenReturn(1)  // DmOperator.RerollMode

        val mutated = mutationService.mutatedDna(dna)

        // Rest has no modes to reroll -> the instruction is unchanged, but the
        // mutation event still counts toward the genome's distance.
        assertEquals(original, mutated.decisionMatrix[5])
        assertEquals(1, mutated.dmMutationCount)
    }

    @Test
    fun testDmMutationRerollSensor() {
        val original = Instruction(
            action = Action(Action.Category.Rest),
            sensor = Sensor.EnergyRatio,
            comparator = Comparator.GreaterThanOrEqual,
            threshold = 0.5,
            jumpOffset = 3
        )
        val dna = DNA(Substance.Blue, Substance.Green, Substance.Yellow, Substance.White, matrixWith(5, original))

        whenever(mockRandomService.random()).thenReturn(0.0f)
        whenever(mockRandomService.random(0, DecisionMatrix.SIZE - 1)).thenReturn(5)
        whenever(mockRandomService.random(0, 4)).thenReturn(2)  // DmOperator.RerollSensor
        whenever(mockRandomService.random(0, Sensor.entries.size - 1)).thenReturn(Sensor.Crowding.ordinal)

        val mutated = mutationService.mutatedDna(dna)

        assertEquals(original.copy(sensor = Sensor.Crowding), mutated.decisionMatrix[5])
    }

    @Test
    fun testDmMutationNudgeThreshold() {
        val original = Instruction(
            action = Action(Action.Category.Rest),
            sensor = Sensor.EnergyRatio,
            comparator = Comparator.GreaterThanOrEqual,
            threshold = 0.5,
            jumpOffset = 3
        )
        val dna = DNA(Substance.Blue, Substance.Green, Substance.Yellow, Substance.White, matrixWith(5, original))

        whenever(mockRandomService.random()).thenReturn(0.0f)
        whenever(mockRandomService.random(0, DecisionMatrix.SIZE - 1)).thenReturn(5)
        whenever(mockRandomService.random(0, 4)).thenReturn(3)  // DmOperator.NudgeThreshold
        whenever(mockRandomService.random(-0.1f, 0.1f)).thenReturn(0.05f)

        val mutated = mutationService.mutatedDna(dna)

        // Computed the same way the implementation computes it, so both
        // sides go through identical Double+Float widening - no float/double
        // rounding mismatch between expected and actual.
        val expectedThreshold = 0.5 + 0.05f
        assertEquals(original.copy(threshold = expectedThreshold), mutated.decisionMatrix[5])
    }

    @ParameterizedTest(name = "reroll jump offset: raw {0} on a 25-cell matrix wraps to {1}")
    @CsvSource(
        "30, 5",    // issue #1 §5's own example: overflow wraps forward
        "-1, 24",   // negative offset wraps backward past the start
    )
    fun testDmMutationRerollJumpOffsetWraps(rawOffset: Int, expectedOffset: Int) {
        val original = Instruction(
            action = Action(Action.Category.Rest),
            sensor = Sensor.EnergyRatio,
            comparator = Comparator.GreaterThanOrEqual,
            threshold = 0.5,
            jumpOffset = 3
        )
        val dna = DNA(Substance.Blue, Substance.Green, Substance.Yellow, Substance.White, matrixWith(5, original))

        whenever(mockRandomService.random()).thenReturn(0.0f)
        whenever(mockRandomService.random(0, DecisionMatrix.SIZE - 1)).thenReturn(5)
        whenever(mockRandomService.random(0, 4)).thenReturn(4)  // DmOperator.RerollJumpOffset
        whenever(mockRandomService.random(-DecisionMatrix.SIZE, DecisionMatrix.SIZE * 2)).thenReturn(rawOffset)

        val mutated = mutationService.mutatedDna(dna)

        assertEquals(original.copy(jumpOffset = expectedOffset), mutated.decisionMatrix[5])
    }

    @Test
    fun testMutationCountsAccumulatePerLineageAcrossGenerations() {
        // A genome that already carries 2 DM mutations and 1 trait mutation
        // from earlier generations - its distance from the founder genome
        // so far.
        val parentDna = DNA(
            Substance.Blue, Substance.Green, Substance.Yellow, Substance.White,
            matrixWith(5, filler),
            dmMutationCount = 2,
            traitMutationCount = 1
        )

        whenever(mockRandomService.random()).thenReturn(0.0f)
        whenever(mockRandomService.random(0, DecisionMatrix.SIZE - 1)).thenReturn(5)
        whenever(mockRandomService.random(0, 4)).thenReturn(2)  // DmOperator.RerollSensor
        whenever(mockRandomService.random(0, Sensor.entries.size - 1)).thenReturn(Sensor.Age.ordinal)

        val offspringDna = mutationService.mutatedDna(parentDna)

        // One more DM mutation lands on top of what the parent already
        // carried - the trait count is inherited unchanged.
        assertEquals(3, offspringDna.dmMutationCount)
        assertEquals(1, offspringDna.traitMutationCount)
    }
}
