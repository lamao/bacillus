package com.invenit.bacillus.model

import com.invenit.bacillus.Settings

/**
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */
class Organic(
    position: Point,
    size: Int,
    var direction: Point,
    body: Substance,
    val consume: Substance,
    val produce: Substance,
    val canMove: Boolean
) : Something(position, size, body) {
    var age: Int = 0
    var energy: Int = size

    fun consume(gain: Int) {
        if (this.energy + gain <= this.size) {
            this.energy += gain
        } else {
            val energyGain = this.size - this.energy
            val sizeGain = gain - energyGain

            this.energy += energyGain
            this.size = Integer.min(this.size + sizeGain, Settings.MaxSize)
        }
    }

    override fun drain(points: Int) {
        super.drain(points)
        if (energy > size) {
            energy = size
        }
    }


}