package com.invenit.bacillus

/**
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */
object Settings {

    const val Width = 1000
    const val Height = 1000

    const val CellSize = 10

    const val GridWidth = Width / CellSize
    const val GridHeight = Height / CellSize

    var TicDelaySeconds = .03f
    val SmoothAnimation: Boolean
        get() = TicDelaySeconds > 0.2f

    const val DefaultHealth = 50
    const val ReproductionThreshold = 100
    const val ReturnHealthWhenReproductionFails = 0.5f
    const val MaxHealth = 200
    const val AttackDamage = 10

    const val ReproductionRange = 1
    const val SensivityRange = 1

    const val ProbabilityToSpawnOrganics = 0.0f
    const val MutationRate = 0.005f
    const val EnexpectedDeathRate = 0.0001f

    const val InitNumberOfOrganics = 50

    object Debug {
        const val displayGrid = false
        const val displaySourcePosition = false
    }
}