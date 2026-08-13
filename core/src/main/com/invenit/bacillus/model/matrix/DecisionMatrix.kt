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

    private fun wrap(index: Int): Int = Math.floorMod(index, SIZE)

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

        private const val HUNT_SIZE = 18
        private const val REST_START = HUNT_SIZE
        private const val LOW_ENERGY_THRESHOLD = 0.3
        private const val RECOVERED_ENERGY_THRESHOLD = 0.6

        /**
         * The starting population's genome (#1 §7, #8 — one hand-authored
         * preset, copied identically into every organic at spawn instead of
         * being randomized). Hunts toward food while energy holds up; once
         * energy runs low it jumps into resting, and jumps back to hunting
         * once energy recovers. The two thresholds leave a hysteresis gap so
         * an energy ratio hovering near one boundary doesn't flip the action
         * every tick. Absent either condition firing, the implicit +1
         * advance still cycles the ring through both clusters on its own, so
         * the genome is viable even before any sensor test ever triggers.
         */
        fun initial(): DecisionMatrix = DecisionMatrix(
            List(SIZE) { index ->
                if (index < REST_START) {
                    Instruction(
                        action = Action(Action.Category.Move, Action.Mode.TowardConsume),
                        sensor = Sensor.EnergyRatio,
                        comparator = Comparator.LessThan,
                        threshold = LOW_ENERGY_THRESHOLD,
                        jumpOffset = Math.floorMod(REST_START - index, SIZE)
                    )
                } else {
                    Instruction(
                        action = Action(Action.Category.Rest),
                        sensor = Sensor.EnergyRatio,
                        comparator = Comparator.GreaterThanOrEqual,
                        threshold = RECOVERED_ENERGY_THRESHOLD,
                        jumpOffset = Math.floorMod(-index, SIZE)
                    )
                }
            }
        )
    }
}