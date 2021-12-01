package com.invenit.bacillus.stage

import com.badlogic.gdx.math.MathUtils
import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.Field
import com.invenit.bacillus.model.Mineral

/**
 * Created by vyacheslav.mischeryakov
 * Created 21.11.2021
 */
class ClearExhaustedItemsStage : Stage {
    override fun execute(field: Field) {
        clearOrganics(field)
        clearMinerals(field)
    }

    private fun clearOrganics(field: Field) {
        field.organics
            .filter { it.energy <= 0 || it.age >= Settings.MaxAge || MathUtils.random() < Settings.UnexpectedDeathRate }
            .forEach {
                field.remove(it.position)
                if (it.size > 0) {
                    val corps = Mineral(
                        it.position,
                        it.size,
                        it.body
                    )
                    field.add(corps)
                }
            }
    }

    private fun clearMinerals(field: Field) {
        field.minerals
            .filter { it.size <= 0 }
            .forEach { field.remove(it.position) }
    }
}