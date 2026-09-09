package com.invenit.bacillus.model.matrix

/**
 * Builds hand-authored [DecisionMatrix] presets (#1 §7, #8).
 */
class DefaultDecisionMatrixFactory : DecisionMatrixFactory {

    companion object {
        private const val HUNT_SIZE = 16
        private const val PRODUCE_START = HUNT_SIZE               // 16
        private const val REST_START = PRODUCE_START + 1          // 17 — Produce is a single checkpoint state
        private const val SPLIT_START = DecisionMatrix.SIZE - 1   // 24 — Split is a single checkpoint state
        private const val LOW_ENERGY_THRESHOLD = 0.3
        private const val RECOVERED_ENERGY_THRESHOLD = 0.6
    }

    /**
     * The starting population's genome (#1 §7, #8 — one hand-authored
     * preset, copied identically into every organic at spawn instead of
     * being randomized). Hunts toward food while energy holds up; once
     * energy runs low it detours through a Produce(Release) checkpoint —
     * dropping off any accumulated waste — before resting, and once rested
     * enough it detours through a Split checkpoint — attempting to
     * reproduce, subject to SplitStep's own energy threshold — before
     * resuming the hunt. The hunt/rest thresholds leave a hysteresis gap so
     * an energy ratio hovering near one boundary doesn't flip the action
     * every tick. Both jump targets always land exactly on their checkpoint
     * regardless of which hunt/rest state triggers them, so every full lap
     * of the ring passes through Produce and Split at least once — neither
     * is a dead branch reachable only if the DM ever mutates. Absent either
     * hysteresis condition firing, the implicit +1 advance still cycles the
     * ring through every state on its own, so the genome is viable even
     * before any sensor test ever triggers.
     */
    override fun initial(): DecisionMatrix = DecisionMatrix(
        List(DecisionMatrix.SIZE) { index ->
            when {
                index < PRODUCE_START -> Instruction(
                    action = Action(Action.Category.Move, Action.Mode.TowardConsume),
                    sensor = Sensor.EnergyRatio,
                    comparator = Comparator.LessThan,
                    threshold = LOW_ENERGY_THRESHOLD,
                    jumpOffset = Math.floorMod(PRODUCE_START - index, DecisionMatrix.SIZE)
                )

                index == PRODUCE_START -> checkpoint(Action(Action.Category.Produce, Action.Mode.Release))

                index < SPLIT_START -> Instruction(
                    action = Action(Action.Category.Rest),
                    sensor = Sensor.EnergyRatio,
                    comparator = Comparator.GreaterThanOrEqual,
                    threshold = RECOVERED_ENERGY_THRESHOLD,
                    jumpOffset = Math.floorMod(SPLIT_START - index, DecisionMatrix.SIZE)
                )

                else -> checkpoint(Action(Action.Category.Split))
            }
        }
    )

    // A single-state waypoint the ring always passes through once per lap.
    // Its test is a placeholder, not a real hysteresis condition: the true
    // and false branches both land on the very next index (jumpOffset 1
    // matches the implicit advance), so which one fires doesn't matter —
    // that's what makes Produce/Split unconditionally reachable rather
    // than dependent on a sensor value ever landing a particular way.
    private fun checkpoint(action: Action) = Instruction(
        action = action,
        sensor = Sensor.EnergyRatio,
        comparator = Comparator.GreaterThanOrEqual,
        threshold = 0.0,
        jumpOffset = 1
    )
}
