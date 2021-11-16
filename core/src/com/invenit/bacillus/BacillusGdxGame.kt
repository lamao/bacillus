package com.invenit.bacillus

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.TimeUtils
import com.invenit.bacillus.model.*


/**
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */
class BacillusGdxGame : ApplicationAdapter() {

    companion object {
        private const val OneSecond = 1000_000_000L
        const val TicInterval = OneSecond.toFloat() * Settings.TicDelaySeconds

        val Transparent = Color(0f, 0f, 0f, 0.3f)
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

        for (i in 1..Settings.InitNumberOfFood) {
            field.spawn(Substance.Cellulose, Substance.Nothing)
        }

        for (i in 1..Settings.InitNumberOfBacilli) {
            field.spawn(Substance.Protein, Substance.Cellulose)
        }
    }

    override fun render() {

        val currentTime = TimeUtils.nanoTime()
        if (currentTime - lastTicTime >= TicInterval) {
            lastTicTime = currentTime

            field.doTic()
            ticsPassed++
        }

        ScreenUtils.clear(0f, 0f, 0.1f, 0.5f)

        Gdx.gl.glEnable(GL30.GL_BLEND)

        if (Settings.Debug.displayGrid) {
            drawGrid(field)
        }

        val ticPercentage = (currentTime - lastTicTime) / TicInterval
        field.organics.draw(ticPercentage)


        batch.begin()
        font.draw(batch, "FPS:  ${Gdx.graphics.framesPerSecond}", 10f, Settings.Height - 10f)
        font.draw(
            batch,
            "Bacilli: ${field.organics.filter { it.body == Substance.Protein }.count()}",
            10f,
            Settings.Height - 30f
        )
        font.draw(
            batch,
            "Food: ${field.organics.filter { it.body == Substance.Cellulose }.count()}",
            10f,
            Settings.Height - 50f
        )
        font.draw(batch, "Tics: $ticsPassed", 10f, Settings.Height - 70f)
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
            this.filter { it.body == Substance.Protein }
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

            if (Settings.Debug.displaySourcePosition && cell.body == Substance.Protein) {
                shapeRenderer.color = Color.GRAY
                shapeRenderer.circle(
                    displayPosition.x,
                    displayPosition.y,
                    (Settings.CellSize / 4).toFloat()
                )
            }


            val alpha = 0.3f + 0.7f * (cell.energy.toFloat() / Settings.MaxHealth.toFloat())
            shapeRenderer.color = when {
                cell.body == Substance.Cellulose ->
                    Color(0f, 1f, 0f, alpha)
                cell.energy < Settings.ReproductionThreshold ->
                    Color(0f, 0f, 1f, alpha)
                else ->
                    Color(1f, 0.5f, 0f, alpha)
            }
            shapeRenderer.circle(
                projectedPosition.x,
                projectedPosition.y,
                (Settings.CellSize / 2).toFloat()
            )
        }
        shapeRenderer.end()

    }

    private fun MutableList<Something>.draw() {

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (something in this) {
            val displayPosition = something.position.toDisplay()

            shapeRenderer.color =
                Color(0f, 1f, 0f, 0.3f + 0.7f * (something.energy.toFloat() / Settings.MaxHealth.toFloat()))
            shapeRenderer.circle(
                displayPosition.x,
                displayPosition.y,
                (Settings.CellSize / 2).toFloat()
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