package com.invenit.bacillus.stage

import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.Field
import com.invenit.bacillus.model.Organic
import com.invenit.bacillus.model.Point
import com.invenit.bacillus.service.RandomService

/**
 * Created by viacheslav.mishcheriakov
 * Created 21.11.2021
 */
class LookUpStep(
    private val random: RandomService,
): Step {



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
    }

    private fun getDirectionToFood(cell: Organic, field: Field): Point {

        var result = Field.NoDirection
        var bestSize = 0

        field.iterateRadial(cell.position, Settings.VisionRange) { x, y ->
            val something = field[x, y]
            if (something?.body == cell.dna.consume && something.size > bestSize) {
                result = cell.position.direction(x, y)
                bestSize = something.size
            }
            return@iterateRadial true
        }

        return result
    }

    private fun getRandomDirection(position: Point, field: Field): Point {
        val direction = Point(
            x = random.random(-1, 1),
            y = random.random(-1, 1)
        )

        val newPosition = position + direction
        if (field.isOutside(newPosition)) {
            return Field.NoDirection
        }

        return direction
    }
}