package com.invenit.bacillus.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.*
import kotlin.math.max
import kotlin.math.min

/**
 * Created by vyacheslav.mischeryakov
 * Created 25.11.2021
 */
class UserInputListener(val field: Field) : InputAdapter() {

    private var ctrlPressed = false
    private var altPressed = false

    override fun keyDown(keycode: Int): Boolean {
        return when (keycode) {
            Input.Keys.NUMPAD_ADD -> {
                Settings.TicDelaySeconds = min(3f, Settings.TicDelaySeconds + 0.01f)
                true
            }
            Input.Keys.NUMPAD_SUBTRACT -> {
                Settings.TicDelaySeconds = max(0f, Settings.TicDelaySeconds - 0.01f)
                true
            }
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
        val position = Point(Gdx.input.x, Gdx.input.y).fromDisplay()
        return if (ctrlPressed) {
            addCell(position)
        } else if (altPressed) {
            replaceCell(position)
        } else {
            false
        }
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        val position = Point(Gdx.input.x, Gdx.input.y).fromDisplay()
        return if (ctrlPressed) {
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

    private fun Point.fromDisplay() = Point(
        (this.x) / Settings.CellSize,
        (Settings.Height - this.y) / Settings.CellSize
    )

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
            Substance.getRandomBody(),
            Substance.getRandomConsume(),
            Substance.getRandomProduce(),
            Substance.getRandomToxin(),
            false
        )
    )
}