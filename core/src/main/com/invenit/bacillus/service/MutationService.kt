package com.invenit.bacillus.service

import com.invenit.bacillus.model.DNA
import com.invenit.bacillus.model.Substance
import com.invenit.bacillus.model.matrix.DecisionMatrix

/**
 * Created by viacheslav.mishcheriakov
 * Created 08.12.2021
 */
interface MutationService {

    fun mutatedSize(size: Int) : Int
    fun mutatedDna(dna: DNA) : DNA

    /**
     * Applies one randomly chosen DM mutation operator (#1 §5) to [matrix],
     * unconditionally - unlike [mutatedDna], this isn't gated by
     * [com.invenit.bacillus.Settings.MutationRate]. Used where a mutation is
     * wanted on demand rather than as part of the automatic reproduction
     * roll, e.g. spawning a manually configured creature with a mutated
     * variant of a starter genome (#13).
     * @param matrix the Decision Matrix to mutate
     * @return a copy of [matrix] with exactly one state's instruction changed
     */
    fun mutatedDecisionMatrix(matrix: DecisionMatrix): DecisionMatrix

    fun randomBody(): Substance
    fun randomConsume(): Substance
    fun randomProduce(): Substance
    fun randomToxin(): Substance
}