package com.invenit.bacillus.model.matrix

/**
 * Builds hand-authored [DecisionMatrix] presets (#1 §7, #8).
 */
class DefaultDecisionMatrixFactory : DecisionMatrixFactory {

    companion object {
        private const val GROW_SIZE = 23
        private const val PRODUCE_START = GROW_SIZE               // 23 — Produce is a single checkpoint state
        private const val SPLIT_START = DecisionMatrix.SIZE - 1   // 24 — Split is a single checkpoint state
        private const val SPLIT_READY_THRESHOLD_PERCENTS = 85
    }

    /**
     * The starting population's genome (#1 §7, #8 — one hand-authored
     * preset, copied identically into every organic at spawn instead of
     * being randomized). Grows a pure digester ("plant" on #1 §3's
     * hunter/digester spectrum): the starting DNA's `consume` is
     * [com.invenit.bacillus.model.Substance.Sun] (see `CreatureFactoryImpl`),
     * and Sun is never a physical field entity, so `Move(TowardConsume)`
     * could never find one to walk toward — every Move tick would just be a
     * random step that pays `MoveConsumption` for zero chance of a gain,
     * while also disabling the ambient Sun income `ConsumeStep` grants on
     * any tick the chosen action isn't Move. A genome for this DNA is
     * strictly better off never moving at all, so this one doesn't: it
     * rests (and thus digests) every tick while it grows, detours through a
     * Produce(Release) checkpoint once it's close to reproduction size —
     * dropping off accumulated waste — and then through a Split checkpoint
     * right after, attempting to reproduce subject to SplitStep's own
     * energy threshold, before resuming growth. The jump target always
     * lands exactly on the Produce checkpoint regardless of which growing
     * state triggers it, so every full lap of the ring passes through both
     * checkpoints at least once — neither is a dead branch reachable only
     * if the DM ever mutates. Absent the size condition ever firing, the
     * implicit +1 advance still cycles the ring through every state on its
     * own, so the genome is viable even before any sensor test triggers.
     */
    override fun initial(): DecisionMatrix = DecisionMatrix(
        List(DecisionMatrix.SIZE) { index ->
            when {
                index < PRODUCE_START -> Instruction(
                    action = Action(Action.Category.Rest),
                    sensor = Sensor.SizeRatio,
                    comparator = Comparator.GreaterThanOrEqual,
                    threshold = SPLIT_READY_THRESHOLD_PERCENTS,
                    jumpOffset = Math.floorMod(PRODUCE_START - index, DecisionMatrix.SIZE)
                )

                index == PRODUCE_START -> checkpoint(Action(Action.Category.Produce, Action.Mode.Release))

                else -> checkpoint(Action(Action.Category.Split))   // index == SPLIT_START
            }
        }
    )

    /**
     * A single-state waypoint the ring always passes through once per lap.
     * Its test is a placeholder, not a real condition: the true and false
     * branches both land on the very next index (jumpOffset 1 matches the
     * implicit advance), so which one fires doesn't matter — that's what
     * makes Produce/Split unconditionally reachable rather than dependent
     * on a sensor value ever landing a particular way.
     * @param action the action to stamp on this waypoint state
     * @return an [Instruction] that always advances to the next index
     */
    private fun checkpoint(action: Action) = Instruction(
        action = action,
        sensor = Sensor.EnergyRatio,
        comparator = Comparator.GreaterThanOrEqual,
        threshold = 0,
        jumpOffset = 1
    )
}
