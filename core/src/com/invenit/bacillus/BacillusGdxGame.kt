package com.invenit.bacillus

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.TimeUtils
import com.invenit.bacillus.model.Field
import com.invenit.bacillus.model.Point

/**
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */
class BacillusGdxGame : ApplicationAdapter() {

    companion object {
        private const val OneSecond = 1000L
        const val TicInterval = OneSecond.toFloat() / Settings.Fps

        val Transparent = Color(0f, 0f, 0f, 0.3f)
    }

    private var lastTicTime = 0L

    private lateinit var shapeRenderer: ShapeRenderer

    private val field = Field(Settings.GridWidth, Settings.GridHeight)

    override fun create() {
        shapeRenderer = ShapeRenderer()

        for (i in 1..10) {
            field.spawnCreature()
        }
    }

    override fun render() {

        val currentTime = TimeUtils.millis()
        if (currentTime - lastTicTime >= TicInterval) {
            lastTicTime = currentTime

            field.moveBacilli()

            for (bacillus in field.bacilli) {
                bacillus.direction = getRandomDirection()
            }
        }

        ScreenUtils.clear(0f, 0f, 0.1f, 0.5f)

        Gdx.gl.glEnable(GL30.GL_BLEND)

        val ticPercentage = (currentTime - lastTicTime) / TicInterval
        shapeRenderer.setAutoShapeType(true)
        shapeRenderer.begin()
        for (bacillus in field.bacilli) {
            val displayPosition = bacillus.position.toDisplay()
            val projectedPosition = displayPosition.projectedPosition(bacillus.direction, ticPercentage)

            shapeRenderer.set(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color.GRAY
            shapeRenderer.circle(
                displayPosition.x,
                displayPosition.y,
                (Settings.CellSize / 4).toFloat()
            )

            shapeRenderer.set(ShapeRenderer.ShapeType.Line)
            shapeRenderer.line(
                displayPosition.x,
                displayPosition.y,
                projectedPosition.x,
                projectedPosition.y
            )

            shapeRenderer.set(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color.BLUE
            shapeRenderer.circle(
                projectedPosition.x,
                projectedPosition.y,
                (Settings.CellSize / 2).toFloat()
            )
        }
        shapeRenderer.end()

    }

    override fun dispose() {
        shapeRenderer.dispose()
    }

    private fun Point.toDisplay(): Vector2 = Vector2(
        (this.x * Settings.CellSize + Settings.CellSize / 2).toFloat(),
        (this.y * Settings.CellSize + Settings.CellSize / 2).toFloat()
    )

    private fun getRandomDirection(): Point = Point(
        x = MathUtils.random(-1, 1),
        y = MathUtils.random(-1, 1)
    )

    private fun Vector2.projectedPosition(direction: Point, percentage: Float): Vector2 = Vector2(
        this.x + percentage * direction.x * Settings.CellSize,
        this.y + percentage * direction.y * Settings.CellSize
    )

}