package com.invenit.bacillus.model

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils

/**
 *
 * @author vyacheslav.mischeryakov
 * Created: 16.11.21
 */
enum class Substance(val color: Color) {
    Nothing(Color.BLACK),
    Blue(Color.BLUE),
    Green(Color.GREEN),
    Pink(Color.PINK),
    White(Color.WHITE),
    Red(Color.RED);

    companion object {
        fun getRandomBody() = values()[MathUtils.random(1, values().size - 1)]
        fun getRandomConsume() = values()[MathUtils.random(0, values().size - 1)]
        fun getRandomProduce() = values()[MathUtils.random(1, values().size - 1)]
    }
}