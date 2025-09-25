package com.invenit.bacillus.stage

import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.Field
import com.invenit.bacillus.model.Organic
import com.invenit.bacillus.model.Point

/**
 * Created by viacheslav.mishcheriakov
 * Created 21.11.2021
 */
class MoveStage : Stage {
    override fun execute(field: Field) {
        field.organics
            .filter { it.dna.canMove }
            .forEach { it.makeStep(field) }
    }

    private fun Organic.makeStep(field: Field) {
        val cell = this
        if (cell.direction == Point.Zero) {
            return
        }

        var newPosition = cell.position + cell.direction
        newPosition = when {
            field.isFree(newPosition) -> {
                newPosition
            }
            field[newPosition]?.body == cell.dna.consume -> {
                val food = field[newPosition]!!
                val actualDrain = food.drain(Settings.BiteYield)
                cell.consume(actualDrain)

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