package com.invenit.bacillus.model.matrix

/**
 * Instruction DNA #7 — draft, unintegrated (see issue #1 §4, #7).
 */
enum class Comparator {
    LessThan,
    GreaterThanOrEqual;

    fun test(value: Int, threshold: Int): Boolean = when (this) {
        LessThan -> value < threshold
        GreaterThanOrEqual -> value >= threshold
    }
}
