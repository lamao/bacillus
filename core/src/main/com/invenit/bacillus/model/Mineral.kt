package com.invenit.bacillus.model

/**
 *
 * @author vyacheslav.mischeryakov
 * Created: 16.11.21
 */
class Mineral(
    position: Point,
    size: Int,
    body: Substance
) : Something(position, size, body) {
}