package com.invenit.bacillus.model

/**
 *
 * @author viacheslav.mishcheriakov
 * Created: 16.11.21
 */
interface Something {
    var position: Point
    var size: Int
    val body: Substance

    fun drain(points: Int): Int {
        return if (size >= points) {
            size -= points
            points
        } else {
            val actualDrain = size
            size = 0
            actualDrain
        }
    }
}