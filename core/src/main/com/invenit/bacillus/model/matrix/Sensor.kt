package com.invenit.bacillus.model.matrix

/**
 * Instruction DNA #1 §4 — full sensor taxonomy. Each instruction's one test
 * compares one of these scalars against a mutable threshold.
 */
enum class Sensor {
    FoodDistance,
    ToxinDistance,
    EnergyRatio,
    SizeRatio,
    Age,
    Crowding,
    Random
}
