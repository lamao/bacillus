package com.invenit.bacillus.model

import kotlin.math.abs

/**
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */
data class Point(val x: Int, val y: Int) {

    operator fun plus(point: Point): Point = Point(x + point.x, y + point.y)
    operator fun minus(point: Point): Point = Point(x - point.x, y - point.y)


    fun distance(x: Int, y: Int): Int = Integer.max(abs(x - this.x), abs(y - this.y))
}