package com.invenit.bacillus.model

/**
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */
data class Point(val x: Int, val y: Int) {

    operator fun plus(point: Point): Point = Point(x + point.x, y + point.y)
    operator fun minus(point: Point): Point = Point(x - point.x, y - point.y)

    fun normalized(): Point = Point(normalized(x), normalized(y))

    private fun normalized(value:Int) : Int {
        return if (value > 0) {
            1
        } else if (value < 0) {
            -1
        } else {
            0
        }
    }

}