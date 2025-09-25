package com.invenit.bacillus.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.TimeUtils
import com.invenit.bacillus.BacillusGdxGame
import com.invenit.bacillus.Environment
import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.*
import kotlin.math.sqrt

/**
 * Created by viacheslav.mishcheriakov
 * Created 25.11.2021
 */
class EnvironmentStage(val field: Field) : Stage() {

    private var lastTicTime = 0L
    private var ticsPassed = 0L
    private var ticPercentage = 0f

    private val shapeRenderer = ShapeRenderer()

    private val environment = Environment()

    override fun act(delta: Float) {
        super.act(delta)

        val currentTime = TimeUtils.nanoTime()
        if (currentTime - lastTicTime >= BacillusGdxGame.TicInterval) {
            lastTicTime = currentTime

            environment.doTic(field)

//            if (MathUtils.random(1f) < Settings.ProbabilityToSpawnOrganics) {
//                spawn(
//                    DNA(
//                        Substance.getRandomBody(),
//                        Substance.getRandomConsume(),
//                        Substance.getRandomProduce(),
//                        Substance.getRandomToxin(),
//                        MathUtils.randomBoolean()
//                    )
//                )
//            }

            ticsPassed++
        }

        ticPercentage = if (Settings.SmoothAnimation) {
            (currentTime - lastTicTime) / BacillusGdxGame.TicInterval
        } else {
            0f
        }
    }

    override fun draw() {
        super.draw()


        Gdx.gl.glEnable(GL30.GL_BLEND)
        Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA)
        field.organics.draw(ticPercentage)
        field.minerals.draw()
        Gdx.gl.glDisable(GL30.GL_BLEND)
    }

    private fun Iterable<Organic>.draw(ticPercentage: Float) {

        // TODO: Move to debug stage
        if (Settings.Debug.displaySourcePosition) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            shapeRenderer.color = Color.GRAY
            this.filter { it.dna.canMove }
                .forEach { cell ->
                    val displayPosition = cell.position.toDisplay()
                    val projectedPosition = displayPosition.projectedPosition(cell.direction, ticPercentage)
                    shapeRenderer.line(
                        displayPosition.x,
                        displayPosition.y,
                        projectedPosition.x,
                        projectedPosition.y
                    )
                }
            shapeRenderer.end()
        }



        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (cell in this) {
            val displayPosition = cell.position.toDisplay()
            val projectedPosition = displayPosition.projectedPosition(cell.direction, ticPercentage)

            if (Settings.Debug.displaySourcePosition && cell.dna.canMove) {
                shapeRenderer.color = Color.GRAY
                shapeRenderer.circle(
                    displayPosition.x,
                    displayPosition.y,
                    BacillusGdxGame.CellRadius / 2
                )
            }


            val alpha = cell.getAlpha()
            val radius = cell.getRadius()
            shapeRenderer.color = Color(cell.body.color)
                .sub(BacillusGdxGame.TransparentMask)
                .add(0f, 0f, 0f, alpha)
            shapeRenderer.circle(
                projectedPosition.x,
                projectedPosition.y,
                radius
            )

            if (radius >= 2f) {
                shapeRenderer.color = Color(cell.dna.consume.color)
                    .sub(BacillusGdxGame.TransparentMask)
                    .add(0f, 0f, 0f, sqrt(alpha))
                shapeRenderer.circle(
                    projectedPosition.x,
                    projectedPosition.y,
                    radius / 2
                )
            }
        }
        shapeRenderer.end()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        for (cell in this) {
            val displayPosition = cell.position.toDisplay()
            val projectedPosition = displayPosition.projectedPosition(cell.direction, ticPercentage)

            val alpha = cell.getAlpha()
            val radius = cell.getRadius()
            if (radius >= 3f) {
                shapeRenderer.color = Color(cell.dna.produce.color)
                    .sub(BacillusGdxGame.TransparentMask)
                    .add(0f, 0f, 0f, sqrt(alpha))
                shapeRenderer.circle(
                    projectedPosition.x,
                    projectedPosition.y,
                    radius
                )
            }

            // toxin mark
            if (radius >= 4f) {
                shapeRenderer.color = Color(cell.dna.toxin.color)
                    .sub(BacillusGdxGame.TransparentMask)
                    .add(0f, 0f, 0f, sqrt(alpha))
                shapeRenderer.line(
                    projectedPosition.x - radius,
                    projectedPosition.y + radius,
                    projectedPosition.x - radius / 2,
                    projectedPosition.y + radius / 2
                )
                shapeRenderer.line(
                    projectedPosition.x + radius,
                    projectedPosition.y + radius,
                    projectedPosition.x + radius / 2,
                    projectedPosition.y + radius / 2
                )
                shapeRenderer.line(
                    projectedPosition.x + radius,
                    projectedPosition.y - radius,
                    projectedPosition.x + radius / 2,
                    projectedPosition.y - radius / 2
                )
                shapeRenderer.line(
                    projectedPosition.x - radius,
                    projectedPosition.y - radius,
                    projectedPosition.x - radius / 2,
                    projectedPosition.y - radius / 2
                )
            }
        }
        shapeRenderer.end()

    }

    private fun Iterable<Mineral>.draw() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (mineral in this) {
            val displayPosition = mineral.position.toDisplay()
            val radius = mineral.getRadius()
            shapeRenderer.color = mineral.body.color

            shapeRenderer.rect(
                displayPosition.x - radius,
                displayPosition.y - radius,
                2 * radius,
                2 * radius
            )
        }
        shapeRenderer.end()
    }

    private fun Organic.getAlpha() =
        0.3f + 0.7f * (this.energy.toFloat() / this.size.toFloat())

    private fun Something.getRadius() =
        0.25f * BacillusGdxGame.CellRadius + 0.75f * BacillusGdxGame.CellRadius * (this.size.toFloat() / Settings.MaxSize)

    private fun Point.toDisplay(): Vector2 = Vector2(
        (this.x * Settings.CellSize + Settings.CellSize / 2).toFloat(),
        (this.y * Settings.CellSize + Settings.CellSize / 2).toFloat()
    )

    private fun Vector2.projectedPosition(direction: Point, percentage: Float): Vector2 = Vector2(
        this.x + percentage * direction.x * Settings.CellSize,
        this.y + percentage * direction.y * Settings.CellSize
    )

    override fun dispose() {
        super.dispose()
        shapeRenderer.dispose()
    }
}