package com.invenit.bacillus

/**
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */
object Settings {

    const val Width = 1000
    const val Height = 500

    const val CellSize = 20

    const val GridWidth = Width / CellSize
    const val GridHeight = Height / CellSize

    const val TicDelaySeconds = 0.3f

    const val DefaultHealth = 50
    const val ProbabilityToSpawn = 1f

    const val InitNumberOfBacilli = 100
}