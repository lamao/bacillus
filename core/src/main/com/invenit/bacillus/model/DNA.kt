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
    val toxin: Substance,
    val canMove: Boolean
) {

    enum class Trait {
        Body,
        Consume,
        Produce,
        Toxin,
        CanMove;

        companion object {
            fun count() = values().size
        }
    }

    fun mutated(): DNA {
        var body = this.body
        var consume = this.consume
        var produce = this.produce
        var poison = this.toxin
        var canMove = this.canMove
        if (MathUtils.random() < Settings.MutationRate) {
            // TODO: Refactor
            when (MathUtils.random(Trait.count() - 1)) {
                Trait.Body.ordinal -> {
                    body = Substance.getRandomBody()
                }
                Trait.Consume.ordinal -> {
                    consume = Substance.getRandomConsume()
                }
                Trait.Produce.ordinal -> {
                    produce = Substance.getRandomProduce()
                }
                Trait.Toxin.ordinal -> {
                    poison = Substance.getRandomToxin()
                }
                Trait.CanMove.ordinal -> {
                    canMove = MathUtils.randomBoolean()
                }
            }
        }

        return DNA(body, consume, produce, poison, canMove)
    }
}