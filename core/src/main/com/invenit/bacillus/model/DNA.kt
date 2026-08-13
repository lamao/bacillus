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
    val canMove: Boolean,
    // Instruction DNA #7 — draft, unintegrated. Not part of DNA.Trait: not
    // mutable yet (#1 §5 mutation operators land in a later task) and not
    // read by any Step (#1 §6 wiring lands in a later task).
    val decisionMatrix: DecisionMatrix = DecisionMatrix.default()
) {

    enum class Trait {
        Body,
        Consume,
        Produce,
        Toxin,
        CanMove;

        companion object {
            fun count() = Trait.entries.size
        }
    }
}