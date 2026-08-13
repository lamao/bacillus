package com.invenit.bacillus.model.matrix

/**
 * Instruction DNA #7 — draft, unintegrated (see issue #1 §2, #7).
 * The circular grid of [Instruction]s a genome runs through. Fixed at 5x5 (25
 * states) for v1 (#1 §7). Index arithmetic wraps modulo [SIZE] in both
 * directions, so the ring has no start or end.
 */
data class DecisionMatrix(private val instructions: List<Instruction>) {

    init {
        require(instructions.size == SIZE) {
            "DecisionMatrix must contain exactly $SIZE instructions, got ${instructions.size}"
        }
    }

    operator fun get(index: Int): Instruction = instructions[wrap(index)]

    /** Index reached by the implicit advance taken when a state's test is false. */
    fun next(index: Int): Int = wrap(index + 1)

    /** Index reached by the mutable jump taken when a state's test is true. */
    fun jump(index: Int, offset: Int): Int = wrap(index + offset)

    /**
     * Acts then tests: reads the [Instruction] at [index], and evaluates its
     * test against [sensorValue] (an already-sensed reading, since sensing a
     * live [com.invenit.bacillus.model.Organic] is #1 task 5's scope, not this draft's) to decide the
     * next index.
     */
    fun evaluate(index: Int, sensorValue: Double): StateResult {
        val instruction = this[index]
        val conditionMet = instruction.comparator.test(sensorValue, instruction.threshold)
        val nextIndex = if (conditionMet) jump(index, instruction.jumpOffset) else next(index)
        return StateResult(instruction.action, nextIndex)
    }

    private fun wrap(index: Int): Int = ((index % SIZE) + SIZE) % SIZE

    companion object {
        const val DIMENSION = 5
        const val SIZE = DIMENSION * DIMENSION

        /** An inert placeholder matrix (rests forever) so [com.invenit.bacillus.model.DNA] stays constructible before a real genome exists. */
        fun default(): DecisionMatrix = DecisionMatrix(
            List(SIZE) {
                Instruction(
                    action = Action(Action.Category.Rest),
                    sensor = Sensor.EnergyRatio,
                    comparator = Comparator.GreaterThanOrEqual,
                    threshold = 0.0,
                    jumpOffset = 0
                )
            }
        )
    }
}