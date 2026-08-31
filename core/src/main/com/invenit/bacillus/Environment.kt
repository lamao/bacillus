package com.invenit.bacillus

import com.invenit.bacillus.model.Field
import com.invenit.bacillus.stage.*

/**
 * Created by viacheslav.mishcheriakov
 * Created 21.11.2021
 */
class Environment {

    private val steps = arrayOf(
        // Conscious — logic a cell's decisions drive. DecideStep (#1 §6)
        // reads each cell's Decision Matrix and stamps a chosen action for
        // this tick; MoveStep is gated by it. SplitStep still runs
        // unconditionally on its energy threshold — Split isn't in the
        // minimal action set yet (#1 task 6), so it isn't reachable from the
        // matrix either way.
        // TODO: Correct moving animation
        DecideStep(ServiceContext.randomService),
        MoveStep(),
        SplitStep(ServiceContext.randomService, ServiceContext.mutationService),

        // Digestion (background/passive) — tied to a cell, but not a decision it
        // makes. Runs automatically whenever the chosen action isn't Move;
        // see ConsumeStep for the trigger condition.
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