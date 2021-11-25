package com.invenit.bacillus

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.TimeUtils
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.invenit.bacillus.model.*
import com.invenit.bacillus.ui.SlidersStage
import com.invenit.bacillus.ui.UserInputListener
import com.invenit.bacillus.util.Mutator
import kotlin.math.sqrt


/**
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */
class BacillusGdxGame : ApplicationAdapter() {

    companion object {
        private const val OneSecond = 1000_000_000L
        val TicInterval: Float
            get() = OneSecond.toFloat() * Settings.TicDelaySeconds

        val TransparentMask = Color(0f, 0f, 0f, 1f)
        val ReproductionMask = Color(0.5f, 0.0f, 0f, 0f)

        val CellRadius = Settings.CellSize.toFloat() / 2
    }

    private var lastTicTime = 0L
    private var ticsPassed = 0L

    private lateinit var camera: OrthographicCamera
    private lateinit var viewport: ExtendViewport
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var batch: SpriteBatch
    private lateinit var font: BitmapFont

    private val field = Field(Settings.GridWidth, Settings.GridHeight)
    private val environment = Environment()

    private lateinit var ui: SlidersStage


    override fun create() {
        camera = OrthographicCamera()
        camera.setToOrtho(false, Settings.Width.toFloat(), Settings.Height.toFloat())

        shapeRenderer = ShapeRenderer()
        batch = SpriteBatch()
        font = BitmapFont()

        for (i in 1..Settings.InitNumberOfOrganics) {
            spawn(DNA(Substance.Green, Substance.Sun, Substance.White, Substance.Red, false))
        }

        ui = SlidersStage()

        Gdx.input.inputProcessor = InputMultiplexer(
            ui,
            UserInputListener(field, camera)
        )

    }

    override fun dispose() {
        shapeRenderer.dispose()
        batch.dispose()
        font.dispose()

        ui.dispose()
    }

    override fun render() {

        val currentTime = TimeUtils.nanoTime()
        if (currentTime - lastTicTime >= TicInterval) {
            lastTicTime = currentTime

            environment.doTic(field)

            if (MathUtils.random(1f) < Settings.ProbabilityToSpawnOrganics) {
                spawn(
                    DNA(
                        Substance.getRandomBody(),
                        Substance.getRandomConsume(),
                        Substance.getRandomProduce(),
                        Substance.getRandomToxin(),
                        MathUtils.randomBoolean()
                    )
                )
            }

            ticsPassed++
        }

        camera.update()

        ScreenUtils.clear(0f, 0f, 0.1f, 1f)

        if (Settings.Debug.displayGrid) {
            drawGrid(field)
        }

        val ticPercentage = if (Settings.SmoothAnimation) {
            (currentTime - lastTicTime) / TicInterval
        } else {
            0f
        }
        Gdx.gl.glEnable(GL30.GL_BLEND)
        Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA)
        field.organics.draw(ticPercentage)
        field.minerals.draw()
        Gdx.gl.glDisable(GL30.GL_BLEND)


        // TODO: Refactor
        ui.setGeneralInfo(Gdx.graphics.framesPerSecond, ticsPassed)
        ui.setTotal(field.organics.count() + field.minerals.count())
        ui.setMinerals(field.minerals.count())
        ui.setStationary(field.organics.count { !it.dna.canMove })
        ui.setMobile(field.organics.count { it.dna.canMove })

        ui.act(Gdx.graphics.deltaTime)
        ui.draw()

    }

    private fun spawn(dna: DNA): Organic {
        val position = getRandomFreePosition()

        val bacillus = Organic(
            position = position,
            direction = Field.NoDirection,
            size = Mutator.getRandomSize(),
            dna = dna
        )
        field.add(bacillus)

        return bacillus
    }


    private fun getRandomFreePosition(): Point {
        var position = getRandomPosition()
        while (!field.isFree(position)) {
            position = getRandomPosition()
        }

        return position
    }

    private fun getRandomPosition() = Point(
        MathUtils.random(field.width - 1),
        MathUtils.random(field.height - 1)
    )

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
                if (!field.isFree(x, y)) {
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

    private fun Iterable<Organic>.draw(ticPercentage: Float) {

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
                    CellRadius / 2
                )
            }


            val alpha = cell.getAlpha()
            val radius = cell.getRadius()
            shapeRenderer.color = Color(cell.body.color)
                .sub(TransparentMask)
                .add(0f, 0f, 0f, alpha)
            shapeRenderer.circle(
                projectedPosition.x,
                projectedPosition.y,
                radius
            )

            if (radius >= 2f) {
                shapeRenderer.color = Color(cell.dna.consume.color)
                    .sub(TransparentMask)
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
                    .sub(TransparentMask)
                    .add(0f, 0f, 0f, sqrt(alpha))
                shapeRenderer.circle(
                    projectedPosition.x,
                    projectedPosition.y,
                    radius
                )

                // toxin mark
                shapeRenderer.color = Color(cell.dna.toxin.color)
                    .sub(TransparentMask)
                    .add(0f, 0f, 0f, sqrt(alpha))
                shapeRenderer.line(
                    displayPosition.x - radius,
                    displayPosition.y + radius,
                    displayPosition.x - radius / 2,
                    displayPosition.y + radius / 2
                )
                shapeRenderer.line(
                    displayPosition.x + radius,
                    displayPosition.y + radius,
                    displayPosition.x + radius / 2,
                    displayPosition.y + radius / 2
                )
                shapeRenderer.line(
                    displayPosition.x + radius,
                    displayPosition.y - radius,
                    displayPosition.x + radius / 2,
                    displayPosition.y - radius / 2
                )
                shapeRenderer.line(
                    displayPosition.x - radius,
                    displayPosition.y - radius,
                    displayPosition.x - radius / 2,
                    displayPosition.y - radius / 2
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
        0.25f * CellRadius + 0.75f * CellRadius * (this.size.toFloat() / Settings.MaxSize)

    private fun Point.toDisplay(): Vector2 = Vector2(
        (this.x * Settings.CellSize + Settings.CellSize / 2).toFloat(),
        (this.y * Settings.CellSize + Settings.CellSize / 2).toFloat()
    )

    private fun Vector2.projectedPosition(direction: Point, percentage: Float): Vector2 = Vector2(
        this.x + percentage * direction.x * Settings.CellSize,
        this.y + percentage * direction.y * Settings.CellSize
    )

}