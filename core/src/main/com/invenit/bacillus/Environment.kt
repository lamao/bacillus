package com.invenit.bacillus

import com.invenit.bacillus.model.Field
import com.invenit.bacillus.stage.*

/**
 * Created by viacheslav.mishcheriakov
 * Created 21.11.2021
 */
class Environment {

    private val stages = arrayOf(
        ClearExhaustedItemsStep(),
        CellLogicStep(
            listOf(BiteLogic(), MoveLogic()),
            CellDecisionApplierImpl()
        ),
        SplitStep(ServiceContext.randomService, ServiceContext.mutationService),
        AdjustCountersStep(),

        ToxinStep(),
        ConsumeStep(),
        CellLogicStep(
            listOf(PassiveConsumeLogic()),
            CellDecisionApplierImpl()
        ),
        ProduceStep(),
        CellLogicStep(
            listOf(LookUpLogic()),
            CellDecisionApplierImpl()
        )
    )

    fun doTic(field: Field) {
        for (stage in stages) {
            stage.execute(field)
        }
    }

}