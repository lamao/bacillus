package com.invenit.bacillus.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

/**
 * A 1x1 solid-[color] drawable backed by its own [Texture] — used for [CellDetailsStage]'s
 * cell backgrounds and [LegendDialog]'s color swatches. The caller owns disposing the
 * returned drawable's texture (`drawable.region.texture.dispose()`).
 */
fun coloredDrawable(color: Color): TextureRegionDrawable {
    val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
    pixmap.setColor(color)
    pixmap.fill()
    val texture = Texture(pixmap)
    pixmap.dispose()
    return TextureRegionDrawable(TextureRegion(texture))
}
