package com.invenit.bacillus.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.invenit.bacillus.BacillusGdxGame
import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.*
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Created by viacheslav.mishcheriakov
 * Created 26.11.2021
 */
class CellDetailsStage(val field: Field, val x: Int, val y: Int) : Stage() {

    companion object {
        const val CellRadius = 50f
    }

    private var cell: Organic? = null
    private val shapeRenderer = ShapeRenderer()
    private val font = BitmapFont()

    override fun dispose() {
        super.dispose()
        shapeRenderer.dispose()
    }

    override fun act(delta: Float) {
        super.act(delta)
        if (cell != null && field[cell!!.position] != cell) {
            cell = null
        }
    }

    override fun draw() {
        super.draw()

        if (cell != null) {
            Gdx.gl.glEnable(GL30.GL_BLEND)
            Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA)
            draw(cell!!)
            Gdx.gl.glDisable(GL30.GL_BLEND)

            batch.begin()
            font.draw(batch, "Energy: ${cell!!.energy}", x.toFloat(), y - 2 * CellRadius - 10f)
            font.draw(batch, "Size: ${cell!!.size}", x.toFloat(), y - 2 * CellRadius - 30f)
            font.draw(batch, "Age: ${cell!!.age}", x.toFloat(), y - 2 * CellRadius - 50f)
            font.draw(batch, "Mobile: " + if (cell!!.dna.canMove) "true" else "false", x.toFloat(), y - 2 * CellRadius - 70f)
            batch.end()
        }
    }

    private fun draw(cell: Organic) {


        val alpha = cell.getAlpha()
        val radius = cell.getRadius() * CellRadius

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        shapeRenderer.color = Color(cell.dna.produce.color)
            .sub(BacillusGdxGame.TransparentMask)
            .add(0f, 0f, 0f, sqrt(alpha))
        shapeRenderer.circle(
            x + CellRadius,
            y - CellRadius,
            radius
        )

        shapeRenderer.color = Color(cell.body.color)
            .sub(BacillusGdxGame.TransparentMask)
            .add(0f, 0f, 0f, alpha)
        shapeRenderer.circle(
            x + CellRadius,
            y - CellRadius,
            radius * 4 / 5
        )

        shapeRenderer.color = Color(cell.dna.consume.color)
            .sub(BacillusGdxGame.TransparentMask)
            .add(0f, 0f, 0f, sqrt(alpha))
        shapeRenderer.circle(
            x + CellRadius,
            y - CellRadius,
            radius * 2 / 5
        )

        // toxin mark
        shapeRenderer.color = Color(cell.dna.toxin.color)
            .sub(BacillusGdxGame.TransparentMask)
            .add(0f, 0f, 0f, sqrt(alpha))
        shapeRenderer.line(
            x + CellRadius - radius,
            y - CellRadius + radius,
            x + CellRadius - radius / 2,
            y - CellRadius + radius / 2
        )
        shapeRenderer.line(
            x + CellRadius + radius,
            y - CellRadius + radius,
            x + CellRadius + radius / 2,
            y - CellRadius + radius / 2
        )
        shapeRenderer.line(
            x + CellRadius + radius,
            y - CellRadius - radius,
            x + CellRadius + radius / 2,
            y - CellRadius - radius / 2
        )
        shapeRenderer.line(
            x + CellRadius - radius,
            y - CellRadius - radius,
            x + CellRadius - radius / 2,
            y - CellRadius - radius / 2
        )
        shapeRenderer.end()
    }

    private fun Organic.getAlpha() =
        0.3f + 0.7f * (this.energy.toFloat() / this.size.toFloat())

    private fun Something.getRadius() =
        0.25f + 0.75f * (this.size.toFloat() / Settings.MaxSize)

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val touchPoint = Vector2(screenX.toFloat(), screenY.toFloat())
        viewport.unproject(touchPoint)

        val position = Point(
            touchPoint.x.roundToInt() / Settings.CellSize,
            touchPoint.y.roundToInt() / Settings.CellSize
        )

        val something = field[position]
        if (something == null || something is Mineral) {
            cell = null
        } else if (something is Organic) {
            cell = something
        }

        println("$something")

        return super.touchUp(screenX, screenY, pointer, button)
    }
}