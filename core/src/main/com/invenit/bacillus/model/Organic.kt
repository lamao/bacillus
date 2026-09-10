package com.invenit.bacillus.model

import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.matrix.Action
import kotlin.math.roundToInt

/**
 * Created by viacheslav.mishcheriakov
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

    val energyPercentage: Int
        get() = toPercentage(energy, size)
    val sizePercentage: Int
        get() = toPercentage(size, Settings.MaxSize)
    val agePercentage: Int
        get() = toPercentage(age, Settings.MaxAge)

    // Decision Matrix instruction pointer (#1 §6) — runtime state, not part
    // of the genome. Newborns start at state 0 (#1 §2 ground rules).
    var currentState: Int = 0
    var chosenAction: Action = Action(Action.Category.Rest)

    // Position at the start of this tic, captured before MoveStep may
    // relocate the cell. Movement is now gated by chosenAction, so it can
    // stop between tics — animation interpolates from here to the current
    // (already up to date) position instead of extrapolating from direction.
    var previousPosition: Point = position

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

    // `whole` is `size` for energyPercentage - normally >0, but drain() can
    // take it to 0 (it clamps energy to match in that case, so energy<=0
    // too and ClearExhaustedEntitiesStep reaps the cell same-tick; still,
    // nothing here should crash if that invariant ever slips).
    private fun toPercentage(part: Int, whole: Int): Int = if (whole <= 0) 0 else (part * 100) / whole

}