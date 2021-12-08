package com.invenit.bacillus.model

/**
 * Created by vyacheslav.mischeryakov
 * Created 21.11.2021
 */
data class DNA(
    val body: Substance,
    val consume: Substance,
    val produce: Substance,
    val toxin: Substance,
    val canMove: Boolean
) {

    enum class Trait {
        Body,
        Consume,
        Produce,
        Toxin,
        CanMove;

        companion object {
            fun count() = values().size
        }
    }
}