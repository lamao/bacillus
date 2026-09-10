package com.invenit.bacillus.ui

import com.badlogic.gdx.graphics.Color
import com.invenit.bacillus.model.matrix.Action
import com.invenit.bacillus.model.matrix.Comparator
import com.invenit.bacillus.model.matrix.Sensor

/**
 * Which vector glyph a [CellDetailsStage] DecisionMatrix grid cell draws
 * for each part of an [com.invenit.bacillus.model.matrix.Instruction]
 * (#9 icon rework). Pure selection logic only — the actual drawing lives
 * in [CellDetailsStage].
 */
enum class ActionIcon { RestBars, Seek, Flee, Explore, Random, Hold, Release, Retain, Split }
enum class SensorGlyph { Dot, Diamond, Triangle, InvertedTriangle, Cross, Cluster, Spark }
enum class ChevronDirection { Up, Down }
enum class JumpDirection { Forward, Backward, Neutral }

/**
 * Flat per-action badge color (#36 redesign, matching bacillus-vibe's per-cell color
 * fill): drawn as the action glyph's backing so its category/mode reads at a glance
 * across the grid, rather than requiring the glyph's own stroke to carry the color.
 */
val RestIconColor: Color = Color(0.55f, 0.6f, 0.75f, 1f)
val SeekColor: Color = Color(0.3f, 0.75f, 0.3f, 1f)
val FleeColor: Color = Color(0.85f, 0.35f, 0.25f, 1f)
val ExploreColor: Color = Color(0.3f, 0.75f, 0.8f, 1f)
val RandomColor: Color = Color(0.7f, 0.4f, 0.85f, 1f)
val HoldColor: Color = Color(0.6f, 0.6f, 0.6f, 1f)
val ReleaseColor: Color = Color(0.85f, 0.55f, 0.2f, 1f)
val RetainColor: Color = Color(0.55f, 0.45f, 0.2f, 1f)
val SplitColor: Color = Color(0.3f, 0.85f, 0.55f, 1f)

/** Per-sensor badge color (#36 redesign), given the same treatment as the action badges. */
val FoodGlyphColor: Color = SeekColor
val EnergyGlyphColor: Color = Color(0.85f, 0.65f, 0.15f, 1f)
val ToxinGlyphColor: Color = FleeColor
val SizeGlyphColor: Color = Color(0.5f, 0.6f, 0.9f, 1f)
val AgeGlyphColor: Color = Color(0.75f, 0.75f, 0.75f, 1f)
val CrowdingGlyphColor: Color = ExploreColor
val RandomGlyphColor: Color = RandomColor

/** Stroke color for glyphs drawn on top of a colored badge (#36) so they stay legible regardless of the badge's hue. */
val IconStrokeColor: Color = Color(0.07f, 0.07f, 0.09f, 1f)

/** Jump glyphs sit directly on the cell's plain background rather than a color badge, so they keep a neutral accent color. */
val JumpGlyphColor: Color = Color(0.8f, 0.8f, 0.8f, 1f)

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
    Action.Mode.Retain -> ActionIcon.Retain
}

/** Badge fill color for [icon] (#36) — what makes an action's category/mode readable at a glance in the Decision Matrix grid. */
fun ActionIcon.badgeColor(): Color = when (this) {
    ActionIcon.RestBars -> RestIconColor
    ActionIcon.Seek -> SeekColor
    ActionIcon.Flee -> FleeColor
    ActionIcon.Explore -> ExploreColor
    ActionIcon.Random -> RandomColor
    ActionIcon.Hold -> HoldColor
    ActionIcon.Release -> ReleaseColor
    ActionIcon.Retain -> RetainColor
    ActionIcon.Split -> SplitColor
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

/** Badge fill color for [sensor] (#36), giving the sensor+condition row the same glanceable treatment as the action badge. */
fun Sensor.badgeColor(): Color = when (this) {
    Sensor.FoodDistance -> FoodGlyphColor
    Sensor.EnergyRatio -> EnergyGlyphColor
    Sensor.ToxinDistance -> ToxinGlyphColor
    Sensor.SizeRatio -> SizeGlyphColor
    Sensor.Age -> AgeGlyphColor
    Sensor.Crowding -> CrowdingGlyphColor
    Sensor.Random -> RandomGlyphColor
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
