package com.invenit.bacillus.stage

import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.Field
import com.invenit.bacillus.model.Mineral
import com.invenit.bacillus.model.Organic
import com.invenit.bacillus.model.Point

/**
 * Created by viacheslav.mishcheriakov
 * Created 21.11.2021
 */
class ProduceStage : Stage {
    override fun execute(field: Field) {
        field.organics.forEach { produceMineral(it, field) }
    }

    // TODO: Rename to avoid intersection with native Organic.produce
    private fun produceMineral(cell: Organic, field: Field) {
        var waste = cell.accumulatedWaste
        if (waste == 0) {
            return
        }
        cell.accumulatedWaste = 0

        field.iterateRadial(cell.position, Settings.ProductionRange) { x, y ->
            val something = field[x, y]
            if (something is Mineral && something.body == cell.dna.produce) {
                val amountToAdd = Integer.min(waste, Settings.MaxSize - something.size)
                something.size += amountToAdd
                waste -= amountToAdd
            }

            return@iterateRadial waste > 0
        }

        if (waste > 0) {
            field.iterateRadial(cell.position, Settings.ProductionRange) { x, y ->
                if (field.isFree(x, y)) {
                    val amountToAdd = Integer.min(waste, Settings.MaxSize)
                    field.add(
                        Mineral(
                            Point(x, y),
                            amountToAdd,
                            cell.dna.produce
                        )
                    )
                    waste -= amountToAdd
                }
                return@iterateRadial waste > 0
            }
        }


        if (waste > 0) {
            cell.energy -= waste
        }
    }
}