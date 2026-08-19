package com.invenit.bacillus.stage

import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.Field
import com.invenit.bacillus.model.Organic
import com.invenit.bacillus.model.Point
import com.invenit.bacillus.model.matrix.Action
import com.invenit.bacillus.model.matrix.Sensor
import com.invenit.bacillus.service.RandomService

/**
 * Instruction DNA #1 §6, task 5 (#10). Each tick, reads a cell's current
 * state in its Decision Matrix, senses the one value its instruction tests,
 * and applies the act-then-test rule: the chosen action is stamped onto the
 * cell for the rest of this tick's steps (MoveStep/ConsumeStep gate on it),
 * and currentState advances to whatever the matrix says comes next.
 */
class DecideStep(
    private val random: RandomService
) : Step {

    override fun execute(field: Field) {
        field.organics.forEach { decide(it, field) }
    }

    private fun decide(cell: Organic, field: Field) {
        val matrix = cell.dna.decisionMatrix
        val sensorValue = sense(matrix[cell.currentState].sensor, cell, field)
        val result = matrix.evaluate(cell.currentState, sensorValue)

        cell.chosenAction = result.action
        cell.currentState = result.nextIndex
        cell.direction = direction(result.action, cell, field)
    }

    private fun sense(sensor: Sensor, cell: Organic, field: Field): Double = when (sensor) {
        Sensor.FoodDistance -> foodDistance(cell, field)
        Sensor.EnergyRatio -> cell.energy.toDouble() / cell.size.toDouble()
    }

    // Nearest cell matching dna.consume, in Chebyshev rings out from the
    // cell (see Field.iterateRadial). Nothing in range reads as just past
    // VisionRange, so both "<" and ">=" threshold tests see it as far away.
    private fun foodDistance(cell: Organic, field: Field): Double {
        var distance = Settings.VisionRange + 1

        field.iterateRadial(cell.position, Settings.VisionRange) { x, y ->
            val something = field[x, y]
            if (something?.body == cell.dna.consume) {
                distance = cell.position.distance(x, y)
                return@iterateRadial false
            }
            return@iterateRadial true
        }

        return distance.toDouble()
    }

    private fun direction(action: Action, cell: Organic, field: Field): Point {
        if (action.category != Action.Category.Move) {
            return Field.NoDirection
        }

        return when (action.mode!!) {
            Action.Mode.TowardConsume -> directionToFood(cell, field) ?: randomDirection(cell.position, field)
            Action.Mode.Hold -> Field.NoDirection
            // AwayFromToxin / TowardOpenSpace / Random modes are #1 task 6's
            // scope (full action set) — fall back to a random step so a
            // future genome reaching them doesn't freeze in place.
            Action.Mode.AwayFromToxin, Action.Mode.TowardOpenSpace, Action.Mode.Random ->
                randomDirection(cell.position, field)
        }
    }

    private fun directionToFood(cell: Organic, field: Field): Point? {
        var result: Point? = null
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

    private fun randomDirection(position: Point, field: Field): Point {
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
