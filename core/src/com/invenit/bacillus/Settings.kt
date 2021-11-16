package com.invenit.bacillus

/**
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */
object Settings {

    const val Width = 500
    const val Height = 500

    const val CellSize = 10

    const val GridWidth = Width / CellSize
    const val GridHeight = Height / CellSize

    const val TicDelaySeconds = .05f

    const val DefaultHealth = 50
    const val ReproductionThreshold = 100
    const val MaxHealth = 200
    const val AttackDamage = 10

    const val ReproductionRange = 1

    const val ProbabilityToSpawnBacillus = 0.0f
    const val ProbabilityToSpawnFood = 0.0f

    const val InitNumberOfFood = 200
    const val InitNumberOfBacilli = 30

    object Debug {
        const val displayGrid = false
        const val displaySourcePosition = false
    }
}