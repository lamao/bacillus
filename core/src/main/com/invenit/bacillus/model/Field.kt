package com.invenit.bacillus.model

import com.badlogic.gdx.math.MathUtils
import com.invenit.bacillus.FieldException

/**
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */
class Field(val width: Int, val height: Int) {

    companion object {
        val NoDirection: Point = Point(0, 0)
    }

    private val grid: Array<Array<Something?>> = Array(height) { arrayOfNulls<Something?>(width) }

    val organics: MutableList<Organic> = mutableListOf()
    val minerals: MutableList<Mineral> = mutableListOf()

    fun isOutside(position: Point): Boolean = isOutside(position.x, position.y)
    fun isOutside(x: Int, y: Int): Boolean = x < 0 || x >= width || y < 0 || y >= height
    fun isInside(x: Int, y: Int): Boolean = !isOutside(x, y)

    fun isFree(position: Point): Boolean = isFree(position.x, position.y)
    fun isFree(x: Int, y: Int): Boolean = get(x, y) == null

    operator fun get(position: Point): Something? = get(position.x, position.y)
    operator fun get(x: Int, y: Int): Something? = grid[y][x]

    private fun putOnGrid(position: Point, something: Something?) {
        grid[position.y][position.x] = something
    }

    fun add(something: Something) {
        if (!isFree(something.position)) {
            throw FieldException("Cell [${something.position.x},${something.position.y}] is occupied")
        }

        if (something is Organic) {
            organics.add(something)
        } else if (something is Mineral) {
            minerals.add(something)
        }
        putOnGrid(something.position, something)
    }

    fun remove(position: Point) {
        val something = get(position)
        if (something == null) {
            return
        } else {
            if (something is Organic) {
                organics.remove(something)
            } else if (something is Mineral) {
                minerals.remove(something)
            }
            putOnGrid(something.position, null)
        }

    }

    fun relocate(something: Something, target: Point) {
        assert(get(something.position) == something) {
            "Item at [${something.position.x},${something.position.y}] is not a $something"
        }
        assert(get(target) == null) {
            "Can't relocate to [${target.x},${target.y}]. Location is occupied"
        }

        putOnGrid(something.position, null)
        putOnGrid(target, something)
        something.position = target
    }


    fun getRandomFreePosition(): Point {
        var position = getRandomPosition()
        while (!isFree(position)) {
            position = getRandomPosition()
        }

        return position
    }

    private fun getRandomPosition() = Point(
        MathUtils.random(width - 1),
        MathUtils.random(height - 1)
    )

    // TODO: Move out of this class
    fun getRandomFreeDirection(position: Point): Point {
        val direction = Point(
            x = MathUtils.random(-1, 1),
            y = MathUtils.random(-1, 1)
        )

        val newPosition = position + direction
        if (isOutside(newPosition)) {
            return NoDirection
        }

        return direction
    }

    // TODO: Maybe split into field.getFrame and Util.iterateRadial(anchor, frame, action)
    fun iterateRadial(anchor: Point, range: Int, action: (x: Int, y: Int) -> Boolean) {
        for (step in 1..range) {

            val upperY = anchor.y + step
            val bottomY = anchor.y - step
            val leftX = anchor.x - step
            val rightX = anchor.x + step

            for (x in leftX..rightX) {
                if (isInside(x, upperY) && !action(x, upperY)) {
                    return
                }
            }

            for (y in bottomY until upperY) {
                if (isInside(rightX, y) && !action(rightX, y)) {
                    return
                }
            }


            for (x in leftX until rightX) {
                if (isInside(x, bottomY) && !action(x, bottomY)) {
                    return
                }
            }


            for (y in bottomY + 1 until upperY) {
                if (isInside(leftX, y) && !action(leftX, y)) {
                    return
                }
            }
        }
    }

}
