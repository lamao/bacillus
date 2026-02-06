package com.invenit.bacillus.stage

import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.Field
import com.invenit.bacillus.model.Organic
import kotlin.math.roundToInt

class ToxinStep : Step {

    override fun execute(field: Field) {
        field.organics.forEach {
            getPoisoned(it, field)
        }
    }

    private fun getPoisoned(cell: Organic, field: Field) {
        var totalDamage = 0f

        field.iterateRadial(cell.position, Settings.ToxinRange) {x, y ->
            val something = field[x, y]
            if (something?.body == cell.dna.toxin) {
                val toxinAmount = something.size
                val distance = cell.position.distance(x, y)

                totalDamage += Settings.toxinDamageFunction(toxinAmount.toFloat(), distance)

            }
            return@iterateRadial true
        }

        cell.energy -= totalDamage.roundToInt()
    }

}
