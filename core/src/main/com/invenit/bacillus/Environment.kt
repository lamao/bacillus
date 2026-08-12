package com.invenit.bacillus

import com.invenit.bacillus.model.Field
import com.invenit.bacillus.stage.*

/**
 * Created by viacheslav.mishcheriakov
 * Created 21.11.2021
 */
class Environment {

    private val steps = arrayOf(
        // Conscious — logic a cell's decisions drive. For now these run
        // unconditionally, still gated only by dna.canMove / energy thresholds.
        // A future DecideStep (#1 §6) will gate them by a chosen action instead.
        // TODO: Correct moving animation
        LookUpStep(ServiceContext.randomService),
        MoveStep(),
        SplitStep(ServiceContext.randomService, ServiceContext.mutationService),

        // Digestion (background/passive) — tied to a cell, but not a decision it
        // makes. Runs automatically every tick; see ConsumeStep for the swappable
        // trigger condition.
        ConsumeStep(),

        // ProduceStep is conceptually Conscious (see above), but stays right after
        // ConsumeStep so waste accumulated this tick is expelled this same tick.
        ProduceStep(),

        // Environmental — logic no cell can influence.
        ToxinStep(),
        ExhaustStep(),
        ClearExhaustedEntitiesStep(ServiceContext.randomService),
    )

    fun doTic(field: Field) {
        for (step in steps) {
            step.execute(field)
        }
    }

}