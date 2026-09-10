package com.invenit.bacillus.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.invenit.bacillus.BacillusGdxGame
import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.*
import com.invenit.bacillus.model.matrix.DecisionMatrix
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Created by viacheslav.mishcheriakov
 * Created 26.11.2021
 */
class CellDetailsStage(val field: Field, val x: Float, val y: Float) : Stage() {

    companion object {
        const val CELL_RADIUS = 50f
        const val ACTION_ROW_HEIGHT = 16f
        const val SENSOR_ROW_HEIGHT = 14f
        const val JUMP_ROW_HEIGHT = 14f
        const val MATRIX_CELL_SIZE = ACTION_ROW_HEIGHT + SENSOR_ROW_HEIGHT + JUMP_ROW_HEIGHT

        const val MATRIX_GAP = 2f
        const val PANEL_GAP = 16f

        const val ACTION_ICON_SIZE = 12f
        const val GLYPH_SIZE = 6f
        const val CHEVRON_SIZE = 6f
        const val GLYPH_INSET = 6f
        const val CHEVRON_INSET = 15f

        // Badge fills behind the action glyph and the sensor+condition glyphs (#36):
        // a flat color block per cell, in the spirit of bacillus-vibe's per-cell color
        // fill, so a state's category reads at a glance across the whole grid.
        const val ACTION_BADGE_INSET = 1f
        const val SENSOR_BADGE_WIDTH = 22f
        const val SENSOR_BADGE_INSET = 1f

        const val LEGEND_BUTTON_SIZE = 18f

        private val NeutralCellColor = Color(0.16f, 0.16f, 0.2f, 1f)
        private val CurrentStateColor = Color(0.45f, 0.38f, 0.1f, 1f)
    }

    private enum class TriDirection { Up, Down, Left, Right }

    private class MatrixCell(
        val container: Table,
        val actionZone: Actor,
        val sensorValueLabel: Label,
        val jumpValueLabel: Label,
        val tooltip: TextTooltip
    )

    private var cell: Organic? = null
    private val shapeRenderer = ShapeRenderer()
    private val skin: Skin = Skin(Gdx.files.internal("uiskin.json"))
    private val table = Table()
    private val matrixTable = Table()
    private val matrixPanel = Table()
    private val legendDialog = LegendDialog(skin)
    private val neutralCellBackground = coloredDrawable(NeutralCellColor)
    private val currentStateBackground = coloredDrawable(CurrentStateColor)

    private val positionLabel: Label
    private val energyValueLabel: Label
    private val sizeValueLabel: Label
    private val ageValueLabel: Label
    private val dmMutationsValueLabel: Label
    private val traitMutationsValueLabel: Label
    private val matrixCells: List<MatrixCell>

    init {
        table.setPosition(x + 2 * CELL_RADIUS + 10f, y - CELL_RADIUS)
        table.align(Align.left)

        table.add(Label("Position:", skin)).left()
        positionLabel = Label("", skin)
        table.add(positionLabel).left().padLeft(10f).row()

        table.add(Label("Energy:", skin)).left()
        energyValueLabel = Label("", skin)
        table.add(energyValueLabel).left().padLeft(10f).row()

        table.add(Label("Size:", skin)).left()
        sizeValueLabel = Label("", skin)
        table.add(sizeValueLabel).left().padLeft(10f).row()

        table.add(Label("Age:", skin)).left()
        ageValueLabel = Label("", skin)
        table.add(ageValueLabel).left().padLeft(10f).row()

        // Per-cell mutation counts (#12): how many DM/trait mutations this
        // cell's genome carries since the founder genome that seeded the
        // population - i.e. this cell's "distance" from that original.
        table.add(Label("DM Mutations:", skin)).left()
        dmMutationsValueLabel = Label("", skin)
        table.add(dmMutationsValueLabel).left().padLeft(10f).row()

        table.add(Label("Trait Mutations:", skin)).left()
        traitMutationsValueLabel = Label("", skin)
        table.add(traitMutationsValueLabel).left().padLeft(10f).row()

        addActor(table)

        matrixCells = List(DecisionMatrix.SIZE) {
            val container = Table()
            container.background = neutralCellBackground

            val actionZone = Actor()
            container.add(actionZone).height(ACTION_ROW_HEIGHT).fillX().expandX().row()

            val sensorValueLabel = Label("", skin)
            sensorValueLabel.setAlignment(Align.right)
            sensorValueLabel.setFontScale(0.5f)
            container.add(sensorValueLabel).height(SENSOR_ROW_HEIGHT).fillX().expandX().padRight(3f).row()

            val jumpValueLabel = Label("", skin)
            jumpValueLabel.setAlignment(Align.right)
            jumpValueLabel.setFontScale(0.5f)
            container.add(jumpValueLabel).height(JUMP_ROW_HEIGHT).fillX().expandX().padRight(3f)

            val tooltip = TextTooltip("", skin)
            container.addListener(tooltip)

            matrixTable.add(container).size(MATRIX_CELL_SIZE, MATRIX_CELL_SIZE).pad(MATRIX_GAP)
            if ((it + 1) % DecisionMatrix.DIMENSION == 0) {
                matrixTable.row()
            }

            MatrixCell(container, actionZone, sensorValueLabel, jumpValueLabel, tooltip)
        }
        matrixTable.pack()

        val legendButton = TextButton("?", skin)
        legendButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, clickX: Float, clickY: Float) {
                legendDialog.show(this@CellDetailsStage)
            }
        })

        val matrixHeader = Table()
        matrixHeader.add(Label("Decision Matrix", skin)).left().expandX()
        matrixHeader.add(legendButton).right().size(LEGEND_BUTTON_SIZE)

        matrixPanel.add(matrixHeader).fillX().padBottom(4f).row()
        matrixPanel.add(matrixTable)
        matrixPanel.pack()
        matrixPanel.setPosition(x, y - 2 * CELL_RADIUS - PANEL_GAP, Align.topLeft)
        addActor(matrixPanel)
    }

    override fun dispose() {
        super.dispose()
        shapeRenderer.dispose()
        legendDialog.dispose()
        skin.dispose()
        neutralCellBackground.region.texture.dispose()
        currentStateBackground.region.texture.dispose()
    }

    override fun act(delta: Float) {
        super.act(delta)

        if (cell != null && field[cell!!.position] != cell) {
            cell = null
        }

        if (cell != null) {
            table.isVisible = true
            matrixPanel.isVisible = true
            val position = cell!!.position
            positionLabel.setText("[${position.x}, ${position.y}]")
            energyValueLabel.setText(cell!!.energy.toString())
            sizeValueLabel.setText(cell!!.size.toString())
            ageValueLabel.setText(cell!!.age.toString())
            dmMutationsValueLabel.setText(cell!!.dna.dmMutationCount.toString())
            traitMutationsValueLabel.setText(cell!!.dna.traitMutationCount.toString())
            updateMatrixCells(matrixCells)
        } else {
            table.isVisible = false
            matrixPanel.isVisible = false
        }
    }

    private fun updateMatrixCells(matrixCells: List<MatrixCell>) {
        val decisionMatrix = cell!!.dna.decisionMatrix
        val currentState = cell!!.currentState
        for (i in matrixCells.indices) {
            val matrixCell = matrixCells[i]
            val instruction = decisionMatrix[i]
            matrixCell.sensorValueLabel.setText("%d".format(instruction.threshold))
            matrixCell.jumpValueLabel.setText(abs(instruction.jumpOffset).toString())
            matrixCell.tooltip.actor.setText(instruction.toDisplayText())
            matrixCell.container.background = if (i == currentState) currentStateBackground else neutralCellBackground
        }
    }

    override fun draw() {
        super.draw()

        if (cell != null) {
            shapeRenderer.projectionMatrix = camera.combined
            Gdx.gl.glEnable(GL30.GL_BLEND)
            Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA)
            draw(cell!!)
            drawDecisionMatrix(cell!!)
            Gdx.gl.glDisable(GL30.GL_BLEND)

        }
    }

    private fun draw(cell: Organic) {
        val alpha = cell.getAlpha()
        val radius = cell.getRadius() * CELL_RADIUS

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        shapeRenderer.color = Color(cell.dna.produce.color)
            .sub(BacillusGdxGame.TransparentMask)
            .add(0f, 0f, 0f, sqrt(alpha))
        shapeRenderer.circle(
            x + CELL_RADIUS,
            y - CELL_RADIUS,
            radius
        )

        shapeRenderer.color = Color(cell.body.color)
            .sub(BacillusGdxGame.TransparentMask)
            .add(0f, 0f, 0f, alpha)
        shapeRenderer.circle(
            x + CELL_RADIUS,
            y - CELL_RADIUS,
            radius * 4 / 5
        )

        shapeRenderer.color = Color(cell.dna.consume.color)
            .sub(BacillusGdxGame.TransparentMask)
            .add(0f, 0f, 0f, sqrt(alpha))
        shapeRenderer.circle(
            x + CELL_RADIUS,
            y - CELL_RADIUS,
            radius * 2 / 5
        )

        val toxinColor = Color(cell.dna.toxin.color)
            .sub(BacillusGdxGame.TransparentMask)
            .add(0f, 0f, 0f, sqrt(alpha))
        drawXMark(x + CELL_RADIUS, y - CELL_RADIUS, radius, toxinColor)
        shapeRenderer.end()
    }

    private fun drawXMark(cx: Float, cy: Float, radius: Float, color: Color) {
        shapeRenderer.color = color
        shapeRenderer.line(cx - radius, cy + radius, cx - radius / 2, cy + radius / 2)
        shapeRenderer.line(cx + radius, cy + radius, cx + radius / 2, cy + radius / 2)
        shapeRenderer.line(cx + radius, cy - radius, cx + radius / 2, cy - radius / 2)
        shapeRenderer.line(cx - radius, cy - radius, cx - radius / 2, cy - radius / 2)
    }

    private fun drawDecisionMatrix(cell: Organic) {
        val decisionMatrix = cell.dna.decisionMatrix

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (i in matrixCells.indices) {
            val matrixCell = matrixCells[i]
            val instruction = decisionMatrix[i]
            val icon = instruction.action.toIcon()

            val actionCenter = matrixCell.actionZone.localToStageCoordinates(
                Vector2(matrixCell.actionZone.width / 2f, matrixCell.actionZone.height / 2f)
            )
            drawBadge(
                actionCenter.x, actionCenter.y,
                matrixCell.actionZone.width, matrixCell.actionZone.height,
                ACTION_BADGE_INSET, icon.badgeColor()
            )
            drawActionIcon(icon, actionCenter.x, actionCenter.y)

            val sensorAnchor = matrixCell.sensorValueLabel.localToStageCoordinates(
                Vector2(0f, matrixCell.sensorValueLabel.height / 2f)
            )
            drawBadge(
                sensorAnchor.x + SENSOR_BADGE_WIDTH / 2f, sensorAnchor.y,
                SENSOR_BADGE_WIDTH, SENSOR_ROW_HEIGHT,
                SENSOR_BADGE_INSET, instruction.sensor.badgeColor()
            )
            drawSensorGlyph(instruction.sensor.toGlyph(), sensorAnchor.x + GLYPH_INSET, sensorAnchor.y, IconStrokeColor)
            drawTriangleGlyph(
                instruction.comparator.toChevron().toTriDirection(),
                sensorAnchor.x + CHEVRON_INSET, sensorAnchor.y, CHEVRON_SIZE, IconStrokeColor
            )

            val jumpAnchor = matrixCell.jumpValueLabel.localToStageCoordinates(
                Vector2(0f, matrixCell.jumpValueLabel.height / 2f)
            )
            drawJumpGlyph(instruction.jumpOffset.toJumpDirection(), jumpAnchor.x + GLYPH_INSET, jumpAnchor.y)
        }
        shapeRenderer.end()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        matrixCells.forEachIndexed { index, matrixCell ->
            if (decisionMatrix[index].action.toIcon() == ActionIcon.Explore) {
                val actionCenter = matrixCell.actionZone.localToStageCoordinates(
                    Vector2(matrixCell.actionZone.width / 2f, matrixCell.actionZone.height / 2f)
                )
                drawExploreRing(actionCenter.x, actionCenter.y)
            }
        }
        shapeRenderer.end()
    }

    /** Flat color-filled backing behind a glyph (#36), centered at ([cx], [cy]) and inset from its ([width], [height]) zone. */
    private fun drawBadge(cx: Float, cy: Float, width: Float, height: Float, inset: Float, color: Color) {
        shapeRenderer.color = color
        shapeRenderer.rect(cx - width / 2f + inset, cy - height / 2f + inset, width - 2 * inset, height - 2 * inset)
    }

    private fun drawActionIcon(icon: ActionIcon, cx: Float, cy: Float) {
        when (icon) {
            ActionIcon.RestBars -> drawRestBars(cx, cy, IconStrokeColor)
            ActionIcon.Seek -> drawSeekIcon(cx, cy, IconStrokeColor)
            ActionIcon.Flee -> drawFleeIcon(cx, cy, IconStrokeColor)
            ActionIcon.Explore -> drawTriangleGlyph(TriDirection.Up, cx, cy - ACTION_ICON_SIZE * 0.15f, ACTION_ICON_SIZE * 0.8f, IconStrokeColor)
            ActionIcon.Random -> drawRandomIcon(cx, cy, IconStrokeColor)
            ActionIcon.Hold -> drawHoldIcon(cx, cy, IconStrokeColor)
            ActionIcon.Release -> drawReleaseIcon(cx, cy, IconStrokeColor)
            ActionIcon.Retain -> drawRetainIcon(cx, cy, IconStrokeColor)
            ActionIcon.Split -> drawSplitIcon(cx, cy, IconStrokeColor)
        }
    }

    private fun drawRestBars(cx: Float, cy: Float, color: Color) {
        val barWidth = ACTION_ICON_SIZE * 0.22f
        val barHeight = ACTION_ICON_SIZE
        val gap = ACTION_ICON_SIZE * 0.16f
        shapeRenderer.color = color
        shapeRenderer.rect(cx - gap / 2 - barWidth, cy - barHeight / 2, barWidth, barHeight)
        shapeRenderer.rect(cx + gap / 2, cy - barHeight / 2, barWidth, barHeight)
    }

    private fun drawSeekIcon(cx: Float, cy: Float, color: Color) {
        drawTriangleGlyph(TriDirection.Up, cx, cy - ACTION_ICON_SIZE * 0.15f, ACTION_ICON_SIZE * 0.8f, color)
        shapeRenderer.color = color
        shapeRenderer.circle(cx, cy + ACTION_ICON_SIZE * 0.55f, ACTION_ICON_SIZE * 0.12f)
    }

    private fun drawFleeIcon(cx: Float, cy: Float, color: Color) {
        drawTriangleGlyph(TriDirection.Up, cx, cy + ACTION_ICON_SIZE * 0.15f, ACTION_ICON_SIZE * 0.8f, color)
        drawXMark(cx, cy - ACTION_ICON_SIZE * 0.45f, ACTION_ICON_SIZE * 0.18f, color)
    }

    private fun drawExploreRing(cx: Float, cy: Float) {
        shapeRenderer.color = IconStrokeColor
        shapeRenderer.circle(cx, cy + ACTION_ICON_SIZE * 0.55f, ACTION_ICON_SIZE * 0.14f)
    }

    private fun drawRandomIcon(cx: Float, cy: Float, color: Color) {
        shapeRenderer.color = color
        val h = ACTION_ICON_SIZE * 0.4f
        val w = ACTION_ICON_SIZE * 0.35f
        shapeRenderer.line(cx - w, cy - h, cx, cy - h * 0.2f)
        shapeRenderer.line(cx, cy - h * 0.2f, cx - w * 0.5f, cy + h * 0.3f)
        shapeRenderer.line(cx - w * 0.5f, cy + h * 0.3f, cx + w, cy + h)
        shapeRenderer.triangle(
            cx + w, cy + h,
            cx + w - ACTION_ICON_SIZE * 0.18f, cy + h - ACTION_ICON_SIZE * 0.05f,
            cx + w - ACTION_ICON_SIZE * 0.05f, cy + h - ACTION_ICON_SIZE * 0.18f
        )
    }

    private fun drawHoldIcon(cx: Float, cy: Float, color: Color) {
        shapeRenderer.color = color
        val size = ACTION_ICON_SIZE * 0.55f
        shapeRenderer.rect(cx - size / 2, cy - size / 2, size, size)
    }

    private fun drawReleaseIcon(cx: Float, cy: Float, color: Color) {
        drawTriangleGlyph(TriDirection.Down, cx, cy, ACTION_ICON_SIZE * 0.8f, color)
    }

    private fun drawRetainIcon(cx: Float, cy: Float, color: Color) {
        shapeRenderer.color = color
        shapeRenderer.circle(cx, cy, ACTION_ICON_SIZE * 0.4f)
    }

    private fun drawSplitIcon(cx: Float, cy: Float, color: Color) {
        shapeRenderer.color = color
        val r = ACTION_ICON_SIZE * 0.28f
        val offset = ACTION_ICON_SIZE * 0.3f
        shapeRenderer.circle(cx - offset, cy, r)
        shapeRenderer.circle(cx + offset, cy, r)
    }

    private fun drawSensorGlyph(glyph: SensorGlyph, cx: Float, cy: Float, color: Color) {
        shapeRenderer.color = color
        when (glyph) {
            SensorGlyph.Dot -> shapeRenderer.circle(cx, cy, GLYPH_SIZE / 2)
            SensorGlyph.Diamond -> {
                shapeRenderer.triangle(cx, cy + GLYPH_SIZE / 2, cx - GLYPH_SIZE / 2, cy, cx + GLYPH_SIZE / 2, cy)
                shapeRenderer.triangle(cx, cy - GLYPH_SIZE / 2, cx - GLYPH_SIZE / 2, cy, cx + GLYPH_SIZE / 2, cy)
            }
            SensorGlyph.Triangle -> drawTriangleGlyph(TriDirection.Up, cx, cy, GLYPH_SIZE, color)
            SensorGlyph.InvertedTriangle -> drawTriangleGlyph(TriDirection.Down, cx, cy, GLYPH_SIZE, color)
            SensorGlyph.Cross -> drawXMark(cx, cy, GLYPH_SIZE / 2, color)
            SensorGlyph.Cluster -> {
                val r = GLYPH_SIZE * 0.18f
                shapeRenderer.circle(cx - GLYPH_SIZE * 0.3f, cy, r)
                shapeRenderer.circle(cx + GLYPH_SIZE * 0.3f, cy, r)
                shapeRenderer.circle(cx, cy + GLYPH_SIZE * 0.3f, r)
            }
            SensorGlyph.Spark -> shapeRenderer.rect(cx - GLYPH_SIZE / 2, cy - GLYPH_SIZE / 2, GLYPH_SIZE, GLYPH_SIZE)
        }
    }

    private fun ChevronDirection.toTriDirection(): TriDirection = when (this) {
        ChevronDirection.Up -> TriDirection.Up
        ChevronDirection.Down -> TriDirection.Down
    }

    private fun drawJumpGlyph(direction: JumpDirection, cx: Float, cy: Float) {
        when (direction) {
            JumpDirection.Forward -> drawTriangleGlyph(TriDirection.Right, cx, cy, CHEVRON_SIZE, JumpGlyphColor)
            JumpDirection.Backward -> drawTriangleGlyph(TriDirection.Left, cx, cy, CHEVRON_SIZE, JumpGlyphColor)
            JumpDirection.Neutral -> {
                shapeRenderer.color = JumpGlyphColor
                shapeRenderer.circle(cx, cy, CHEVRON_SIZE * 0.25f)
            }
        }
    }

    private fun drawTriangleGlyph(direction: TriDirection, cx: Float, cy: Float, size: Float, color: Color) {
        shapeRenderer.color = color
        val h = size / 2
        when (direction) {
            TriDirection.Up -> shapeRenderer.triangle(cx, cy + h, cx - h, cy - h, cx + h, cy - h)
            TriDirection.Down -> shapeRenderer.triangle(cx, cy - h, cx - h, cy + h, cx + h, cy + h)
            TriDirection.Left -> shapeRenderer.triangle(cx - h, cy, cx + h, cy - h, cx + h, cy + h)
            TriDirection.Right -> shapeRenderer.triangle(cx + h, cy, cx - h, cy - h, cx - h, cy + h)
        }
    }

    private fun Organic.getAlpha() =
        0.3f + 0.7f * (this.energy.toFloat() / this.size.toFloat())

    private fun Something.getRadius() =
        0.25f + 0.75f * (this.size.toFloat() / Settings.MaxSize)

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val position = fromDisplay(screenX, screenY)

        val something = field[position]
        if (something == null || something is Mineral) {
            cell = null
        } else if (something is Organic) {
            cell = something
        }

        return super.touchUp(screenX, screenY, pointer, button)
    }

    private fun fromDisplay(screenX: Int, screenY: Int): Point {
        val touchPoint = Vector2(screenX.toFloat(), screenY.toFloat())
        viewport.unproject(touchPoint)
        return Point(
            touchPoint.x.toInt() / Settings.CellSize,
            touchPoint.y.toInt() / Settings.CellSize
        )
    }
}