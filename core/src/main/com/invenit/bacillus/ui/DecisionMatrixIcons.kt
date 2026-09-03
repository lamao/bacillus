package com.invenit.bacillus.ui

import com.invenit.bacillus.model.matrix.Action
import com.invenit.bacillus.model.matrix.Comparator
import com.invenit.bacillus.model.matrix.Sensor

/**
 * Which vector glyph a [CellDetailsStage] DecisionMatrix grid cell draws
 * for each part of an [com.invenit.bacillus.model.matrix.Instruction]
 * (#9 icon rework). Pure selection logic only — the actual drawing lives
 * in [CellDetailsStage].
 */
enum class ActionIcon { RestBars, Seek, Flee, Explore, Random, Hold, Release, Hoard, Split }
enum class SensorGlyph { Dot, Diamond, Triangle, InvertedTriangle, Cross, Cluster, Spark }
enum class ChevronDirection { Up, Down }
enum class JumpDirection { Forward, Backward, Neutral }

fun Action.toIcon(): ActionIcon = when (category) {
    Action.Category.Rest -> ActionIcon.RestBars
    Action.Category.Split -> ActionIcon.Split
    Action.Category.Move, Action.Category.Produce -> mode!!.toIcon()
}

private fun Action.Mode.toIcon(): ActionIcon = when (this) {
    Action.Mode.TowardConsume -> ActionIcon.Seek
    Action.Mode.AwayFromToxin -> ActionIcon.Flee
    Action.Mode.TowardOpenSpace -> ActionIcon.Explore
    Action.Mode.Random -> ActionIcon.Random
    Action.Mode.Hold -> ActionIcon.Hold
    Action.Mode.Release -> ActionIcon.Release
    Action.Mode.Hoard -> ActionIcon.Hoard
}

fun Sensor.toGlyph(): SensorGlyph = when (this) {
    Sensor.FoodDistance -> SensorGlyph.Dot
    Sensor.EnergyRatio -> SensorGlyph.Diamond
    Sensor.ToxinDistance -> SensorGlyph.Triangle
    Sensor.SizeRatio -> SensorGlyph.InvertedTriangle
    Sensor.Age -> SensorGlyph.Cross
    Sensor.Crowding -> SensorGlyph.Cluster
    Sensor.Random -> SensorGlyph.Spark
}

fun Comparator.toChevron(): ChevronDirection = when (this) {
    Comparator.GreaterThanOrEqual -> ChevronDirection.Up
    Comparator.LessThan -> ChevronDirection.Down
}

fun Int.toJumpDirection(): JumpDirection = when {
    this > 0 -> JumpDirection.Forward
    this < 0 -> JumpDirection.Backward
    else -> JumpDirection.Neutral
}
