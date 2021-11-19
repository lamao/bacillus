package com.invenit.bacillus

/**
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */
object Settings {

    const val Width = 1000
    const val Height = 1000

    const val CellSize = 3

    const val GridWidth = Width / CellSize
    const val GridHeight = Height / CellSize

    var TicDelaySeconds = .02f
    val SmoothAnimation: Boolean
        get() = TicDelaySeconds > 0.2f

    const val BiteYield = 200
    const val SunYield = 20
    const val MineralsYield = 10
    fun correctedMineralsYield(amount: Float, distance: Int): Float =
        amount /// 2f.pow(distance - 1)

    const val MoveConsumption = 10
    const val PermanentConsumption = 10

    const val ProductionPerformance = 0.1f
    const val MineralDegradation = 3

    const val DefaultSize = 750
    const val ReproductionThreshold = 2000
    const val MaxSize = ReproductionThreshold + BiteYield
    const val MaxAge = 1500

    const val ReturnHealthWhenReproductionFails = 0.5f


    const val ReproductionRange = 1
    const val VisionRange = 1
    const val ConsumingRange = 2
    const val ProductionRange = 1

    const val ProbabilityToSpawnOrganics = 0.0f
    const val MutationRate = 0.05f
    const val UnexpectedDeathRate = 0f

    const val InitNumberOfOrganics = 20

    object Debug {
        const val displayGrid = false
        const val displaySourcePosition = false
    }
}