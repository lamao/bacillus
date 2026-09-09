package com.invenit.bacillus.stage

import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.Field
import com.invenit.bacillus.model.Organic
import com.invenit.bacillus.model.Point
import com.invenit.bacillus.model.Substance
import com.invenit.bacillus.model.matrix.Action
import com.invenit.bacillus.model.matrix.Sensor
import com.invenit.bacillus.service.RandomService
import kotlin.math.round

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

    private fun sense(sensor: Sensor, cell: Organic, field: Field): Int = when (sensor) {
        Sensor.FoodDistance -> distanceTo(cell.position, field, Settings.VisionRange, cell.dna.consume)
        Sensor.ToxinDistance -> distanceTo(cell.position, field, Settings.ToxinRange, cell.dna.toxin)
        Sensor.EnergyRatio -> cell.energyPercentage
        Sensor.SizeRatio -> cell.sizePercentage
        Sensor.Age -> cell.agePercentage
        Sensor.Crowding -> crowding(cell.position, field)
        Sensor.Random -> round(random.random() * 100).toInt()
    }



    /**
     * The nearest cell whose body matches `substance`, in Chebyshev rings out
     * from the cell (see Field.iterateRadial). Nothing in range reads as
     * just past `range`, so both "<" and ">=" threshold tests see it as far
     * away.
     * @param position source position
     * @param field field object
     * @param range range to look up
     * @param substance substance to search distance to
     */
    private fun distanceTo(position: Point, field: Field, range: Int, substance: Substance): Int {
        var distance = range + 1

        field.iterateRadial(position, range) { x, y ->
            val something = field[x, y]
            if (something?.body == substance) {
                distance = position.distance(x, y)
                return@iterateRadial false
            }
            return@iterateRadial true
        }

        return distance
    }

    private fun crowding(position: Point, field: Field): Int {
        var count = 0

        field.iterateRadial(position, Settings.VisionRange) { x, y ->
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
        Action.Mode.AwayFromToxin -> directionAwayFromToxin(cell, field)
        Action.Mode.TowardOpenSpace -> directionAwayFromCrowd(cell.position, field)
        Action.Mode.Random -> randomDirection(cell.position, field)
        Action.Mode.Hold -> Field.NoDirection
        Action.Mode.Release, Action.Mode.Retain ->
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

    /**
     * The nearest toxin source wins, same as [distanceTo]/FoodDistance; the
     * direction returned points from it back to the cell, i.e. away.
     * @param cell the cell fleeing the toxin
     * @param field the field to search
     * @return the direction away from the nearest matching toxin cell within
     * [Settings.ToxinRange], or `null` if none is in range
     */
    private fun directionAwayFromToxin(cell: Organic, field: Field): Point {
        var direction: Point? = null

        field.iterateRadial(cell.position, Settings.ToxinRange) { x, y ->
            val something = field[x, y]
            if (something?.body == cell.dna.toxin) {
                direction = Point(x, y).direction(cell.position.x, cell.position.y)
                return@iterateRadial false
            }
            return@iterateRadial true
        }

        val result = direction ?: return Field.NoDirection
        if (field.isOutside(cell.position + result)) {
            return Field.NoDirection
        }
        return result
    }

    /**
     * Steps away from the centroid of everything occupying a cell within
     * [Settings.VisionRange].
     * @param position the position to step away from the crowd around
     * @param field the field to search
     * @return the direction away from the crowd centroid, or `null` when
     * nothing's nearby (already open) or the crowd is symmetric around
     * `position` (no direction reads as more open than another) — either
     * way, the caller falls back to a random step
     */
    private fun directionAwayFromCrowd(position: Point, field: Field): Point {
        var sumX = 0
        var sumY = 0
        var count = 0

        field.iterateRadial(position, Settings.VisionRange) { x, y ->
            if (field[x, y] != null) {
                sumX += x
                sumY += y
                count++
            }
            return@iterateRadial true
        }

        if (count == 0) {
            return Field.NoDirection
        }

        val crowdCenter = Point(sumX / count, sumY / count)
        val direction = crowdCenter.direction(position.x, position.y)

        val newPosition = position + direction
        if (field.isOutside(newPosition)) {
            return Field.NoDirection
        }
        return direction
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
