package com.invenit.bacillus.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.invenit.bacillus.BacillusGdxGame
import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.*
import com.invenit.bacillus.model.matrix.Action
import com.invenit.bacillus.model.matrix.DecisionMatrix
import com.invenit.bacillus.model.matrix.Sensor
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

        private val NeutralCellColor = Color(0.16f, 0.16f, 0.2f, 1f)
        private val CurrentStateColor = Color(0.45f, 0.38f, 0.1f, 1f)
        private val RestIconColor = Color(0.55f, 0.6f, 0.75f, 1f)
        private val SeekColor = Color(0.3f, 0.75f, 0.3f, 1f)
        private val FleeColor = Color(0.85f, 0.35f, 0.25f, 1f)
        private val ExploreColor = Color(0.3f, 0.75f, 0.8f, 1f)
        private val RandomColor = Color(0.7f, 0.4f, 0.85f, 1f)
        private val HoldColor = Color(0.6f, 0.6f, 0.6f, 1f)
        private val FoodGlyphColor = SeekColor
        private val EnergyGlyphColor = Color(0.85f, 0.65f, 0.15f, 1f)
        private val JumpGlyphColor = Color(0.8f, 0.8f, 0.8f, 1f)
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
    private val neutralCellBackground = coloredDrawable(NeutralCellColor)
    private val currentStateBackground = coloredDrawable(CurrentStateColor)

    private val positionLabel: Label
    private val energyValueLabel: Label
    private val sizeValueLabel: Label
    private val ageValueLabel: Label
    private val mobileValueLabel: Label
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

        table.add(Label("Mobile:", skin)).left()
        mobileValueLabel = Label("", skin)
        table.add(mobileValueLabel).left().padLeft(10f).row()

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
        matrixTable.setPosition(x, y - 2 * CELL_RADIUS - PANEL_GAP, Align.topLeft)
        addActor(matrixTable)
    }

    private fun coloredDrawable(color: Color): TextureRegionDrawable {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(color)
        pixmap.fill()
        val texture = Texture(pixmap)
        pixmap.dispose()
        return TextureRegionDrawable(TextureRegion(texture))
    }

    override fun dispose() {
        super.dispose()
        shapeRenderer.dispose()
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
            matrixTable.isVisible = true
            val position = cell!!.position
            positionLabel.setText("[${position.x}, ${position.y}]")
            energyValueLabel.setText(cell!!.energy.toString())
            sizeValueLabel.setText(cell!!.size.toString())
            ageValueLabel.setText(cell!!.age.toString())
            mobileValueLabel.setText(if (cell!!.chosenAction.category == Action.Category.Move) "true" else "false")
            updateMatrixCells(matrixCells)
        } else {
            table.isVisible = false
            matrixTable.isVisible = false
        }
    }

    private fun updateMatrixCells(matrixCells: List<MatrixCell>) {
        val decisionMatrix = cell!!.dna.decisionMatrix
        val currentState = cell!!.currentState
        for (i in matrixCells.indices) {
            val matrixCell = matrixCells[i]
            val instruction = decisionMatrix[i]
            matrixCell.sensorValueLabel.setText("%.2f".format(instruction.threshold))
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

            val actionCenter = matrixCell.actionZone.localToStageCoordinates(
                Vector2(matrixCell.actionZone.width / 2f, matrixCell.actionZone.height / 2f)
            )
            drawActionIcon(instruction.action.toIcon(), actionCenter.x, actionCenter.y)

            val sensorColor = sensorGlyphColor(instruction.sensor)
            val sensorAnchor = matrixCell.sensorValueLabel.localToStageCoordinates(
                Vector2(0f, matrixCell.sensorValueLabel.height / 2f)
            )
            drawSensorGlyph(instruction.sensor.toGlyph(), sensorAnchor.x + GLYPH_INSET, sensorAnchor.y, sensorColor)
            drawTriangleGlyph(
                instruction.comparator.toChevron().toTriDirection(),
                sensorAnchor.x + CHEVRON_INSET, sensorAnchor.y, CHEVRON_SIZE, sensorColor
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

    private fun drawActionIcon(icon: ActionIcon, cx: Float, cy: Float) {
        when (icon) {
            ActionIcon.RestBars -> drawRestBars(cx, cy, RestIconColor)
            ActionIcon.Seek -> drawSeekIcon(cx, cy, SeekColor)
            ActionIcon.Flee -> drawFleeIcon(cx, cy, FleeColor)
            ActionIcon.Explore -> drawTriangleGlyph(TriDirection.Up, cx, cy - ACTION_ICON_SIZE * 0.15f, ACTION_ICON_SIZE * 0.8f, ExploreColor)
            ActionIcon.Random -> drawRandomIcon(cx, cy, RandomColor)
            ActionIcon.Hold -> drawHoldIcon(cx, cy, HoldColor)
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
        shapeRenderer.color = ExploreColor
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

    private fun drawSensorGlyph(glyph: SensorGlyph, cx: Float, cy: Float, color: Color) {
        shapeRenderer.color = color
        when (glyph) {
            SensorGlyph.Dot -> shapeRenderer.circle(cx, cy, GLYPH_SIZE / 2)
            SensorGlyph.Diamond -> {
                shapeRenderer.triangle(cx, cy + GLYPH_SIZE / 2, cx - GLYPH_SIZE / 2, cy, cx + GLYPH_SIZE / 2, cy)
                shapeRenderer.triangle(cx, cy - GLYPH_SIZE / 2, cx - GLYPH_SIZE / 2, cy, cx + GLYPH_SIZE / 2, cy)
            }
        }
    }

    private fun sensorGlyphColor(sensor: Sensor): Color = when (sensor) {
        Sensor.FoodDistance -> FoodGlyphColor
        Sensor.EnergyRatio -> EnergyGlyphColor
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