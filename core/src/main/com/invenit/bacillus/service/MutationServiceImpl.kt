package com.invenit.bacillus.service

import com.badlogic.gdx.math.MathUtils
import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.DNA
import com.invenit.bacillus.model.Substance
import com.invenit.bacillus.model.matrix.Action
import com.invenit.bacillus.model.matrix.DecisionMatrix
import com.invenit.bacillus.model.matrix.Instruction
import com.invenit.bacillus.model.matrix.Sensor

/**
 * Created by viacheslav.mishcheriakov
 * Created 08.12.2021
 */
class MutationServiceImpl(
    private val randomService: RandomService
) : MutationService {

    companion object {
        private const val THRESHOLD_NUDGE_RANGE_PERCENTS = 10
        private const val MINIMAL_THRESHOLD_NUDGE_RANGE = 1
    }

    /**
     * The five Decision Matrix mutation operators from #1 §5. Each rewrites
     * exactly one field of one randomly chosen [Instruction].
     */
    private enum class DmOperator {
        RerollAction,
        RerollMode,
        RerollSensor,
        NudgeThreshold,
        RerollJumpOffset
    }

    override fun mutatedSize(size: Int): Int =
        size + MathUtils.random(-size / 4, size / 4)

    /**
     * Mutation (#1 §5, #12) is a two-stage roll: first, whether a mutation
     * happens at all this call, gated by [Settings.MutationRate] (unchanged
     * from the original trait-only mutation); second — only if it does —
     * whether it targets the Decision Matrix or a trait, weighted by
     * [Settings.DmMutationRatio]. Whichever fires also bumps that half of
     * [dna]'s own per-lineage mutation count (#12) — [DNA.dmMutationCount]
     * or [DNA.traitMutationCount], this genome's "distance" from the
     * founder genome, carried on the genome itself rather than tracked
     * globally so every cell can be inspected individually.
     * @param dna the genome to (possibly) mutate
     * @return [dna] unchanged, or a copy with one DM state or one trait
     * rerolled and the matching mutation count incremented
     */
    override fun mutatedDna(dna: DNA): DNA {
        if (randomService.random() >= Settings.MutationRate) {
            return dna
        }

        if (randomService.random() < Settings.DmMutationRatio) {
            return dna.copy(
                decisionMatrix = mutatedDecisionMatrix(dna.decisionMatrix),
                dmMutationCount = dna.dmMutationCount + 1
            )
        }

        return mutatedTrait(dna)
    }

    private fun mutatedTrait(dna: DNA): DNA {
        val mutated = when (DNA.Trait.entries[randomService.random(0, DNA.Trait.count() - 1)]) {
            DNA.Trait.Body -> dna.copy(body = randomBody())
            DNA.Trait.Consume -> dna.copy(consume = randomConsume())
            DNA.Trait.Produce -> dna.copy(produce = randomProduce())
            DNA.Trait.Toxin -> dna.copy(toxin = randomToxin())
        }
        return mutated.copy(traitMutationCount = dna.traitMutationCount + 1)
    }

    /**
     * Picks one random state in [matrix] and rewrites it with one randomly
     * chosen [DmOperator].
     * @param matrix the Decision Matrix to mutate
     * @return a copy of [matrix] with exactly one state's instruction changed
     */
    override fun mutatedDecisionMatrix(matrix: DecisionMatrix): DecisionMatrix {
        val index = randomService.random(0, DecisionMatrix.SIZE - 1)
        val instruction = matrix[index]

        val mutated = when (DmOperator.entries[randomService.random(0, DmOperator.entries.size - 1)]) {
            DmOperator.RerollAction -> instruction.copy(action = randomAction())
            DmOperator.RerollMode -> instruction.copy(action = rerolledMode(instruction.action))
            DmOperator.RerollSensor -> instruction.copy(sensor = randomSensor())
            DmOperator.NudgeThreshold -> instruction.copy(threshold = nudgedThreshold(instruction.threshold))
            DmOperator.RerollJumpOffset -> instruction.copy(jumpOffset = randomJumpOffset())
        }

        return matrix.withInstruction(index, mutated)
    }

    /**
     * Reroll action (#1 §5): swaps a state's action for a fresh random one,
     * picked independently of its current category or mode.
     * @return a random, structurally valid [Action]
     */
    private fun randomAction(): Action {
        val category = Action.Category.entries[randomService.random(0, Action.Category.entries.size - 1)]
        return Action(category, randomModeFor(category))
    }

    /**
     * Reroll mode (#1 §5): keeps [action]'s category and picks a fresh
     * random mode for it. A no-op for Rest/Split, whose category has no
     * modes to reroll.
     * @param action the action whose mode is rerolled
     * @return [action] with a new mode, or [action] unchanged if its category has none
     */
    private fun rerolledMode(action: Action): Action {
        val mode = randomModeFor(action.category) ?: return action
        return action.copy(mode = mode)
    }

    private fun randomModeFor(category: Action.Category): Action.Mode? {
        if (category.modes.isEmpty()) {
            return null
        }
        val modes = category.modes.toList()
        return modes[randomService.random(0, modes.size - 1)]
    }

    /**
     * Reroll sensor (#1 §5): the state's one test starts reading a
     * different sensor.
     * @return a random [Sensor]
     */
    private fun randomSensor(): Sensor = Sensor.entries[randomService.random(0, Sensor.entries.size - 1)]

    /**
     * Nudge threshold (#1 §5): a fresh value drawn from a small window
     * around the current threshold, so it drifts gradually instead of
     * jumping to an unrelated value. The window's half-width is a
     * percentage of the current threshold, floored at
     * [MINIMAL_THRESHOLD_NUDGE_RANGE] so a small threshold still has
     * somewhere to move. Every sensor now reads a non-negative percentage
     * or count (#12), so the window's low end is floored at 0 - the one
     * bound #1 §7 didn't anticipate back when thresholds were unbounded
     * doubles.
     * @param threshold the current threshold
     * @return a value drawn from `[threshold - offsetRange, threshold + offsetRange]`,
     * clamped to never go negative
     */
    private fun nudgedThreshold(threshold: Int): Int {
        val offsetRange = (threshold * THRESHOLD_NUDGE_RANGE_PERCENTS / 100)
            .coerceAtLeast(MINIMAL_THRESHOLD_NUDGE_RANGE)
        val rangeStart = (threshold - offsetRange).coerceAtLeast(0)
        val rangeEnd = threshold + offsetRange

        return randomService.random(rangeStart, rangeEnd)
    }

    private fun randomJumpOffset(): Int = randomService.random(0, DecisionMatrix.SIZE - 1)

    override fun randomBody() = Substance.entries[randomService.random(1, Substance.entries.size - 1)]
    override fun randomConsume() = Substance.entries[randomService.random(0, Substance.entries.size - 1)]
    override fun randomProduce() = Substance.entries[randomService.random(1, Substance.entries.size - 1)]
    override fun randomToxin() = Substance.entries[randomService.random(1, Substance.entries.size - 1)]
}
