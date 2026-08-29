package com.invenit.bacillus.model

import com.invenit.bacillus.model.matrix.DecisionMatrix

/**
 * Created by viacheslav.mishcheriakov
 * Created 21.11.2021
 */
data class DNA(
    val body: Substance,
    val consume: Substance,
    val produce: Substance,
    val toxin: Substance,
    // The Decision Matrix (#1 §6) now owns movement decisions; canMove is
    // retired. Not part of DNA.Trait: not mutable yet (#1 §5 mutation
    // operators land in a later task).
    val decisionMatrix: DecisionMatrix = DecisionMatrix.default()
) {

    enum class Trait {
        Body,
        Consume,
        Produce,
        Toxin;

        companion object {
            fun count() = Trait.entries.size
        }
    }
}