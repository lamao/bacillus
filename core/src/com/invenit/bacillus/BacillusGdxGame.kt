package com.invenit.bacillus

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.TimeUtils
import com.invenit.bacillus.model.Field
import com.invenit.bacillus.model.Organic
import com.invenit.bacillus.model.Point
import com.invenit.bacillus.model.Substance
import kotlin.math.sqrt


/**
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */
class BacillusGdxGame : ApplicationAdapter() {

    companion object {
        private const val OneSecond = 1000_000_000L
        const val TicInterval = OneSecond.toFloat() * Settings.TicDelaySeconds

        val TransparentMask = Color(0f, 0f, 0f, 1f)
        val ReproductionMask = Color(0.5f, 0.0f, 0f, 0f)
    }

    private var lastTicTime = 0L
    private var ticsPassed = 0L

    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var batch: SpriteBatch
    private lateinit var font: BitmapFont

    private val field = Field(Settings.GridWidth, Settings.GridHeight)

    override fun create() {
        shapeRenderer = ShapeRenderer()
        batch = SpriteBatch()
        font = BitmapFont()

        for (i in 1..Settings.InitNumberOfOrganics) {
            field.spawn(Substance.Green, Substance.Nothing)
        }

    }

    override fun render() {

        val currentTime = TimeUtils.nanoTime()
        if (currentTime - lastTicTime >= TicInterval) {
            lastTicTime = currentTime

            field.doTic()

            if (MathUtils.random(1f) < Settings.ProbabilityToSpawnOrganics) {
                val body = Substance.values()[MathUtils.random(1, Substance.values().size - 1)]
                val consume = Substance.values()[MathUtils.random(Substance.values().size - 1)]
                field.spawn(body, consume)
            }

            ticsPassed++
        }

        ScreenUtils.clear(0f, 0f, 0.1f, 0.5f)

        Gdx.gl.glEnable(GL30.GL_BLEND)

        if (Settings.Debug.displayGrid) {
            drawGrid(field)
        }

        val ticPercentage = if (Settings.SmoothAnimation) {
            (currentTime - lastTicTime) / TicInterval
        } else {
            0f
        }
        field.organics.draw(ticPercentage)


        batch.begin()
        font.draw(batch, "FPS:  ${Gdx.graphics.framesPerSecond}", 10f, Settings.Height - 10f)
        font.draw(batch, "Tics: $ticsPassed", 10f, Settings.Height - 30f)
        font.draw(
            batch,
            "Population: ${field.organics.count()}",
            10f,
            Settings.Height - 50f
        )

        batch.end()

    }

    private fun drawGrid(field: Field) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.DARK_GRAY

        for (y in 0 until field.height) {
            shapeRenderer.line(
                0f,
                y * Settings.CellSize.toFloat(),
                Settings.Width.toFloat(),
                y * Settings.CellSize.toFloat()
            )
        }

        for (x in 0 until field.width) {
            shapeRenderer.line(
                x * Settings.CellSize.toFloat(),
                0f,
                x * Settings.CellSize.toFloat(),
                Settings.Height.toFloat()
            )
        }

        for (y in 0 until field.height) {
            for (x in 0 until field.width) {
                if (field.grid[y][x] != null) {
                    val displayPosition = Point(x, y).toDisplay()

                    shapeRenderer.line(
                        displayPosition.x - Settings.CellSize / 2,
                        displayPosition.y - Settings.CellSize / 2,
                        displayPosition.x + Settings.CellSize / 2,
                        displayPosition.y + Settings.CellSize / 2
                    )
                    shapeRenderer.line(
                        displayPosition.x - Settings.CellSize / 2,
                        displayPosition.y + Settings.CellSize / 2,
                        displayPosition.x + Settings.CellSize / 2,
                        displayPosition.y - Settings.CellSize / 2
                    )
                }
            }
        }
        shapeRenderer.end()
    }

    private fun MutableList<Organic>.draw(ticPercentage: Float) {

        if (Settings.Debug.displaySourcePosition) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            shapeRenderer.color = Color.GRAY
            this.filter { it.body == Substance.Blue }
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

            if (Settings.Debug.displaySourcePosition && cell.body == Substance.Blue) {
                shapeRenderer.color = Color.GRAY
                shapeRenderer.circle(
                    displayPosition.x,
                    displayPosition.y,
                    (Settings.CellSize / 4).toFloat()
                )
            }


            val alpha = 0.3f + 0.7f * (cell.energy.toFloat() / Settings.MaxHealth.toFloat())
            shapeRenderer.color = cell.body.color
                .sub(TransparentMask)
                .add(0f, 0f, 0f, alpha)
            if (cell.energy >= Settings.ReproductionThreshold) {
                shapeRenderer.color.add(ReproductionMask)
            }
            shapeRenderer.circle(
                projectedPosition.x,
                projectedPosition.y,
                (Settings.CellSize / 2).toFloat()
            )
            shapeRenderer.color = cell.consume.color
                .sub(TransparentMask)
                .add(0f, 0f, 0f, sqrt(alpha))
            shapeRenderer.circle(
                projectedPosition.x,
                projectedPosition.y,
                (Settings.CellSize / 4).toFloat()
            )
        }
        shapeRenderer.end()

    }

    override fun dispose() {
        shapeRenderer.dispose()
        batch.dispose()
        font.dispose()
    }

    private fun Point.toDisplay(): Vector2 = Vector2(
        (this.x * Settings.CellSize + Settings.CellSize / 2).toFloat(),
        (this.y * Settings.CellSize + Settings.CellSize / 2).toFloat()
    )

    private fun Vector2.projectedPosition(direction: Point, percentage: Float): Vector2 = Vector2(
        this.x + percentage * direction.x * Settings.CellSize,
        this.y + percentage * direction.y * Settings.CellSize
    )

}