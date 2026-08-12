package com.invenit.bacillus.stage

import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.Field
import com.invenit.bacillus.model.Organic
import com.invenit.bacillus.model.Substance
import kotlin.math.roundToInt

/**
 * Created by viacheslav.mishcheriakov
 * Created 21.11.2021
 */
class ConsumeStep : Step {

    override fun execute(field: Field) {
        field.organics.filter { it.dna.consume == Substance.Sun }
            .forEach { it.consume(Settings.SunYield) }
        field.organics
            .filter { isDigesting(it) }
            .forEach { consumeMinerals(it, field) }
    }

    // Digestion runs for cells not currently moving. Today that's the static
    // canMove flag; once DecideStep (#1 §6) exists, swap this for
    // "the cell's chosen action isn't Move".
    private fun isDigesting(cell: Organic): Boolean = !cell.dna.canMove

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