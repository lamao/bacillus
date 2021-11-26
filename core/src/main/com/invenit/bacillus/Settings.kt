package com.invenit.bacillus

import kotlin.math.pow

/**
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */
object Settings {

    const val Width = 800
    const val Height = 800
    const val UiWidth = 300
    const val TotalWidth = Width + UiWidth

    const val CellSize = 10

    const val GridWidth = Width / CellSize
    const val GridHeight = Height / CellSize

    var TicDelaySeconds = .02f
    val SmoothAnimation: Boolean
        get() = TicDelaySeconds > 0.2f

    var BiteYield = 200
    var SunYield = 25
    var MineralsYield = 10
    fun correctedMineralsYield(amount: Float, distance: Int): Float =
        amount

    fun toxinDamageFunction(amount: Float, distance: Int): Float =
        amount / 2f.pow(distance - 1)

    var MoveConsumption = 10
    var PermanentConsumption = 10

    var ProductionPerformance = 0.1f
    var MineralDegradation = 3

    var DefaultSize = 750
    var ReproductionThreshold = 2000
    val MaxSize: Int
        get() = ReproductionThreshold + BiteYield

    var MaxAge = 1500

    const val ReturnHealthWhenReproductionFails = 0.5f


    const val ReproductionRange = 1
    const val VisionRange = 1
    const val ConsumingRange = 2
    const val ProductionRange = 1
    const val ToxinRange = 2

    const val ProbabilityToSpawnOrganics = 0.0f
    var MutationRate = 0.01f
    const val UnexpectedDeathRate = 0f

    const val InitNumberOfOrganics = 20

    object Debug {
        var displayGrid = false
        var displaySourcePosition = false
    }
}