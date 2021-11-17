package com.invenit.bacillus.model

/**
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */
class Organic(
    position: Point,
    energy: Int,
    var direction: Point,
    body: Substance,
    val consume: Substance,
    val produce: Substance,
    val canMove: Boolean,
    var age: Int = 0
) : Something(position, energy, body) {

}