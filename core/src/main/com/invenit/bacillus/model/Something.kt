package com.invenit.bacillus.model

/**
 *
 * @author vyacheslav.mischeryakov
 * Created: 16.11.21
 */
interface Something {
    var position: Point
    var size: Int
    val body: Substance

    fun drain(points: Int) {
        size -= points
    }
}