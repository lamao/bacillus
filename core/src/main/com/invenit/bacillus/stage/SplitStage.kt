package com.invenit.bacillus.stage

import com.badlogic.gdx.math.MathUtils
import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.Field
import com.invenit.bacillus.model.Organic
import com.invenit.bacillus.model.Point
import com.invenit.bacillus.util.Mutator
import kotlin.math.roundToInt

/**
 * Created by vyacheslav.mischeryakov
 * Created 21.11.2021
 */
class SplitStage : Stage {
    override fun execute(field: Field) {
        field.organics
            .filter { it.energy >= Settings.ReproductionThreshold }
            .forEach { split(it, field) }
    }

    private fun split(cell: Organic, field: Field) {
        val offspringOffset = Point(
            MathUtils.random(-Settings.ReproductionRange, Settings.ReproductionRange),
            MathUtils.random(-Settings.ReproductionRange, Settings.ReproductionRange)
        )

        val offspingSize = Mutator.getRandomSize()

        cell.energy -= offspingSize

        val offspingPosition = cell.position + offspringOffset
        if (field.isOutside(offspingPosition) || !field.isFree(offspingPosition)) {
            cell.energy += (offspingSize * Settings.ReturnHealthWhenReproductionFails).roundToInt()
            return
        }

        cell.size -= offspingSize
        val offsping = Mutator.cloneWithMutation(cell, offspingPosition, offspingSize)
        field.add(offsping)
    }
}