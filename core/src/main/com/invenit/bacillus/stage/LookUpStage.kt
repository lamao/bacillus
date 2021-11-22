package com.invenit.bacillus.stage

import com.badlogic.gdx.math.MathUtils
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
        field.organics.filter { it.dna.canMove }
            .forEach { lookUp(it, field) }
    }

    private fun lookUp(cell: Organic, field: Field) {

        val directionToFood = getDirectionToFood(cell, field)
        cell.direction = if (directionToFood == Field.NoDirection) {
            getRandomDirection(cell.position, field)
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
            if (field[x, y]?.body == cell.dna.consume) {
                result = cell.position.direction(x, y)
                return@iterateRadial false
            }
            return@iterateRadial true
        }

        return result
    }

    private fun getRandomDirection(position: Point, field: Field): Point {
        val direction = Point(
            x = MathUtils.random(-1, 1),
            y = MathUtils.random(-1, 1)
        )

        val newPosition = position + direction
        if (field.isOutside(newPosition)) {
            return Field.NoDirection
        }

        return direction
    }
}