package com.invenit.bacillus.stage

import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.Field
import com.invenit.bacillus.model.Organic
import com.invenit.bacillus.model.Substance
import com.invenit.bacillus.model.matrix.Action
import kotlin.math.roundToInt

/**
 * Created by viacheslav.mishcheriakov
 * Created 21.11.2021
 */
class ConsumeStep : Step {

    override fun execute(field: Field) {
        field.organics
            .filter { isDigesting(it) }
            .forEach { digest(it, field) }
    }

    // Digestion runs for cells whose chosen action this tick isn't Move
    // (#1 §6, #2) — covers both the Sun-income branch and radial "spread"
    // substance absorption below, generalized beyond just Sun (#1 task 6).
    private fun isDigesting(cell: Organic): Boolean = cell.chosenAction.category != Action.Category.Move

    private fun digest(cell: Organic, field: Field) {
        if (cell.dna.consume == Substance.Sun) {
            cell.consume(Settings.SunYield)
        } else {
            consumeMinerals(cell, field)
        }
    }

    private fun consumeMinerals(cell: Organic, field: Field) {

        var result = 0f

        field.iterateRadial(cell.position, Settings.ConsumingRange) { x, y ->
            val something = field[x, y]
            if (something?.body == cell.dna.consume) {
                val rawGain = Integer.min(something.size, Settings.MineralsYield)
                val distance = cell.position.distance(x, y)

                // TODO: More accurate calculations
                result += Settings.correctedMineralsYield(rawGain.toFloat(), distance)
                something.drain(rawGain)

                if (cell.energy + result.roundToInt() > Settings.MaxSize) {
                    return@iterateRadial false
                }
            }
            return@iterateRadial true
        }

        cell.consume(result.roundToInt())
    }
}