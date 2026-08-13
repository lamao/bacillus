package com.invenit.bacillus.model

/**
 * Instruction DNA #7 — draft, unintegrated (see issue #1 §2, §4, #7).
 * One state of a [DecisionMatrix]: an action to perform, and the one test that
 * decides where to go next. False always advances by 1; true jumps by [jumpOffset].
 */
data class Instruction(
    val action: Action,
    val sensor: Sensor,
    val comparator: Comparator,
    val threshold: Double,
    val jumpOffset: Int
)
