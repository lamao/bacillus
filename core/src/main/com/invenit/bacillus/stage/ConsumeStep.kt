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
            .forEach {
                val gain = Settings.SunYield
                val performance = 1 - Settings.ProductionPerformance
                val actualGain = (gain * performance).roundToInt()
                it.consume(actualGain)
                it.accumulatedWaste += (gain - actualGain)
            }
    }
}