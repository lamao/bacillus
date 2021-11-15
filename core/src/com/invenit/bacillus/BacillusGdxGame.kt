package com.invenit.bacillus

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.ScreenUtils
import com.invenit.bacillus.model.Field
import com.invenit.bacillus.model.Point

/**
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */
class BacillusGdxGame : ApplicationAdapter() {

    private lateinit var shapeRenderer: ShapeRenderer

    private val field = Field(Settings.GridWidth, Settings.GridHeight)

    override fun create() {
        shapeRenderer = ShapeRenderer()

        for (i in 1..10) {
            field.spawnCreature()
        }
    }

    override fun render() {
        ScreenUtils.clear(0f, 0f, 0.1f, 0.5f)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color.BLUE
        for (bacillus in field.bacilli) {
            shapeRenderer.circle(
                bacillus.position.toDisplayX(),
                bacillus.position.toDisplayY(),
                Settings.CellSize.toFloat()
            )
        }
        shapeRenderer.end()
    }

    override fun dispose() {
        shapeRenderer.dispose()
    }

    private fun Point.toDisplayX(): Float = (this.x * Settings.CellSize).toFloat()

    private fun Point.toDisplayY(): Float = (this.y * Settings.CellSize).toFloat()

}