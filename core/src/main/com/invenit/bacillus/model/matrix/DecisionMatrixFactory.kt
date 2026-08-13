package com.invenit.bacillus.model.matrix

/**
 *
 * @author viacheslav.mishcheriakov
 * Created 13.08.2026
 */
interface DecisionMatrixFactory {
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
    fun initial(): DecisionMatrix
}