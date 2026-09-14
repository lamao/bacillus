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
    }

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
        DecisionMatrixRenderer.drawXMark(shapeRenderer, x + CELL_RADIUS, y - CELL_RADIUS, radius, toxinColor)
        shapeRenderer.end()
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
            DecisionMatrixRenderer.drawBadge(
                shapeRenderer, actionCenter.x, actionCenter.y,
                matrixCell.actionZone.width, matrixCell.actionZone.height,
                ACTION_BADGE_INSET, icon.badgeColor()
            )
            DecisionMatrixRenderer.drawActionIcon(shapeRenderer, icon, actionCenter.x, actionCenter.y, ACTION_ICON_SIZE, IconStrokeColor)

            val sensorAnchor = matrixCell.sensorValueLabel.localToStageCoordinates(
                Vector2(0f, matrixCell.sensorValueLabel.height / 2f)
            )
            DecisionMatrixRenderer.drawBadge(
                shapeRenderer, sensorAnchor.x + SENSOR_BADGE_WIDTH / 2f, sensorAnchor.y,
                SENSOR_BADGE_WIDTH, SENSOR_ROW_HEIGHT,
                SENSOR_BADGE_INSET, instruction.sensor.badgeColor()
            )
            DecisionMatrixRenderer.drawSensorGlyph(
                shapeRenderer, instruction.sensor.toGlyph(), sensorAnchor.x + GLYPH_INSET, sensorAnchor.y, GLYPH_SIZE, IconStrokeColor
            )
            DecisionMatrixRenderer.drawChevron(
                shapeRenderer, instruction.comparator.toChevron(),
                sensorAnchor.x + CHEVRON_INSET, sensorAnchor.y, CHEVRON_SIZE, IconStrokeColor
            )

            val jumpAnchor = matrixCell.jumpValueLabel.localToStageCoordinates(
                Vector2(0f, matrixCell.jumpValueLabel.height / 2f)
            )
            DecisionMatrixRenderer.drawJumpGlyph(
                shapeRenderer, instruction.jumpOffset.toJumpDirection(), jumpAnchor.x + GLYPH_INSET, jumpAnchor.y, CHEVRON_SIZE, JumpGlyphColor
            )
        }
        shapeRenderer.end()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        matrixCells.forEachIndexed { index, matrixCell ->
            if (decisionMatrix[index].action.toIcon() == ActionIcon.Explore) {
                val actionCenter = matrixCell.actionZone.localToStageCoordinates(
                    Vector2(matrixCell.actionZone.width / 2f, matrixCell.actionZone.height / 2f)
                )
                DecisionMatrixRenderer.drawExploreRing(shapeRenderer, actionCenter.x, actionCenter.y, ACTION_ICON_SIZE, IconStrokeColor)
            }
        }
        shapeRenderer.end()
    }

    private fun Organic.getAlpha() =
        0.3f + 0.7f * (this.energy.toFloat() / this.size.toFloat())

    private fun Something.getRadius() =
        0.25f + 0.75f * (this.size.toFloat() / Settings.MaxSize)

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val position = fromDisplay(screenX, screenY)

        // Clicks landing on this stage's own UI (e.g. the legend button, #36) fall outside
        // the field grid; field[position] has no bounds check, so skip it here rather than
        // let it throw and abort before super.touchUp() gets to dispatch the click to actors.
        if (!field.isOutside(position)) {
            val something = field[position]
            if (something == null || something is Mineral) {
                cell = null
            } else if (something is Organic) {
                cell = something
            }
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