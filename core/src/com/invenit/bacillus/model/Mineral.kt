package com.invenit.bacillus.model

/**
 *
 * @author vyacheslav.mischeryakov
 * Created: 16.11.21
 */
class Mineral(
    position: Point,
    energy: Int,
    body: Substance
) : Something(position, energy, body) {
}