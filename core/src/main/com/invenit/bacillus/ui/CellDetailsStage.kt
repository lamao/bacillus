package com.invenit.bacillus.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.invenit.bacillus.BacillusGdxGame
import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.*
import kotlin.math.sqrt

/**
 * Created by viacheslav.mishcheriakov
 * Created 26.11.2021
 */
class CellDetailsStage(val field: Field, val x: Float, val y: Float) : Stage() {

    companion object {
        const val CELL_RADIUS = 50f
    }

    private var cell: Organic? = null
    private val shapeRenderer = ShapeRenderer()
    private val skin: Skin = Skin(Gdx.files.internal("uiskin.json"))
    private val table = Table()

    private val positionLabel: Label
    private val energyValueLabel: Label
    private val sizeValueLabel: Label
    private val ageValueLabel: Label
    private val mobileValueLabel: Label

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
    }

    override fun dispose() {
        super.dispose()
        shapeRenderer.dispose()
        skin.dispose()
    }

    override fun act(delta: Float) {
        super.act(delta)

        if (cell != null && field[cell!!.position] != cell) {
            cell = null
        }

        if (cell != null) {
            table.isVisible = true
            val position = cell!!.position
            positionLabel.setText("[${position.x}, ${position.y}]")
            energyValueLabel.setText(cell!!.energy.toString())
            sizeValueLabel.setText(cell!!.size.toString())
            ageValueLabel.setText(cell!!.age.toString())
            mobileValueLabel.setText(if (cell!!.dna.canMove) "true" else "false")
        } else {
            table.isVisible = false
        }
    }

    override fun draw() {
        super.draw()

        if (cell != null) {
            Gdx.gl.glEnable(GL30.GL_BLEND)
            Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA)
            draw(cell!!)
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

        // toxin mark
        shapeRenderer.color = Color(cell.dna.toxin.color)
            .sub(BacillusGdxGame.TransparentMask)
            .add(0f, 0f, 0f, sqrt(alpha))
        shapeRenderer.line(
            x + CELL_RADIUS - radius,
            y - CELL_RADIUS + radius,
            x + CELL_RADIUS - radius / 2,
            y - CELL_RADIUS + radius / 2
        )
        shapeRenderer.line(
            x + CELL_RADIUS + radius,
            y - CELL_RADIUS + radius,
            x + CELL_RADIUS + radius / 2,
            y - CELL_RADIUS + radius / 2
        )
        shapeRenderer.line(
            x + CELL_RADIUS + radius,
            y - CELL_RADIUS - radius,
            x + CELL_RADIUS + radius / 2,
            y - CELL_RADIUS - radius / 2
        )
        shapeRenderer.line(
            x + CELL_RADIUS - radius,
            y - CELL_RADIUS - radius,
            x + CELL_RADIUS - radius / 2,
            y - CELL_RADIUS - radius / 2
        )
        shapeRenderer.end()
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