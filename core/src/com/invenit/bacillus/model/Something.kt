package com.invenit.bacillus.model

/**
 *
 * @author vyacheslav.mischeryakov
 * Created: 16.11.21
 */
open class Something(
    var position: Point,
    var size: Int,
    val body: Substance
) {

    open fun drain(points: Int) {
        size -= points
    }
}