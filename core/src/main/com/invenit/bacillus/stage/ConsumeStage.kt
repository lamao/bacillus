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
class ConsumeStage : Stage {

    override fun execute(field: Field) {
        field.organics.filter { it.dna.consume == Substance.Sun }
            .forEach { it.consume(Settings.SunYield) }
        field.organics.forEach { consumeMinerals(it, field) }
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