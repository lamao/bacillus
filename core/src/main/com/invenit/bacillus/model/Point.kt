package com.invenit.bacillus.model

import kotlin.math.abs

/**
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */
data class Point(val x: Int, val y: Int) {

    companion object {
        val Zero = Point(0, 0)
    }

    operator fun plus(point: Point): Point = Point(x + point.x, y + point.y)
    operator fun minus(point: Point): Point = Point(x - point.x, y - point.y)


    fun distance(x: Int, y: Int): Int = Integer.max(abs(x - this.x), abs(y - this.y))

    fun direction(x: Int, y: Int): Point = Point(x - this.x, y - this.y).normalized()

    private fun normalized(): Point = Point(normalized(x), normalized(y))

    private fun normalized(value: Int): Int {
        return if (value > 0) {
            1
        } else if (value < 0) {
            -1
        } else {
            0
        }
    }
}