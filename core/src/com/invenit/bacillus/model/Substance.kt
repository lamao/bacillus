package com.invenit.bacillus.model

import com.badlogic.gdx.graphics.Color

/**
 *
 * @author vyacheslav.mischeryakov
 * Created: 16.11.21
 */
enum class Substance(val color: Color) {
    Nothing(Color.BLACK),
    Protein(Color.BLUE),
    Cellulose(Color.GREEN)
}