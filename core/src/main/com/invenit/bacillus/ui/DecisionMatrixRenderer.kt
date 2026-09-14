package com.invenit.bacillus.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

/**
 * Draws the Decision Matrix's badge/glyph visual language (#36) with a [ShapeRenderer] the
 * caller owns and has already `begin()`-ed. Shared by [CellDetailsStage]'s live grid and
 * [LegendDialog]'s icon previews, so the two always draw the exact same shapes.
 */
object DecisionMatrixRenderer {

    enum class TriDirection { Up, Down, Left, Right }

    private fun ChevronDirection.toTriDirection(): TriDirection = when (this) {
        ChevronDirection.Up -> TriDirection.Up
        ChevronDirection.Down -> TriDirection.Down
    }

    /** Flat color-filled backing behind a glyph, centered at ([cx], [cy]) and inset from its ([width], [height]) zone. */
    fun drawBadge(shapeRenderer: ShapeRenderer, cx: Float, cy: Float, width: Float, height: Float, inset: Float, color: Color) {
        shapeRenderer.color = color
        shapeRenderer.rect(cx - width / 2f + inset, cy - height / 2f + inset, width - 2 * inset, height - 2 * inset)
    }

    fun drawActionIcon(shapeRenderer: ShapeRenderer, icon: ActionIcon, cx: Float, cy: Float, size: Float, color: Color) {
        when (icon) {
            ActionIcon.RestBars -> drawRestBars(shapeRenderer, cx, cy, size, color)
            ActionIcon.Seek -> drawSeekIcon(shapeRenderer, cx, cy, size, color)
            ActionIcon.Flee -> drawFleeIcon(shapeRenderer, cx, cy, size, color)
            ActionIcon.Explore -> drawTriangleGlyph(shapeRenderer, TriDirection.Up, cx, cy - size * 0.15f, size * 0.8f, color)
            ActionIcon.Random -> drawRandomIcon(shapeRenderer, cx, cy, size, color)
            ActionIcon.Hold -> drawHoldIcon(shapeRenderer, cx, cy, size, color)
            ActionIcon.Release -> drawReleaseIcon(shapeRenderer, cx, cy, size, color)
            ActionIcon.Retain -> drawRetainIcon(shapeRenderer, cx, cy, size, color)
            ActionIcon.Split -> drawSplitIcon(shapeRenderer, cx, cy, size, color)
        }
    }

    private fun drawRestBars(shapeRenderer: ShapeRenderer, cx: Float, cy: Float, size: Float, color: Color) {
        val barWidth = size * 0.22f
        val barHeight = size
        val gap = size * 0.16f
        shapeRenderer.color = color
        shapeRenderer.rect(cx - gap / 2 - barWidth, cy - barHeight / 2, barWidth, barHeight)
        shapeRenderer.rect(cx + gap / 2, cy - barHeight / 2, barWidth, barHeight)
    }

    private fun drawSeekIcon(shapeRenderer: ShapeRenderer, cx: Float, cy: Float, size: Float, color: Color) {
        drawTriangleGlyph(shapeRenderer, TriDirection.Up, cx, cy - size * 0.15f, size * 0.8f, color)
        shapeRenderer.color = color
        shapeRenderer.circle(cx, cy + size * 0.55f, size * 0.12f)
    }

    private fun drawFleeIcon(shapeRenderer: ShapeRenderer, cx: Float, cy: Float, size: Float, color: Color) {
        drawTriangleGlyph(shapeRenderer, TriDirection.Up, cx, cy + size * 0.15f, size * 0.8f, color)
        drawXMark(shapeRenderer, cx, cy - size * 0.45f, size * 0.18f, color)
    }

    fun drawExploreRing(shapeRenderer: ShapeRenderer, cx: Float, cy: Float, size: Float, color: Color) {
        shapeRenderer.color = color
        shapeRenderer.circle(cx, cy + size * 0.55f, size * 0.14f)
    }

    private fun drawRandomIcon(shapeRenderer: ShapeRenderer, cx: Float, cy: Float, size: Float, color: Color) {
        shapeRenderer.color = color
        val h = size * 0.4f
        val w = size * 0.35f
        shapeRenderer.line(cx - w, cy - h, cx, cy - h * 0.2f)
        shapeRenderer.line(cx, cy - h * 0.2f, cx - w * 0.5f, cy + h * 0.3f)
        shapeRenderer.line(cx - w * 0.5f, cy + h * 0.3f, cx + w, cy + h)
        shapeRenderer.triangle(
            cx + w, cy + h,
            cx + w - size * 0.18f, cy + h - size * 0.05f,
            cx + w - size * 0.05f, cy + h - size * 0.18f
        )
    }

    private fun drawHoldIcon(shapeRenderer: ShapeRenderer, cx: Float, cy: Float, size: Float, color: Color) {
        shapeRenderer.color = color
        val s = size * 0.55f
        shapeRenderer.rect(cx - s / 2, cy - s / 2, s, s)
    }

    private fun drawReleaseIcon(shapeRenderer: ShapeRenderer, cx: Float, cy: Float, size: Float, color: Color) {
        drawTriangleGlyph(shapeRenderer, TriDirection.Down, cx, cy, size * 0.8f, color)
    }

    private fun drawRetainIcon(shapeRenderer: ShapeRenderer, cx: Float, cy: Float, size: Float, color: Color) {
        shapeRenderer.color = color
        shapeRenderer.circle(cx, cy, size * 0.4f)
    }

    private fun drawSplitIcon(shapeRenderer: ShapeRenderer, cx: Float, cy: Float, size: Float, color: Color) {
        shapeRenderer.color = color
        val r = size * 0.28f
        val offset = size * 0.3f
        shapeRenderer.circle(cx - offset, cy, r)
        shapeRenderer.circle(cx + offset, cy, r)
    }

    fun drawSensorGlyph(shapeRenderer: ShapeRenderer, glyph: SensorGlyph, cx: Float, cy: Float, size: Float, color: Color) {
        shapeRenderer.color = color
        when (glyph) {
            SensorGlyph.Dot -> shapeRenderer.circle(cx, cy, size / 2)
            SensorGlyph.Diamond -> {
                shapeRenderer.triangle(cx, cy + size / 2, cx - size / 2, cy, cx + size / 2, cy)
                shapeRenderer.triangle(cx, cy - size / 2, cx - size / 2, cy, cx + size / 2, cy)
            }
            SensorGlyph.Triangle -> drawTriangleGlyph(shapeRenderer, TriDirection.Up, cx, cy, size, color)
            SensorGlyph.InvertedTriangle -> drawTriangleGlyph(shapeRenderer, TriDirection.Down, cx, cy, size, color)
            SensorGlyph.Cross -> drawXMark(shapeRenderer, cx, cy, size / 2, color)
            SensorGlyph.Cluster -> {
                val r = size * 0.18f
                shapeRenderer.circle(cx - size * 0.3f, cy, r)
                shapeRenderer.circle(cx + size * 0.3f, cy, r)
                shapeRenderer.circle(cx, cy + size * 0.3f, r)
            }
            SensorGlyph.Spark -> shapeRenderer.rect(cx - size / 2, cy - size / 2, size, size)
        }
    }

    fun drawChevron(shapeRenderer: ShapeRenderer, direction: ChevronDirection, cx: Float, cy: Float, size: Float, color: Color) {
        drawTriangleGlyph(shapeRenderer, direction.toTriDirection(), cx, cy, size, color)
    }

    fun drawJumpGlyph(shapeRenderer: ShapeRenderer, direction: JumpDirection, cx: Float, cy: Float, size: Float, color: Color) {
        when (direction) {
            JumpDirection.Forward -> drawTriangleGlyph(shapeRenderer, TriDirection.Right, cx, cy, size, color)
            JumpDirection.Backward -> drawTriangleGlyph(shapeRenderer, TriDirection.Left, cx, cy, size, color)
            JumpDirection.Neutral -> {
                shapeRenderer.color = color
                shapeRenderer.circle(cx, cy, size * 0.25f)
            }
        }
    }

    fun drawTriangleGlyph(shapeRenderer: ShapeRenderer, direction: TriDirection, cx: Float, cy: Float, size: Float, color: Color) {
        shapeRenderer.color = color
        val h = size / 2
        when (direction) {
            TriDirection.Up -> shapeRenderer.triangle(cx, cy + h, cx - h, cy - h, cx + h, cy - h)
            TriDirection.Down -> shapeRenderer.triangle(cx, cy - h, cx - h, cy + h, cx + h, cy + h)
            TriDirection.Left -> shapeRenderer.triangle(cx - h, cy, cx + h, cy - h, cx + h, cy + h)
            TriDirection.Right -> shapeRenderer.triangle(cx + h, cy, cx - h, cy - h, cx - h, cy + h)
        }
    }

    fun drawXMark(shapeRenderer: ShapeRenderer, cx: Float, cy: Float, radius: Float, color: Color) {
        shapeRenderer.color = color
        shapeRenderer.line(cx - radius, cy + radius, cx - radius / 2, cy + radius / 2)
        shapeRenderer.line(cx + radius, cy + radius, cx + radius / 2, cy + radius / 2)
        shapeRenderer.line(cx + radius, cy - radius, cx + radius / 2, cy - radius / 2)
        shapeRenderer.line(cx - radius, cy - radius, cx - radius / 2, cy - radius / 2)
    }
}
