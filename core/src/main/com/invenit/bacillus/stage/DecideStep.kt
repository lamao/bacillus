package com.invenit.bacillus.stage

import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.Field
import com.invenit.bacillus.model.Organic
import com.invenit.bacillus.model.Point
import com.invenit.bacillus.model.Substance
import com.invenit.bacillus.model.matrix.Action
import com.invenit.bacillus.model.matrix.Sensor
import com.invenit.bacillus.service.RandomService

/**
 * Instruction DNA #1 §6, task 6 (#11). Each tick, reads a cell's current
 * state in its Decision Matrix, senses the one value its instruction tests,
 * and applies the act-then-test rule: the chosen action is stamped onto the
 * cell for the rest of this tick's steps (MoveStep/SplitStep/ProduceStep/
 * ConsumeStep gate on it), and currentState advances to whatever the matrix
 * says comes next.
 */
class DecideStep(
    private val random: RandomService
) : Step {

    override fun execute(field: Field) {
        field.organics.forEach { decide(it, field) }
    }

    private fun decide(cell: Organic, field: Field) {
        // Captured before MoveStep (later this tic) may relocate the cell,
        // so EnvironmentStage can animate from here to wherever it ends up.
        cell.previousPosition = cell.position

        val matrix = cell.dna.decisionMatrix
        val sensorValue = sense(matrix[cell.currentState].sensor, cell, field)
        val result = matrix.evaluate(cell.currentState, sensorValue)

        cell.chosenAction = result.action
        cell.currentState = result.nextIndex
        cell.direction = direction(result.action, cell, field)
    }

    private fun sense(sensor: Sensor, cell: Organic, field: Field): Double = when (sensor) {
        Sensor.FoodDistance -> distanceTo(cell, field, Settings.VisionRange, cell.dna.consume)
        Sensor.ToxinDistance -> distanceTo(cell, field, Settings.ToxinRange, cell.dna.toxin)
        Sensor.EnergyRatio -> cell.energy.toDouble() / cell.size.toDouble()
        Sensor.SizeRatio -> cell.size.toDouble() / Settings.MaxSize.toDouble()
        Sensor.Age -> cell.age.toDouble() / Settings.MaxAge.toDouble()
        Sensor.Crowding -> crowding(cell, field).toDouble()
        Sensor.Random -> random.random().toDouble()
    }

    // Nearest cell whose body matches `substance`, in Chebyshev rings out
    // from the cell (see Field.iterateRadial). Nothing in range reads as
    // just past `range`, so both "<" and ">=" threshold tests see it as far
    // away.
    private fun distanceTo(cell: Organic, field: Field, range: Int, substance: Substance): Double {
        var distance = range + 1

        field.iterateRadial(cell.position, range) { x, y ->
            val something = field[x, y]
            if (something?.body == substance) {
                distance = cell.position.distance(x, y)
                return@iterateRadial false
            }
            return@iterateRadial true
        }

        return distance.toDouble()
    }

    private fun crowding(cell: Organic, field: Field): Int {
        var count = 0

        field.iterateRadial(cell.position, Settings.VisionRange) { x, y ->
            if (field[x, y] is Organic) {
                count++
            }
            return@iterateRadial true
        }

        return count
    }

    private fun direction(action: Action, cell: Organic, field: Field): Point {
        if (action.category != Action.Category.Move) {
            return Field.NoDirection
        }

        return moveDirection(action.mode!!, cell, field)
    }

    private fun moveDirection(mode: Action.Mode, cell: Organic, field: Field): Point = when (mode) {
        Action.Mode.TowardConsume -> directionToFood(cell, field) ?: randomDirection(cell.position, field)
        Action.Mode.AwayFromToxin -> directionAwayFromToxin(cell, field) ?: randomDirection(cell.position, field)
        Action.Mode.TowardOpenSpace -> directionAwayFromCrowd(cell, field) ?: randomDirection(cell.position, field)
        Action.Mode.Random -> randomDirection(cell.position, field)
        Action.Mode.Hold -> Field.NoDirection
        Action.Mode.Release, Action.Mode.Hoard ->
            error("$mode is a Produce mode; moveDirection only runs for a chosen Move action")
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

    // Nearest toxin source wins, same as FoodDistance; the direction points
    // from it back to the cell, i.e. away.
    private fun directionAwayFromToxin(cell: Organic, field: Field): Point? {
        var result: Point? = null

        field.iterateRadial(cell.position, Settings.ToxinRange) { x, y ->
            val something = field[x, y]
            if (something?.body == cell.dna.toxin) {
                result = Point(x, y).direction(cell.position.x, cell.position.y)
                return@iterateRadial false
            }
            return@iterateRadial true
        }

        return result
    }

    // Steps away from the centroid of everything occupying a cell within
    // VisionRange. Null when nothing's nearby (already open) or the crowd
    // is symmetric around the cell (no direction reads as more open than
    // another) — either way, the caller falls back to a random step.
    private fun directionAwayFromCrowd(cell: Organic, field: Field): Point? {
        var sumX = 0
        var sumY = 0
        var count = 0

        field.iterateRadial(cell.position, Settings.VisionRange) { x, y ->
            if (field[x, y] != null) {
                sumX += x
                sumY += y
                count++
            }
            return@iterateRadial true
        }

        if (count == 0) {
            return null
        }

        val crowdCenter = Point(sumX / count, sumY / count)
        val direction = crowdCenter.direction(cell.position.x, cell.position.y)
        return if (direction == Field.NoDirection) null else direction
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
