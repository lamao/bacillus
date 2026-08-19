package com.invenit.bacillus.ui

import com.invenit.bacillus.model.matrix.Action
import com.invenit.bacillus.model.matrix.Comparator
import com.invenit.bacillus.model.matrix.Instruction
import com.invenit.bacillus.model.matrix.Sensor

/**
 * Compact 3-line label for one [Instruction] in a [com.invenit.bacillus.ui.CellDetailsStage]
 * DecisionMatrix grid cell (#9): action, then its test, then the jump it
 * takes when that test passes.
 */
fun Instruction.toDisplayText(): String =
    "${action.toDisplayText()}\n${sensor.abbreviation}${comparator.symbol}${"%.2f".format(threshold)}\n${jumpOffset.toSignedString()}"

private fun Action.toDisplayText(): String = when (category) {
    Action.Category.Rest -> "Rest"
    Action.Category.Move -> "Move:${mode!!.abbreviation}"
}

private val Action.Mode.abbreviation: String
    get() = when (this) {
        Action.Mode.TowardConsume -> "seek"
        Action.Mode.AwayFromToxin -> "flee"
        Action.Mode.TowardOpenSpace -> "open"
        Action.Mode.Random -> "rand"
        Action.Mode.Hold -> "hold"
    }

private val Sensor.abbreviation: String
    get() = when (this) {
        Sensor.FoodDistance -> "F"
        Sensor.EnergyRatio -> "E"
    }

private val Comparator.symbol: String
    get() = when (this) {
        Comparator.LessThan -> "<"
        Comparator.GreaterThanOrEqual -> ">="
    }

private fun Int.toSignedString(): String = if (this >= 0) "+$this" else "$this"
