package com.invenit.bacillus.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.Field


/**
 * Created by viacheslav.mishcheriakov
 * Created 25.11.2021
 */
class SlidersStage(val field: Field) : Stage() {

    private val configureCreatureButton: Button

    private var skin: Skin = Skin(Gdx.files.internal("uiskin.json"))

    init {

        val table = Table()
        table.setSize(Settings.UiWidth.toFloat() - 40f, Settings.Height.toFloat())
        table.setPosition(Settings.Width.toFloat() + 20f, 0f)
        table.align(Align.bottomLeft)

        addActor(table)

        table.row()
        table.add(Label("", skin)).colspan(3)

        table.addSlider(0f, 1f, 0.02f, Settings.TicDelaySeconds,
            { Settings.TicDelaySeconds = it },
            "Tic Delay",
            { "%.2f s".format(Settings.TicDelaySeconds) }
        )

        table.addSlider(0f, 0.3f, 0.005f, Settings.MutationRate,
            { Settings.MutationRate = it },
            "Mutation Rate",
            { "%.3f".format(Settings.MutationRate) }
        )

        table.addSlider(0f, 200f, 5f, Settings.SunYield.toFloat(),
            { Settings.SunYield = it.toInt() },
            "Sun Yield",
            { "%,d".format(Settings.SunYield) }
        )
        table.addSlider(0f, 500f, 20f, Settings.BiteYield.toFloat(),
            { Settings.BiteYield = it.toInt() },
            "Bite Yield",
            { "%,d".format(Settings.BiteYield) }
        )
        table.addSlider(0f, 200f, 5f, Settings.MineralsYield.toFloat(),
            { Settings.MineralsYield = it.toInt() },
            "Minerals Yield",
            { "%,d".format(Settings.MineralsYield) }
        )

        table.addSlider(0f, 100f, 5f, Settings.MoveConsumption.toFloat(),
            { Settings.MoveConsumption = it.toInt() },
            "Move Consumption",
            { "%,d".format(Settings.MoveConsumption) }
        )
        table.addSlider(0f, 100f, 5f, Settings.PermanentConsumption.toFloat(),
            { Settings.PermanentConsumption = it.toInt() },
            "Permanent Consumption",
            { "%,d".format(Settings.PermanentConsumption) }
        )

        table.addSlider(0f, 1f, 0.05f, Settings.ProductionPerformance,
            { Settings.ProductionPerformance = it },
            "Production Performance",
            { "%.2f".format(Settings.ProductionPerformance) }
        )

        table.addSlider(0f, 30f, 1f, Settings.MineralDegradation.toFloat(),
            { Settings.MineralDegradation = it.toInt() },
            "Mineral Degradation",
            { "%,d".format(Settings.MineralDegradation) }
        )

        table.addSlider(100f, 2000f, 50f, Settings.DefaultSize.toFloat(),
            { Settings.DefaultSize = it.toInt() },
            "Default Size",
            { "%,d".format(Settings.DefaultSize) }
        )
        table.addSlider(500f, 5000f, 100f, Settings.ReproductionThreshold.toFloat(),
            { Settings.ReproductionThreshold = it.toInt() },
            "Reproduction Threshold",
            { "%,d".format(Settings.ReproductionThreshold) }
        )
        table.addSlider(500f, 5000f, 100f, Settings.MaxAge.toFloat(),
            { Settings.MaxAge = it.toInt() },
            "MaxAge",
            { "%,d".format(Settings.MaxAge) }
        )

        table.row()
        table.add(Label("", skin)).colspan(3)

        table.addCheckBox(Settings.Debug.displayGrid, " Display grid") {
            Settings.Debug.displayGrid = it
        }

        table.addCheckBox(Settings.Debug.displaySourcePosition, " Display source position") {
            Settings.Debug.displaySourcePosition = it
        }

        table.row()
        table.add(Label("", skin)).colspan(3)
        configureCreatureButton = table.addButton("Configure creature")
        table.layout()
    }

    private fun Table.addButton(
        labelText: String
    ) : Button {
        this.row().align(Align.left)

        val button = TextButton(labelText, this@SlidersStage.skin)
        this.add(button).colspan(3)
        return button
    }

    private fun Table.addCheckBox(
        default: Boolean,
        labelText: String,
        handler: (Boolean) -> Unit
    ) {
        this.row().align(Align.left)

        val checkBox = CheckBox(labelText, this@SlidersStage.skin)
        checkBox.isChecked = default
        checkBox.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                handler(checkBox.isChecked)
            }

        })
        this.add(checkBox).colspan(3)
    }

    private fun Table.addSlider(
        min: Float, max: Float, step: Float, default: Float,
        handler: (Float) -> Unit,
        labelText: String,
        valueProvider: () -> String
    ) {
        this.row().align(Align.left)
        val label = Label(labelText, this@SlidersStage.skin)
        this.add(label).padRight(10f).left()

        val slider = Slider(min, max, step, false, this@SlidersStage.skin)
        slider.value = default
        val valueLabel = Label(valueProvider(), this@SlidersStage.skin)
        slider.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                if (slider.isDragging) {
                    handler(slider.value)
                    valueLabel.setText(valueProvider())
                }
            }
        })
        this.add(slider).fillX().expandX().padRight(10f)
        this.add(valueLabel).width(80f).left()
    }

    override fun act(delta: Float) {
        super.act(delta)
    }

    fun setConfigureButtonHandler(handler: () -> Unit) {
        configureCreatureButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                handler()
            }
        })
    }


    override fun dispose() {
        skin.dispose()
        super.dispose()
    }
}
