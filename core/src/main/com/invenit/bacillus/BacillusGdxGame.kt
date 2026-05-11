package com.invenit.bacillus

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.ScreenUtils
import com.invenit.bacillus.model.*
import com.invenit.bacillus.ui.*


/**
 * Created by viacheslav.mishcheriakov
 * Created 15.11.2021
 */
class BacillusGdxGame : ApplicationAdapter() {

    companion object {
        private const val OneSecond = 1000_000_000L
        val TicInterval: Float
            get() = OneSecond.toFloat() * Settings.TicDelaySeconds

        val TransparentMask = Color(0f, 0f, 0f, 1f)

        const val CellRadius = Settings.CellSize.toFloat() / 2
    }


    private lateinit var camera: OrthographicCamera
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var batch: SpriteBatch
    private lateinit var font: BitmapFont

    private val field = Field(Settings.GridWidth, Settings.GridHeight)
    private val mutationService = ServiceContext.mutationService
    private val creatureFactory = ServiceContext.creationFactory

    private lateinit var debugStage: DebugStage
    private lateinit var environmentStage: EnvironmentStage
    private lateinit var statisticsStage: StatisticsStage
    private lateinit var slidersStage: SlidersStage
    private lateinit var cellDetailsStage: CellDetailsStage


    override fun create() {
        camera = OrthographicCamera()
        camera.setToOrtho(false, Settings.TotalWidth.toFloat(), Settings.Height.toFloat())

        shapeRenderer = ShapeRenderer()
        batch = SpriteBatch()
        font = BitmapFont()

        (1..Settings.InitNumberOfOrganics).forEach { _ ->
            spawn(DNA(Substance.Green, Substance.Sun, Substance.White, Substance.Red, false))
        }

        debugStage = DebugStage(field)
        environmentStage = EnvironmentStage(field)
        statisticsStage = StatisticsStage(field)
        statisticsStage.viewport.setWorldSize(Settings.TotalWidth.toFloat(), Settings.Height.toFloat())
        statisticsStage.viewport.update(Gdx.graphics.width, Gdx.graphics.height, true)
        slidersStage = SlidersStage(field)
        slidersStage.viewport.setWorldSize(Settings.TotalWidth.toFloat(), Settings.Height.toFloat())
        slidersStage.viewport.update(Gdx.graphics.width, Gdx.graphics.height, true)

        cellDetailsStage = CellDetailsStage(
            field,
            Settings.Width + 200f + 20f,
            Settings.Height - 20f
        )
        cellDetailsStage.viewport.setWorldSize(Settings.TotalWidth.toFloat(), Settings.Height.toFloat())
        cellDetailsStage.viewport.update(Gdx.graphics.width, Gdx.graphics.height, true)

        slidersStage.setConfigureButtonHandler {
            Settings.pause = true
            AddCreatureDialog(creatureFactory) {
                Settings.pause = false
            }.showConfiguration(cellDetailsStage)
        }

        Gdx.input.inputProcessor = InputMultiplexer(
            slidersStage,
            UserInputListener(field, environmentStage.viewport, creatureFactory),
            cellDetailsStage,
            statisticsStage
        )

    }

    override fun resize(width: Int, height: Int) {
        camera.setToOrtho(false, Settings.TotalWidth.toFloat(), Settings.Height.toFloat())
        debugStage.viewport.update(width, height, true)
        environmentStage.viewport.update(width, height, true)
        statisticsStage.viewport.update(width, height, true)
        slidersStage.viewport.update(width, height, true)
        cellDetailsStage.viewport.update(width, height, true)
    }

    override fun dispose() {
        shapeRenderer.dispose()
        batch.dispose()
        font.dispose()

        debugStage.dispose()
        environmentStage.dispose()
        statisticsStage.dispose()
        slidersStage.dispose()
        cellDetailsStage.dispose()
    }

    override fun render() {

        environmentStage.act(Gdx.graphics.deltaTime)

        camera.update()

        ScreenUtils.clear(0f, 0f, 0.1f, 1f)
        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color.BLACK
        shapeRenderer.rect(Settings.Width.toFloat(), 0f, Settings.UiWidth.toFloat(), Settings.Height.toFloat())
        shapeRenderer.color = Color.GRAY
        shapeRenderer.line(Settings.Width.toFloat(), 0f, Settings.Width.toFloat(), Settings.Height.toFloat())
        shapeRenderer.end()

        if (Settings.Debug.displayGrid) {
            debugStage.draw()
        }

        environmentStage.draw()

        statisticsStage.setGeneralInfo(Gdx.graphics.framesPerSecond, environmentStage.ticsPassed)
        statisticsStage.act(Gdx.graphics.deltaTime)
        statisticsStage.draw()

        slidersStage.act(Gdx.graphics.deltaTime)
        slidersStage.draw()

        cellDetailsStage.act(Gdx.graphics.deltaTime)
        cellDetailsStage.draw()

    }

    private fun spawn(dna: DNA): Organic {
        val position = getRandomFreePosition()

        val bacillus = Organic(
            position = position,
            direction = Field.NoDirection,
            size = mutationService.mutatedSize(Settings.DefaultSize),
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

}