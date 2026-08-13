package com.invenit.bacillus.model.matrix

/**
 * Instruction DNA #7 — draft, unintegrated (see issue #1 §3, #7).
 * Minimal seed set: Move and Rest categories only, full set is #1 task 6's scope.
 */
data class Action(val category: Category, val mode: Mode? = null) {

    init {
        require((category == Category.Move) == (mode != null)) {
            "mode is required for Move and must be absent for $category"
        }
    }

    enum class Category {
        Move,
        Rest
    }

    enum class Mode {
        TowardConsume,
        AwayFromToxin,
        TowardOpenSpace,
        Random,
        Hold
    }
}