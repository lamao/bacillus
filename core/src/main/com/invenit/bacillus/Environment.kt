package com.invenit.bacillus

import com.invenit.bacillus.model.Field
import com.invenit.bacillus.stage.*

/**
 * Created by vyacheslav.mischeryakov
 * Created 21.11.2021
 */
class Environment {

    private val stages = arrayOf(
        ClearExhaustedItems(),
        MoveStage(),
        SplitStage(),
        AdjustCountersStage(),
        ConsumeStage(),
        ProduceStage(),
        LookUpStage()
    )

    fun doTic(field: Field) {
        for (stage in stages) {
            stage.execute(field)
        }
    }

}