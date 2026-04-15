package com.invenit.bacillus.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.invenit.bacillus.model.DNA
import com.invenit.bacillus.model.Substance
import com.invenit.bacillus.service.CreatureFactory

class AddCreatureDialog(
    private val creatureFactory: CreatureFactory,
    private val onClosed: () -> Unit
) : Dialog("Configure Creature", Skin(Gdx.files.internal("uiskin.json"))) {

    private class SubstanceListCell(val substance: Substance, skin: Skin) : Table() {
        init {
            val pixmap = Pixmap(12, 12, Pixmap.Format.RGBA8888)
            pixmap.setColor(substance.color)
            pixmap.fill()
            val texture = Texture(pixmap)
            pixmap.dispose()
            val drawable = TextureRegionDrawable(TextureRegion(texture))
            add(Image(drawable)).size(12f).padRight(5f)
            add(Label(substance.name, skin))
        }

        override fun toString(): String {
            return substance.name
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SubstanceListCell) return false
            return substance == other.substance
        }

        override fun hashCode(): Int {
            return substance.hashCode()
        }
    }

    private val bodySelect = SelectBox<SubstanceListCell>(skin)
    private val consumeSelect = SelectBox<SubstanceListCell>(skin)
    private val produceSelect = SelectBox<SubstanceListCell>(skin)
    private val toxinSelect = SelectBox<SubstanceListCell>(skin)
    
    private val allSubstances = Substance.values().map { SubstanceListCell(it, skin) }
    private val nonSunSubstances = allSubstances.filter { it.substance != Substance.Sun }
    private val canMoveCheck = CheckBox("Can Move", skin)
    private val sizeSlider = Slider(100f, 2000f, 10f, false, skin)
    private val sizeLabel = Label("", skin)
    
    init {
        val table = contentTable
        table.defaults().pad(5f).left()

        bodySelect.setItems(*nonSunSubstances.toTypedArray())
        consumeSelect.setItems(*allSubstances.toTypedArray())
        produceSelect.setItems(*nonSunSubstances.toTypedArray())
        toxinSelect.setItems(*nonSunSubstances.toTypedArray())

        table.add(Label("Body:", skin))
        table.add(bodySelect).row()
        
        table.add(Label("Consume:", skin))
        table.add(consumeSelect).row()
        
        table.add(Label("Produce:", skin))
        table.add(produceSelect).row()
        
        table.add(Label("Toxin:", skin))
        table.add(toxinSelect).row()
        
        table.add(canMoveCheck).colspan(2).row()
        
        table.add(Label("Initial Size:", skin))
        table.add(sizeSlider).row()
        table.add(sizeLabel).colspan(2).center().row()

        sizeSlider.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                sizeLabel.setText(sizeSlider.value.toInt().toString())
            }
        })

        button("OK", true)
        button("Cancel", false)
        
        loadLastParams()
    }

    private fun loadLastParams() {
        bodySelect.selected = nonSunSubstances.find { it.substance == creatureFactory.lastDNA.body }
        consumeSelect.selected = allSubstances.find { it.substance == creatureFactory.lastDNA.consume }
        produceSelect.selected = nonSunSubstances.find { it.substance == creatureFactory.lastDNA.produce }
        toxinSelect.selected = nonSunSubstances.find { it.substance == creatureFactory.lastDNA.toxin }
        canMoveCheck.isChecked = creatureFactory.lastDNA.canMove
        sizeSlider.value = creatureFactory.lastSize.toFloat()
        sizeLabel.setText(sizeSlider.value.toInt().toString())
    }

    fun showConfiguration(stage: Stage) {
        loadLastParams() 
        show(stage)
    }

    override fun result(`object`: Any?) {
        if (`object` == true) {
            creatureFactory.lastDNA = DNA(
                bodySelect.selected.substance,
                consumeSelect.selected.substance,
                produceSelect.selected.substance,
                toxinSelect.selected.substance,
                canMoveCheck.isChecked
            )

            creatureFactory.lastSize = sizeSlider.value.toInt()
        }
        onClosed()
    }
}
