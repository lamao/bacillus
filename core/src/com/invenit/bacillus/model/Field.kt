package com.invenit.bacillus.model

import com.badlogic.gdx.math.MathUtils

/**
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */
class Field(val width: Int, val height: Int) {

    val bacilli: MutableList<Bacillus> = mutableListOf()

    fun spawnCreature(): Bacillus {
        val bacillus = Bacillus(
            position = Point(
                MathUtils.random(width - 1),
                MathUtils.random(height - 1)
            ),
            direction = Point(
                MathUtils.random(-1, 1),
                MathUtils.random(-1, 1)
            )
        )
        bacilli.add(bacillus)

        return bacillus
    }

    fun isOutside(position: Point): Boolean {
        return position.x < 0 || position.x >= width
                || position.y < 0 || position.y >= height
    }

    fun fitInside(position: Point): Point {
        val x = if (position.x < 0) {
            0
        } else if (position.x >= width) {
            width - 1
        } else {
            position.x
        }

        val y = if (position.y < 0) {
            0
        } else if (position.y >= height) {
            height - 1
        } else {
            position.y
        }

        return Point(x, y)
    }

    fun moveBacilli() {
        for (bacillus in bacilli) {
            val newPosition = bacillus.position + bacillus.direction
            bacillus.position = if (isOutside(newPosition)) {
                fitInside(newPosition)
            } else {
                newPosition
            }
        }
    }
}