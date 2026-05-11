package com.invenit.bacillus.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.Field

/**
 * Created by viacheslav.mishcheriakov
 */
class StatisticsStage(val field: Field) : Stage() {

    private val fpsValueLabel: Label
    private val ticsValueLabel: Label
    private val totalValueLabel: Label
    private val mineralsValueLabel: Label
    private val stationaryValueLabel: Label
    private val mobileValueLabel: Label

    private var skin: Skin = Skin(Gdx.files.internal("uiskin.json"))

    init {
        val table = Table()
        table.setSize(200f, 150f)
        table.setPosition(Settings.Width.toFloat() + 20f, Settings.Height.toFloat() - 20f, Align.topLeft)
        table.align(Align.topLeft)

        addActor(table)

        table.row().align(Align.left)
        table.add(Label("FPS:", skin)).left()
        fpsValueLabel = Label("", skin)
        table.add(fpsValueLabel).left().padLeft(10f)
        table.add().expandX()

        table.row().align(Align.left)
        table.add(Label("Tics:", skin)).left()
        ticsValueLabel = Label("", skin)
        table.add(ticsValueLabel).left().padLeft(10f)
        table.add().expandX()

        table.row().align(Align.left)
        table.add(Label("Total:", skin)).left()
        totalValueLabel = Label("", skin)
        table.add(totalValueLabel).left().padLeft(10f)
        table.add().expandX()

        table.row().align(Align.left)
        table.add(Label("Minerals:", skin)).left()
        mineralsValueLabel = Label("", skin)
        table.add(mineralsValueLabel).left().padLeft(10f)
        table.add().expandX()

        table.row().align(Align.left)
        table.add(Label("Stationary:", skin)).left()
        stationaryValueLabel = Label("", skin)
        table.add(stationaryValueLabel).left().padLeft(10f)
        table.add().expandX()

        table.row().align(Align.left)
        table.add(Label("Mobile:", skin)).left()
        mobileValueLabel = Label("", skin)
        table.add(mobileValueLabel).left().padLeft(10f)
        table.add().expandX()
    }

    override fun act(delta: Float) {
        super.act(delta)

        setTotal(field.organics.count() + field.minerals.count())
        setMinerals(field.minerals.count())
        setStationary(field.organics.count { !it.dna.canMove })
        setMobile(field.organics.count { it.dna.canMove })
    }

    fun setGeneralInfo(fps: Int, ticsPassed: Long) {
        fpsValueLabel.setText(fps.toString())
        ticsValueLabel.setText("%,d".format(ticsPassed))
    }

    private fun setTotal(total: Int) {
        totalValueLabel.setText(total.toString())
    }

    private fun setMinerals(minerals: Int) {
        mineralsValueLabel.setText(minerals.toString())
    }

    private fun setStationary(value: Int) {
        stationaryValueLabel.setText(value.toString())
    }

    private fun setMobile(value: Int) {
        mobileValueLabel.setText(value.toString())
    }

    override fun dispose() {
        skin.dispose()
        super.dispose()
    }
}
