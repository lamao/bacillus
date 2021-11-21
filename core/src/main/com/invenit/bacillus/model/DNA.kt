package com.invenit.bacillus.model

import com.badlogic.gdx.math.MathUtils
import com.invenit.bacillus.Settings

/**
 * Created by vyacheslav.mischeryakov
 * Created 21.11.2021
 */
data class DNA(
    val body: Substance,
    val consume: Substance,
    val produce: Substance,
    val canMove: Boolean
) {

    fun mutated(): DNA {
        var body = this.body
        var consume = this.consume
        var produce = this.produce
        var canMove = this.canMove
        if (MathUtils.random() < Settings.MutationRate) {
            // TODO: Refactor
            when (MathUtils.random(0, 3)) {
                0 -> {
                    body = Substance.getRandomBody()
                }
                1 -> {
                    do {
                        consume = Substance.getRandomConsume()
                    } while (consume == produce)
                }
                2 -> {
                    do {
                        produce = Substance.getRandomProduce()
                    } while (produce == consume)
                }
                3 -> {
                    canMove = MathUtils.randomBoolean()
                }
            }
        }

        return DNA(body, consume, produce, canMove)
    }
}