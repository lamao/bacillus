package com.invenit.bacillus.model

import com.invenit.bacillus.Settings
import kotlin.math.roundToInt

/**
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */
data class Organic(
    override var position: Point,
    override var size: Int,
    var direction: Point,
    val dna: DNA
) : Something {
    var age: Int = 0
    var energy: Int = size

    override val body: Substance
        get() = dna.body

    var accumulatedWaste: Int = 0

    fun consume(gain: Int) {
        // TODO: Fix to more accurate numbers
        val actualGain = (gain * (1 - Settings.ProductionPerformance)).roundToInt()

        if (this.energy + actualGain <= this.size) {
            this.energy += actualGain
        } else {
            val energyGain = this.size - this.energy
            val sizeGain = actualGain - energyGain

            this.energy += energyGain
            this.size = Integer.min(this.size + sizeGain, Settings.MaxSize)
        }
        accumulatedWaste += (gain - actualGain)
    }

    override fun drain(points: Int) : Int {
        val actualDrain = super.drain(points)
        if (energy > size) {
            energy = size
        }
        return actualDrain
    }

}