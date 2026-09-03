package com.invenit.bacillus.model.matrix

/**
 * Instruction DNA #1 §3 — full action taxonomy. Move and Produce carry a
 * mode; Rest and Split are complete on their own — Split's only behavior is
 * "attempt if energy allows" (#1 §3), so there is nothing left to select.
 */
data class Action(val category: Category, val mode: Mode? = null) {

    init {
        val expectsMode = category == Category.Move || category == Category.Produce
        require(expectsMode == (mode != null)) {
            "mode is required for Move/Produce and must be absent for $category"
        }
        require(mode == null || mode in category.modes) {
            "$mode is not a valid mode for $category"
        }
    }

    enum class Category(val modes: Set<Mode>) {
        Move(setOf(Mode.TowardConsume, Mode.AwayFromToxin, Mode.TowardOpenSpace, Mode.Random, Mode.Hold)),
        Rest(emptySet()),
        Produce(setOf(Mode.Release, Mode.Hoard)),
        Split(emptySet())
    }

    enum class Mode {
        TowardConsume,
        AwayFromToxin,
        TowardOpenSpace,
        Random,
        Hold,

        // Produce modes. "Hoard" is the issue's own wording for Produce's
        // "Hold" ("keep hoarding") — Move already owns the name "Hold".
        Release,
        Hoard
    }
}
