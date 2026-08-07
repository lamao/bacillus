package com.invenit.bacillus

import com.invenit.bacillus.model.Field
import com.invenit.bacillus.stage.*

/**
 * Created by viacheslav.mishcheriakov
 * Created 21.11.2021
 */
class Environment {

    private val steps = arrayOf(
        // bacilli active steps
        // TODO: Correct moving animation
        LookUpStep(ServiceContext.randomService),
        MoveStep(),
        SplitStep(ServiceContext.randomService, ServiceContext.mutationService),

        // bacilli passive steps
        ConsumeStep(),
        ProduceStep(),
        ToxinStep(),

        // environmental steps
        ExhaustStep(),
        ClearExhaustedEntitiesStep(ServiceContext.randomService),
    )

    fun doTic(field: Field) {
        for (step in steps) {
            step.execute(field)
        }
    }

}