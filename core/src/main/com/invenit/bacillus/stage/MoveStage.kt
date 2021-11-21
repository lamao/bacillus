package com.invenit.bacillus.stage

import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.Field
import com.invenit.bacillus.model.Organic

/**
 * Created by vyacheslav.mischeryakov
 * Created 21.11.2021
 */
class MoveStage : Stage {
    override fun execute(field: Field) {
        field.organics
            .filter { it.canMove }
            .forEach { it.makeStep(field) }
    }

    private fun Organic.makeStep(field: Field) {
        val cell = this
        var newPosition = cell.position + cell.direction
        newPosition = when {
            field.isFree(newPosition) -> {
                newPosition
            }
            field[newPosition]?.body == cell.consume -> {
                val food = field[newPosition]!!
                food.drain(Settings.BiteYield)
                cell.consume(Settings.BiteYield)

                cell.position
            }
            else -> {
                cell.position
            }
        }

        if (newPosition != cell.position) {
            field.relocate(cell, newPosition)
        }
    }
}