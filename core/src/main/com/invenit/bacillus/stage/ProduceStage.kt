package com.invenit.bacillus.stage

import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.Field
import com.invenit.bacillus.model.Mineral
import com.invenit.bacillus.model.Organic
import com.invenit.bacillus.model.Point

/**
 * Created by vyacheslav.mischeryakov
 * Created 21.11.2021
 */
class ProduceStage : Stage {
    override fun execute(field: Field) {
        field.organics.forEach { produceMineral(it, field) }
    }

    // TODO: Rename to avoid intersection with native Organic.produce
    private fun produceMineral(cell: Organic, field: Field) {
        var produced = cell.produced()
        if (produced == 0) {
            return
        }

        field.iterateRadial(cell.position, Settings.ProductionRange) { x, y ->
            val something = field[x, y]
            if (something is Mineral && something.body == cell.dna.produce) {
                val amountToAdd = Integer.min(produced, Settings.MaxSize - something.size)
                something.size += amountToAdd
                produced -= amountToAdd
            }

            return@iterateRadial produced > 0
        }

        if (produced > 0) {
            field.iterateRadial(cell.position, Settings.ProductionRange) { x, y ->
                if (field.isFree(x, y)) {
                    val amountToAdd = Integer.min(produced, Settings.MaxSize)
                    field.add(
                        Mineral(
                            Point(x, y),
                            amountToAdd,
                            cell.dna.produce
                        )
                    )
                    produced -= amountToAdd
                }
                return@iterateRadial produced > 0
            }
        }


        if (produced > 0) {
            cell.energy -= produced
        }
    }
}