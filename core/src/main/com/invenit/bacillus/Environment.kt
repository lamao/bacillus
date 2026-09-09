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
        // this tick; MoveStep and SplitStep are gated by it (#1 task 6).
        // TODO: Correct moving animation
        DecideStep(ServiceContext.randomService),
        MoveStep(),
        SplitStep(ServiceContext.randomService, ServiceContext.mutationService),

        // Digestion (background/passive) — tied to a cell, but not a decision it
        // makes. Runs automatically whenever the chosen action isn't Move;
        // see ConsumeStep for the trigger condition.
        ConsumeStep(),

        // ProduceStep is conceptually Conscious (see above) — gated by the
        // chosen action being Produce(Release) (#1 task 6) — but stays right
        // after ConsumeStep so waste accumulated this tick is expelled this
        // same tick.
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