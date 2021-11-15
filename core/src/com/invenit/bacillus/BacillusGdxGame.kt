package com.invenit.bacillus

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
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
    }

    private var lastTicMillis = 0L
    private var lastFrameMillis = 0L

    private lateinit var shapeRenderer: ShapeRenderer

    private val field = Field(Settings.GridWidth, Settings.GridHeight)

    override fun create() {
        shapeRenderer = ShapeRenderer()

        for (i in 1..10) {
            field.spawnCreature()
        }
    }

    override fun render() {

        val currentMillis = TimeUtils.millis()
        if (currentMillis - lastTicMillis >= TicInterval) {
            lastTicMillis = currentMillis

            for (bacillus in field.bacilli) {
                bacillus.direction = getRandomDirection()
            }

            field.moveBacilli()
        }

        ScreenUtils.clear(0f, 0f, 0.1f, 0.5f)

        val ticPercentage = (currentMillis - lastFrameMillis) / TicInterval
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color.BLUE
        for (bacillus in field.bacilli) {
            shapeRenderer.circle(
                bacillus.position.toDisplayX(),
                bacillus.position.toDisplayY(),
                (Settings.CellSize / 2).toFloat()
            )
        }
        shapeRenderer.end()
    }

    override fun dispose() {
        shapeRenderer.dispose()
    }

    private fun Point.toDisplayX(): Float = (this.x * Settings.CellSize + Settings.CellSize / 2).toFloat()

    private fun Point.toDisplayY(): Float = (this.y * Settings.CellSize + Settings.CellSize / 2).toFloat()

    private fun getRandomDirection(): Point = Point(
        x = MathUtils.random(-1, 1),
        y = MathUtils.random(-1, 1)
    )

}