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
    // retired. Not part of DNA.Trait: its own five mutation operators (#1
    // §5, #12) are separate from trait mutation.
    val decisionMatrix: DecisionMatrix = DecisionMatrix.default(),
    // Per-lineage mutation counts (#12): how many times a mutation of each
    // kind has landed on this genome since the founder genome that seeded
    // the population - this genome's "distance" from that original. Not
    // mutable traits themselves, so not part of DNA.Trait; carried forward
    // unchanged by any DNA.copy() that doesn't touch them, and bumped only
    // by MutationServiceImpl.mutatedDna().
    val dmMutationCount: Int = 0,
    val traitMutationCount: Int = 0
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