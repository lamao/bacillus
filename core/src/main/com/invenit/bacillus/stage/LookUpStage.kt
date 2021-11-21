package com.invenit.bacillus.stage

import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.Field
import com.invenit.bacillus.model.Organic
import com.invenit.bacillus.model.Point

/**
 * Created by vyacheslav.mischeryakov
 * Created 21.11.2021
 */
class LookUpStage : Stage {

    override fun execute(field: Field) {
        field.organics.filter { it.canMove }
            .forEach { lookUp(it, field) }
    }

    private fun lookUp(cell: Organic, field: Field) {

        val directionToFood = getDirectionToFood(cell, field)
        cell.direction = if (directionToFood == Field.NoDirection) {
            field.getRandomFreeDirection(cell.position)
        } else {
            directionToFood
        }

        if (cell.direction != Field.NoDirection) {
            cell.energy -= Settings.MoveConsumption
        }
    }

    private fun getDirectionToFood(cell: Organic, field: Field): Point {

        var result = Field.NoDirection

        field.iterateRadial(cell.position, Settings.VisionRange) { x, y ->
            if (field[x, y]?.body == cell.consume) {
                result = cell.position.direction(x, y)
                return@iterateRadial false
            }
            return@iterateRadial true
        }

        return result
    }
}