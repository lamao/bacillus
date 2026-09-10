package com.invenit.bacillus.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Disposable
import com.invenit.bacillus.model.matrix.Sensor

private const val SWATCH_SIZE = 14f

/**
 * Explains the Decision Matrix's badge color/icon language (#36): what each action
 * badge and sensor badge color stands for, and how the condition chevron and jump
 * glyph read. Opened from [CellDetailsStage]'s "?" button; built once and reused.
 */
class LegendDialog(private val skin: Skin) : Dialog("Decision Matrix Legend", skin), Disposable {

    private val swatchTextures = mutableListOf<Texture>()

    init {
        val content = contentTable
        content.defaults().pad(4f).left()

        addSectionTitle(content, "Actions")
        for (icon in ActionIcon.entries) {
            addSwatchRow(content, icon.name, icon.badgeColor())
        }

        addSectionTitle(content, "Sensors")
        for (sensor in Sensor.entries) {
            addSwatchRow(content, sensor.name, sensor.badgeColor())
        }

        addSectionTitle(content, "Condition")
        content.add(Label("Chevron up (>=): passes at or above the threshold", skin)).colspan(2).row()
        content.add(Label("Chevron down (<): passes below the threshold", skin)).colspan(2).row()

        addSectionTitle(content, "Jump")
        content.add(Label("Right arrow: jumps forward when the test passes", skin)).colspan(2).row()
        content.add(Label("Left arrow: jumps backward when the test passes", skin)).colspan(2).row()
        content.add(Label("Dot: stays on the next state (offset 0)", skin)).colspan(2).row()

        button("Close")
    }

    private fun addSectionTitle(content: Table, title: String) {
        content.add(Label(title, skin)).colspan(2).padTop(8f).row()
    }

    private fun addSwatchRow(content: Table, name: String, color: Color) {
        val drawable = coloredDrawable(color)
        swatchTextures.add(drawable.region.texture)
        content.add(Image(drawable)).size(SWATCH_SIZE)
        content.add(Label(name, skin)).left().row()
    }

    override fun dispose() {
        swatchTextures.forEach { it.dispose() }
        swatchTextures.clear()
    }
}
