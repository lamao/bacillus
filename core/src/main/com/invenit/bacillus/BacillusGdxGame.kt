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
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */
class BacillusGdxGame : ApplicationAdapter() {

    companion object {
        private const val OneSecond = 1000_000_000L
        val TicInterval: Float
            get() = OneSecond.toFloat() * Settings.TicDelaySeconds

        val TransparentMask = Color(0f, 0f, 0f, 1f)

        val CellRadius = Settings.CellSize.toFloat() / 2
    }

    private var ticsPassed = 0L

    private lateinit var camera: OrthographicCamera
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var batch: SpriteBatch
    private lateinit var font: BitmapFont

    private val field = Field(Settings.GridWidth, Settings.GridHeight)
    private val mutationService = ServiceContext.mutationService

    private lateinit var debugStage: DebugStage
    private lateinit var environmentStage: EnvironmentStage
    private lateinit var slidersStage: SlidersStage
    private lateinit var cellDetailsStage: CellDetailsStage


    override fun create() {
        camera = OrthographicCamera()
        camera.setToOrtho(false, Settings.TotalWidth.toFloat(), Settings.Height.toFloat())

        shapeRenderer = ShapeRenderer()
        batch = SpriteBatch()
        font = BitmapFont()

        for (i in 1..Settings.InitNumberOfOrganics) {
            spawn(DNA(Substance.Green, Substance.Sun, Substance.White, Substance.Red, false))
        }

        debugStage = DebugStage(field)
        environmentStage = EnvironmentStage(field)
        slidersStage = SlidersStage(field)
        cellDetailsStage = CellDetailsStage(field, Settings.Width + 200, Settings.Height)

        Gdx.input.inputProcessor = InputMultiplexer(
            slidersStage,
            UserInputListener(field, camera, mutationService),
            cellDetailsStage
        )

    }

    override fun dispose() {
        shapeRenderer.dispose()
        batch.dispose()
        font.dispose()

        debugStage.dispose()
        environmentStage.dispose()
        slidersStage.dispose()
        cellDetailsStage.dispose()
    }

    override fun render() {

        environmentStage.act(Gdx.graphics.deltaTime)

        camera.update()

        ScreenUtils.clear(0f, 0f, 0.1f, 1f)
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

        // TODO: Refactor
        slidersStage.setGeneralInfo(Gdx.graphics.framesPerSecond, ticsPassed)

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