package com.invenit.bacillus.util

import com.badlogic.gdx.math.MathUtils
import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.Field
import com.invenit.bacillus.model.Organic
import com.invenit.bacillus.model.Point
import com.invenit.bacillus.model.Substance

/**
 * Created by vyacheslav.mischeryakov
 * Created 21.11.2021
 */
// TODO: Refactor
object Mutator {

    fun getRandomSize() =
        Settings.DefaultSize + MathUtils.random(-Settings.DefaultSize / 4, Settings.DefaultSize / 4)

    // TODO: Move mutable traits to DNA class
    fun cloneWithMutation(
        cell: Organic,
        offspingPosition: Point,
        offspingSize: Int
    ): Organic {


        var body = cell.body
        var consume = cell.consume
        var produce = cell.produce
        var canMove = cell.canMove
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

        return Organic(
            position = offspingPosition,
            direction = Field.NoDirection,
            size = offspingSize,
            body = body,
            consume = consume,
            produce = produce,
            canMove = canMove
        )
    }
}