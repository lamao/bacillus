package com.invenit.bacillus.stage

import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.Field
import com.invenit.bacillus.model.Organic
import com.invenit.bacillus.model.Point
import com.invenit.bacillus.service.MutationService
import com.invenit.bacillus.service.RandomService
import kotlin.math.roundToInt

/**
 * Created by vyacheslav.mischeryakov
 * Created 21.11.2021
 */
class SplitStage(
    private val random: RandomService,
    private val mutationService: MutationService
) : Stage {


    override fun execute(field: Field) {
        field.organics
            .filter { it.energy >= Settings.ReproductionThreshold }
            .forEach { split(it, field) }
    }

    private fun split(cell: Organic, field: Field) {
        val offspringOffset = Point(
            random.random(-Settings.ReproductionRange, Settings.ReproductionRange),
            random.random(-Settings.ReproductionRange, Settings.ReproductionRange)
        )

        val offspingSize = mutationService.mutatedSize(Settings.DefaultSize)

        cell.energy -= offspingSize

        val offspingPosition = cell.position + offspringOffset
        if (field.isOutside(offspingPosition) || !field.isFree(offspingPosition)) {
            cell.energy += (offspingSize * Settings.ReturnHealthWhenReproductionFails).roundToInt()
            return
        }

        cell.size -= offspingSize
        field.add(
            Organic(
                position = offspingPosition,
                direction = Field.NoDirection,
                size = offspingSize,
                dna = mutationService.mutatedDna(cell.dna)
            )
        )
    }
}