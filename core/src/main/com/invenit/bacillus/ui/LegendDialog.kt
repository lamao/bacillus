package com.invenit.bacillus.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Disposable
import com.invenit.bacillus.model.matrix.Sensor

private const val ICON_TEXTURE_SIZE = 40
private const val ICON_GLYPH_SIZE = 22f
private const val ICON_DISPLAY_SIZE = 30f
private const val BADGE_INSET = 2f
private const val CONTENT_WIDTH = 380f
private const val CONTENT_HEIGHT = 620f

/**
 * Explains the Decision Matrix's badge color/icon language (#36): what each action badge and
 * sensor badge stands for, and how the condition chevron and jump glyph read. Every icon here
 * is rendered with [DecisionMatrixRenderer] — the same drawing code the live grid in
 * [CellDetailsStage] uses — so the legend's shapes always match what's on screen. Opened from
 * [CellDetailsStage]'s "?" button; built once and reused.
 */
class LegendDialog(private val skin: Skin) : Dialog("Decision Matrix Legend", skin), Disposable {

    private val iconTextures = mutableListOf<Texture>()
    private val iconFrameBuffer = FrameBuffer(Pixmap.Format.RGBA8888, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE, false)
    private val iconShapeRenderer = ShapeRenderer()

    init {
        val body = Table()
        body.defaults().pad(4f).left()

        addSectionTitle(body, "Actions")
        for (icon in ActionIcon.entries) {
            addIconRow(body, icon.name) { sr, cx, cy ->
                DecisionMatrixRenderer.drawBadge(sr, cx, cy, ICON_TEXTURE_SIZE.toFloat(), ICON_TEXTURE_SIZE.toFloat(), BADGE_INSET, icon.badgeColor())
                DecisionMatrixRenderer.drawActionIcon(sr, icon, cx, cy, ICON_GLYPH_SIZE, IconStrokeColor)
            }
        }

        addSectionTitle(body, "Sensors")
        for (sensor in Sensor.entries) {
            addIconRow(body, sensor.name) { sr, cx, cy ->
                DecisionMatrixRenderer.drawBadge(sr, cx, cy, ICON_TEXTURE_SIZE.toFloat(), ICON_TEXTURE_SIZE.toFloat(), BADGE_INSET, sensor.badgeColor())
                DecisionMatrixRenderer.drawSensorGlyph(sr, sensor.toGlyph(), cx, cy, ICON_GLYPH_SIZE, IconStrokeColor)
            }
        }

        addSectionTitle(body, "Condition")
        addIconRow(body, "passes at or above the threshold") { sr, cx, cy ->
            DecisionMatrixRenderer.drawBadge(sr, cx, cy, ICON_TEXTURE_SIZE.toFloat(), ICON_TEXTURE_SIZE.toFloat(), BADGE_INSET, NeutralCellColor)
            DecisionMatrixRenderer.drawChevron(sr, ChevronDirection.Up, cx, cy, ICON_GLYPH_SIZE, IconStrokeColor)
        }
        addIconRow(body, "passes below the threshold") { sr, cx, cy ->
            DecisionMatrixRenderer.drawBadge(sr, cx, cy, ICON_TEXTURE_SIZE.toFloat(), ICON_TEXTURE_SIZE.toFloat(), BADGE_INSET, NeutralCellColor)
            DecisionMatrixRenderer.drawChevron(sr, ChevronDirection.Down, cx, cy, ICON_GLYPH_SIZE, IconStrokeColor)
        }

        addSectionTitle(body, "Jump")
        addIconRow(body, "jumps forward when the test passes") { sr, cx, cy ->
            DecisionMatrixRenderer.drawBadge(sr, cx, cy, ICON_TEXTURE_SIZE.toFloat(), ICON_TEXTURE_SIZE.toFloat(), BADGE_INSET, NeutralCellColor)
            DecisionMatrixRenderer.drawJumpGlyph(sr, JumpDirection.Forward, cx, cy, ICON_GLYPH_SIZE, JumpGlyphColor)
        }
        addIconRow(body, "jumps backward when the test passes") { sr, cx, cy ->
            DecisionMatrixRenderer.drawBadge(sr, cx, cy, ICON_TEXTURE_SIZE.toFloat(), ICON_TEXTURE_SIZE.toFloat(), BADGE_INSET, NeutralCellColor)
            DecisionMatrixRenderer.drawJumpGlyph(sr, JumpDirection.Backward, cx, cy, ICON_GLYPH_SIZE, JumpGlyphColor)
        }
        addIconRow(body, "stays on the next state (offset 0)") { sr, cx, cy ->
            DecisionMatrixRenderer.drawBadge(sr, cx, cy, ICON_TEXTURE_SIZE.toFloat(), ICON_TEXTURE_SIZE.toFloat(), BADGE_INSET, NeutralCellColor)
            DecisionMatrixRenderer.drawJumpGlyph(sr, JumpDirection.Neutral, cx, cy, ICON_GLYPH_SIZE, JumpGlyphColor)
        }

        val scrollPane = ScrollPane(body, skin)
        scrollPane.setFadeScrollBars(false)
        contentTable.add(scrollPane).width(CONTENT_WIDTH).height(CONTENT_HEIGHT)

        button("Close")
    }

    private fun addSectionTitle(body: Table, title: String) {
        body.add(Label(title, skin)).colspan(2).padTop(8f).row()
    }

    private fun addIconRow(body: Table, description: String, draw: (ShapeRenderer, Float, Float) -> Unit) {
        body.add(Image(renderIcon(draw))).size(ICON_DISPLAY_SIZE)
        body.add(Label(description, skin)).left().row()
    }

    /** Rasterizes one icon offscreen with [DecisionMatrixRenderer], so the legend always shows the exact shape the live grid draws. */
    private fun renderIcon(draw: (ShapeRenderer, Float, Float) -> Unit): TextureRegionDrawable {
        val size = ICON_TEXTURE_SIZE.toFloat()
        iconFrameBuffer.begin()
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        iconShapeRenderer.projectionMatrix = Matrix4().setToOrtho2D(0f, 0f, size, size)
        iconShapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        draw(iconShapeRenderer, size / 2f, size / 2f)
        iconShapeRenderer.end()
        val pixmap = Pixmap.createFromFrameBuffer(0, 0, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE)
        iconFrameBuffer.end()

        val texture = Texture(pixmap)
        pixmap.dispose()
        iconTextures.add(texture)
        val region = TextureRegion(texture)
        region.flip(false, true)
        return TextureRegionDrawable(region)
    }

    override fun dispose() {
        iconShapeRenderer.dispose()
        iconFrameBuffer.dispose()
        iconTextures.forEach { it.dispose() }
        iconTextures.clear()
    }
}
