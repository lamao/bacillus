package com.invenit.bacillus.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Slider
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.invenit.bacillus.Settings


/**
 * Created by vyacheslav.mischeryakov
 * Created 25.11.2021
 */
class SlidersStage : Stage() {

    private val generalInfo: Label
    private val totalLabel: Label
    private val mineralsLabel: Label
    private val stationaryLabel: Label
    private val mobileLabel: Label

    private val ticDelayLabel: Label
    private val ticDelaySlider: Slider
    private val mutationRateLabel: Label
    private val mutationRateSlider: Slider

    private var skin: Skin = Skin(Gdx.files.internal("uiskin.json"))

    init {

        val table = Table()
        table.setSize(300f, Gdx.graphics.height.toFloat())
        table.align(Align.topLeft)

        table.debug()
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

        table.row().align(Align.left)
        ticDelayLabel = Label("", skin)
        refreshTicDelay()
        table.add(ticDelayLabel)

        ticDelaySlider = Slider(0f, 3f, 0.01f, false, skin)
        ticDelaySlider.value = Settings.TicDelaySeconds
        ticDelaySlider.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                if (ticDelaySlider.isDragging) {
                    Settings.TicDelaySeconds = ticDelaySlider.value
                    refreshTicDelay()
                }
            }
        })
        table.add(ticDelaySlider)

        table.row().align(Align.left)
        mutationRateLabel = Label("", skin)
        refreshMutationRate()
        table.add(mutationRateLabel)

        mutationRateSlider = Slider(0f, 0.5f, 0.005f, false, skin)
        mutationRateSlider.value = Settings.MutationRate
        mutationRateSlider.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                if (mutationRateSlider.isDragging) {
                    Settings.MutationRate = mutationRateSlider.value
                    refreshMutationRate()
                }
            }
        })
        table.add(mutationRateSlider)

        table.layout()
    }

    fun setGeneralInfo(fps: Int, ticsPassed: Long) {
        generalInfo.setText("FPS:  $fps. Tics: $ticsPassed")
    }

    fun setTotal(total: Int) {
        totalLabel.setText("Total: $total")
    }

    fun setMinerals(minerals: Int) {
        mineralsLabel.setText("Minerals: $minerals")
    }

    fun setStationary(value: Int) {
        stationaryLabel.setText("Stationary: $value")
    }

    fun setMobile(value: Int) {
        mobileLabel.setText("Mobile: $value")
    }

    private fun refreshTicDelay() {
        ticDelayLabel.setText("Tic Delay: %.2f s".format(Settings.TicDelaySeconds))
    }

    private fun refreshMutationRate() {
        mutationRateLabel.setText("Mutation Rate: %.3f %%".format(Settings.MutationRate))
    }

    override fun dispose() {
        skin.dispose()
        super.dispose()
    }
}