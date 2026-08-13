package com.invenit.bacillus.model

/**
 * Instruction DNA #7 — draft, unintegrated (see issue #1 §4, #7).
 */
enum class Comparator {
    LessThan,
    GreaterThanOrEqual;

    fun test(value: Double, threshold: Double): Boolean = when (this) {
        LessThan -> value < threshold
        GreaterThanOrEqual -> value >= threshold
    }
}
