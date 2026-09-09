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
        // Nudge Threshold (#1 §5) perturbs a threshold by a small step in
        // either direction — small enough that "flee at distance 2" drifts
        // toward "flee at distance 3" gradually rather than jumping.
        private const val THRESHOLD_NUDGE_RANGE = 0.1f
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

    override var dmMutationCount: Int = 0
        private set

    override var traitMutationCount: Int = 0
        private set

    override fun mutatedSize(size: Int): Int =
        size + MathUtils.random(-size / 4, size / 4)

    /**
     * Mutation (#1 §5, #12) is a two-stage roll: first, whether a mutation
     * happens at all this call, gated by [Settings.MutationRate] (unchanged
     * from the original trait-only mutation); second — only if it does —
     * whether it targets the Decision Matrix or a trait, weighted by
     * [Settings.DmMutationRatio].
     * @param dna the genome to (possibly) mutate
     * @return [dna] unchanged, or a copy with one DM state or one trait rerolled
     */
    override fun mutatedDna(dna: DNA): DNA {
        if (randomService.random() >= Settings.MutationRate) return dna

        return if (randomService.random() < Settings.DmMutationRatio) {
            dmMutationCount++
            dna.copy(decisionMatrix = mutatedDecisionMatrix(dna.decisionMatrix))
        } else {
            traitMutationCount++
            mutatedTrait(dna)
        }
    }

    private fun mutatedTrait(dna: DNA): DNA =
        when (DNA.Trait.entries[randomService.random(0, DNA.Trait.count() - 1)]) {
            DNA.Trait.Body -> dna.copy(body = randomBody())
            DNA.Trait.Consume -> dna.copy(consume = randomConsume())
            DNA.Trait.Produce -> dna.copy(produce = randomProduce())
            DNA.Trait.Toxin -> dna.copy(toxin = randomToxin())
        }

    /**
     * Picks one random state in [matrix] and rewrites it with one randomly
     * chosen [DmOperator].
     * @param matrix the Decision Matrix to mutate
     * @return a copy of [matrix] with exactly one state's instruction changed
     */
    private fun mutatedDecisionMatrix(matrix: DecisionMatrix): DecisionMatrix {
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
        if (category.modes.isEmpty()) return null
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
     * Nudge threshold (#1 §5): a small perturbation in either direction, so
     * a threshold drifts gradually instead of jumping to an unrelated
     * value. No bounds are applied (#1 §7 — unbounded by design).
     * @param threshold the current threshold
     * @return [threshold] shifted by a small random amount
     */
    private fun nudgedThreshold(threshold: Double): Double =
        threshold + randomService.random(-THRESHOLD_NUDGE_RANGE, THRESHOLD_NUDGE_RANGE)

    /**
     * Reroll jump offset (#1 §5): the true-branch offset becomes a fresh
     * random value, wrapped modulo the matrix size — an offset of 30 on a
     * 25-cell matrix becomes 5. The false branch (the implicit +1 advance)
     * is never touched; it isn't stored.
     * @return a random offset in `[0, DecisionMatrix.SIZE)`
     */
    private fun randomJumpOffset(): Int =
        Math.floorMod(randomService.random(-DecisionMatrix.SIZE, DecisionMatrix.SIZE * 2), DecisionMatrix.SIZE)

    override fun randomBody() = Substance.values()[randomService.random(1, Substance.values().size - 1)]
    override fun randomConsume() = Substance.values()[randomService.random(0, Substance.values().size - 1)]
    override fun randomProduce() = Substance.values()[randomService.random(1, Substance.values().size - 1)]
    override fun randomToxin() = Substance.values()[randomService.random(1, Substance.values().size - 1)]
}
