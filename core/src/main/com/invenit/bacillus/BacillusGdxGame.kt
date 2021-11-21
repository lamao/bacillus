package com.invenit.bacillus

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL30
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.TimeUtils
import com.invenit.bacillus.model.*
import com.invenit.bacillus.util.Mutator
import kotlin.math.max
import kotlin.math.min
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

    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var batch: SpriteBatch
    private lateinit var font: BitmapFont

    private val field = Field(Settings.GridWidth, Settings.GridHeight)
    private val environment = Environment()

    override fun create() {
        shapeRenderer = ShapeRenderer()
        batch = SpriteBatch()
        font = BitmapFont()

        for (i in 1..Settings.InitNumberOfOrganics) {
            spawn(Substance.Green, Substance.Sun, Substance.White, false)
        }

    }

    private fun spawn(body: Substance, consume: Substance, produce: Substance, canMove: Boolean): Organic {
        val position = field.getRandomFreePosition()

        val bacillus = Organic(
            position = position,
            direction = if (canMove) field.getRandomFreeDirection(position) else Field.NoDirection,
            size = Mutator.getRandomSize(),
            body = body,
            consume = consume,
            produce = produce,
            canMove = canMove
        )
        field.add(bacillus)

        return bacillus
    }


    override fun render() {

        if (Gdx.input.isKeyPressed(Input.Keys.NUMPAD_ADD)) {
            Settings.TicDelaySeconds = min(3f, Settings.TicDelaySeconds + 0.01f)
        } else if (Gdx.input.isKeyPressed(Input.Keys.NUMPAD_SUBTRACT)) {
            Settings.TicDelaySeconds = max(0f, Settings.TicDelaySeconds - 0.01f)
        }

        val currentTime = TimeUtils.nanoTime()
        if (currentTime - lastTicTime >= TicInterval) {
            lastTicTime = currentTime

            environment.doTic(field)

            if (MathUtils.random(1f) < Settings.ProbabilityToSpawnOrganics) {
                var consume = Substance.getRandomConsume()
                var produce = Substance.getRandomProduce()
                while (consume == produce) {
                    consume = Substance.getRandomConsume()
                    produce = Substance.getRandomProduce()
                }
                spawn(
                    Substance.getRandomBody(),
                    consume,
                    produce,
                    MathUtils.randomBoolean()
                )
            }

            ticsPassed++
        }

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


        val fpsMessage = "FPS:  ${Gdx.graphics.framesPerSecond}. " +
                "Delay: %.2f secs. ".format(Settings.TicDelaySeconds) +
                "Tics: $ticsPassed"
        batch.begin()
        font.draw(batch, fpsMessage, 10f, Settings.Height - 10f)
        font.draw(batch, "Total: ${field.organics.count() + field.minerals.count()}", 10f, Settings.Height - 30f)
        font.draw(batch, "Minerals: ${field.minerals.count()}", 10f, Settings.Height - 50f)
        font.draw(batch, "Stationary: ${field.organics.count { !it.canMove }}", 10f, Settings.Height - 70f)
        font.draw(batch, "Mobile: ${field.organics.count { it.canMove }}", 10f, Settings.Height - 90f)
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
                if (field.isFree(x, y)) {
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
            this.filter { it.canMove }
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

            if (Settings.Debug.displaySourcePosition && cell.canMove) {
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
                shapeRenderer.color = Color(cell.consume.color)
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
                shapeRenderer.color = Color(cell.produce.color)
                    .sub(TransparentMask)
                    .add(0f, 0f, 0f, sqrt(alpha))
                shapeRenderer.circle(
                    projectedPosition.x,
                    projectedPosition.y,
                    radius
                )
            }
        }
        shapeRenderer.end()

    }

    private fun MutableList<Mineral>.draw() {
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