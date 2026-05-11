package com.invenit.bacillus.ui

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.Field
import com.invenit.bacillus.model.Point
import com.invenit.bacillus.service.CreatureFactory

/**
 * Created by viacheslav.mishcheriakov
 * Created 25.11.2021
 */
class UserInputListener(
    val field: Field,
    val viewport: Viewport,
    private val creatureFactory: CreatureFactory
) : InputAdapter() {

    private var ctrlPressed = false
    private var altPressed = false

    override fun keyDown(keycode: Int): Boolean {
        return when (keycode) {
            Input.Keys.CONTROL_LEFT -> {
                ctrlPressed = true
                true
            }
            Input.Keys.ALT_LEFT -> {
                altPressed = true
                true
            }
            else -> false
        }

    }

    override fun keyUp(keycode: Int): Boolean {
        return when (keycode) {
            Input.Keys.CONTROL_LEFT -> {
                ctrlPressed = false
                true
            }
            Input.Keys.ALT_LEFT -> {
                altPressed = false
                true
            }
            else -> false
        }
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return onMouseClick(screenX, screenY)
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return onMouseClick(screenX, screenY)
    }

    private fun onMouseClick(screenX: Int, screenY: Int): Boolean {
        // If the stage containing this listener is not the only processor, 
        // we might need to be careful, but InputMultiplexer handles order.
        
        val position = fromDisplay(screenX, screenY)
        return if (field.isOutside(position)) {
            true
        } else if (ctrlPressed) {
            addCell(position)
        } else if (altPressed) {
            replaceCell(position)
        } else {
            false
        }
    }

    private fun addCell(position: Point): Boolean {
        if (!field.isFree(position)) {
            return false
        }
        field.add(creatureFactory.createOrganic(position))
        return true
    }

    private fun fromDisplay(screenX: Int, screenY: Int): Point {
        val touchPoint = Vector2(screenX.toFloat(), screenY.toFloat())
        viewport.unproject(touchPoint)
        return Point(
            touchPoint.x.toInt() / Settings.CellSize,
            touchPoint.y.toInt() / Settings.CellSize
        )
    }

    private fun replaceCell(position: Point): Boolean {
        if (!field.isFree(position)) {
            field.remove(position)
        }
        field.add(creatureFactory.createOrganic(position))
        return true
    }
}