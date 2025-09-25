package com.invenit.bacillus.ui

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.Vector3
import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.*
import com.invenit.bacillus.service.MutationService
import kotlin.math.roundToInt

/**
 * Created by viacheslav.mishcheriakov
 * Created 25.11.2021
 */
class UserInputListener(
    val field: Field,
    val camera: Camera,
    private val mutationService: MutationService
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
        field.add(createRandomCell(position))
        return true
    }

    private fun fromDisplay(screenX: Int, screenY: Int): Point {
        val touchPoint = Vector3(screenX.toFloat(), screenY.toFloat(), 0f)
        camera.unproject(touchPoint)
        return Point(
            touchPoint.x.roundToInt() / Settings.CellSize,
            touchPoint.y.roundToInt() / Settings.CellSize
        )
    }

    private fun replaceCell(position: Point): Boolean {
        if (!field.isFree(position)) {
            field.remove(position)
        }
        field.add(createRandomCell(position))
        return true
    }

    private fun createRandomCell(position: Point) = Organic(
        position,
        Settings.DefaultSize,
        Point(0, 0),
        DNA(
            mutationService.randomBody(),
            mutationService.randomConsume(),
            mutationService.randomProduce(),
            mutationService.randomToxin(),
            false
        )
    )
}