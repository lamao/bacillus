package com.invenit.bacillus.model

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle

/**
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */
class Field(val width: Int, val height: Int) {

    val bacilli: MutableList<Bacillus> = mutableListOf()

    fun spawnCreature(): Bacillus {
        val position = Point(
            MathUtils.random(width - 1),
            MathUtils.random(height - 1 )
        )
        val bacillus = Bacillus(position)
        bacilli.add(bacillus)

        return bacillus
    }
}