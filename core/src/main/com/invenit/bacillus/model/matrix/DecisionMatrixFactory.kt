package com.invenit.bacillus.model.matrix

/**
 *
 * @author viacheslav.mishcheriakov
 * Created 13.08.2026
 */
fun interface DecisionMatrixFactory {
    /** The decision matrix to seed a newly created organic's [com.invenit.bacillus.model.DNA] with. */
    fun initial(): DecisionMatrix
}