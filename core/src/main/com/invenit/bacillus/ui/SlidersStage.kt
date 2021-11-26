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
 * Created by vyacheslav.mischeryakov
 * Created 25.11.2021
 */
class SlidersStage(val field: Field) : Stage() {

    private val generalInfo: Label
    private val totalLabel: Label
    private val mineralsLabel: Label
    private val stationaryLabel: Label
    private val mobileLabel: Label

    private var skin: Skin = Skin(Gdx.files.internal("uiskin.json"))

    init {

        val table = Table()
        table.setSize(Settings.UiWidth.toFloat(), Gdx.graphics.height.toFloat())
        table.setPosition(Settings.Width.toFloat() + 20f, 0f)
        table.align(Align.topLeft)

        addActor(table)

        table.row().align(Align.left)
        generalInfo = Label("", skin)
        table.add(generalInfo)

        table.row().align(Align.left)
        totalLabel = Label("", skin)
        table.add(totalLabel)

        table.row().align(Align.left)
        mineralsLabel = Label("", skin)
        table.add(mineralsLabel)

        table.row().align(Align.left)
        stationaryLabel = Label("", skin)
        table.add(stationaryLabel)

        table.row().align(Align.left)
        mobileLabel = Label("", skin)
        table.add(mobileLabel)

        table.row()
        table.add(Label("", skin))

        table.addSlider(0f, 1f, 0.02f, Settings.TicDelaySeconds,
            { Settings.TicDelaySeconds = it },
            { "Tic Delay: %.2f s".format(Settings.TicDelaySeconds) }
        )

        table.addSlider(0f, 0.3f, 0.005f, Settings.MutationRate,
            { Settings.MutationRate = it },
            { "Mutation Rate: %.3f".format(Settings.MutationRate) }
        )

        table.addSlider(0f, 200f, 10f, Settings.SunYield.toFloat(),
            { Settings.SunYield = it.toInt() },
            { "Sun Yield: %,d".format(Settings.SunYield) }
        )
        table.addSlider(0f, 500f, 20f, Settings.BiteYield.toFloat(),
            { Settings.BiteYield = it.toInt() },
            { "Bite Yield: %,d".format(Settings.BiteYield) }
        )
        table.addSlider(0f, 200f, 10f, Settings.MineralsYield.toFloat(),
            { Settings.MineralsYield = it.toInt() },
            { "Minerals Yield: %,d".format(Settings.MineralsYield) }
        )

        table.addSlider(0f, 100f, 5f, Settings.MoveConsumption.toFloat(),
            { Settings.MoveConsumption = it.toInt() },
            { "Move Consumption: %,d".format(Settings.MoveConsumption) }
        )
        table.addSlider(0f, 100f, 5f, Settings.PermanentConsumption.toFloat(),
            { Settings.PermanentConsumption = it.toInt() },
            { "Permanent Consumption: %,d".format(Settings.PermanentConsumption) }
        )

        table.addSlider(0f, 1f, 0.05f, Settings.ProductionPerformance,
            { Settings.ProductionPerformance = it },
            { "Production Performance: %.2f".format(Settings.ProductionPerformance) }
        )

        table.addSlider(0f, 30f, 1f, Settings.MineralDegradation.toFloat(),
            { Settings.MineralDegradation = it.toInt() },
            { "Mineral Degradation: %,d".format(Settings.MineralDegradation) }
        )

        table.addSlider(100f, 2000f, 50f, Settings.DefaultSize.toFloat(),
            { Settings.DefaultSize = it.toInt() },
            { "Default Size: %,d".format(Settings.DefaultSize) }
        )
        table.addSlider(500f, 5000f, 100f, Settings.ReproductionThreshold.toFloat(),
            { Settings.ReproductionThreshold = it.toInt() },
            { "Reproduction Threshold: %,d".format(Settings.ReproductionThreshold) }
        )
        table.addSlider(500f, 5000f, 100f, Settings.MaxAge.toFloat(),
            { Settings.MaxAge = it.toInt() },
            { "MaxAge: %,d".format(Settings.MaxAge) }
        )

        table.row()
        table.add(Label("", skin))

        table.addCheckBox(Settings.Debug.displayGrid, " Display grid") {
            Settings.Debug.displayGrid = it
        }

        table.addCheckBox(Settings.Debug.displaySourcePosition, " Display source position") {
            Settings.Debug.displaySourcePosition = it
        }

        table.layout()
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
        this.add(checkBox)
    }

    private fun Table.addSlider(
        min: Float, max: Float, step: Float, default: Float,
        handler: (Float) -> Unit,
        messageProvider: () -> String
    ) {
        this.row().align(Align.left)
        val label = Label(messageProvider(), this@SlidersStage.skin)
        this.add(label)

        this.row().align(Align.left)
        val slider = Slider(min, max, step, false, this@SlidersStage.skin)
        slider.value = default
        slider.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                if (slider.isDragging) {
                    handler(slider.value)
                    label.setText(messageProvider())
                }
            }
        })
        this.add(slider)
    }

    override fun act(delta: Float) {
        super.act(delta)

        setTotal(field.organics.count() + field.minerals.count())
        setMinerals(field.minerals.count())
        setStationary(field.organics.count { !it.dna.canMove })
        setMobile(field.organics.count { it.dna.canMove })
    }

    fun setGeneralInfo(fps: Int, ticsPassed: Long) {
        generalInfo.setText("FPS:  $fps. Tics: %,d".format(ticsPassed))
    }

    private fun setTotal(total: Int) {
        totalLabel.setText("Total: $total")
    }

    private fun setMinerals(minerals: Int) {
        mineralsLabel.setText("Minerals: $minerals")
    }

    private fun setStationary(value: Int) {
        stationaryLabel.setText("Stationary: $value")
    }

    private fun setMobile(value: Int) {
        mobileLabel.setText("Mobile: $value")
    }

    override fun dispose() {
        skin.dispose()
        super.dispose()
    }
}
