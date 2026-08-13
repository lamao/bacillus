package com.invenit.bacillus.model.matrix

/**
 * Builds hand-authored [DecisionMatrix] presets (#1 §7, #8), as opposed to
 * [DecisionMatrix.default]'s inert placeholder.
 */
object DecisionMatrixFactory {

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
        List(DecisionMatrix.SIZE) { index ->
            if (index < REST_START) {
                Instruction(
                    action = Action(Action.Category.Move, Action.Mode.TowardConsume),
                    sensor = Sensor.EnergyRatio,
                    comparator = Comparator.LessThan,
                    threshold = LOW_ENERGY_THRESHOLD,
                    jumpOffset = Math.floorMod(REST_START - index, DecisionMatrix.SIZE)
                )
            } else {
                Instruction(
                    action = Action(Action.Category.Rest),
                    sensor = Sensor.EnergyRatio,
                    comparator = Comparator.GreaterThanOrEqual,
                    threshold = RECOVERED_ENERGY_THRESHOLD,
                    jumpOffset = Math.floorMod(-index, DecisionMatrix.SIZE)
                )
            }
        }
    )
}
