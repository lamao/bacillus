package com.invenit.bacillus.model

/**
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */
class Organic(
    position: Point,
    energy: Int,
    var direction: Point,
    body: Substance
) : Something(position, energy, body) {
}