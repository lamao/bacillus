package com.invenit.bacillus.model

import com.badlogic.gdx.math.MathUtils
import com.invenit.bacillus.FieldException
import com.invenit.bacillus.stage.*
import com.invenit.bacillus.util.Mutator
import java.lang.Integer.max
import java.lang.Integer.min

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

    private val stages = arrayOf(
        ClearExhaustedItems(),
        MoveStage(),
        SplitStage(),
        AdjustCountersStage(),
        ConsumeStage(),
        ProduceStage(),
        LookUpStage()
    )

    fun doTic() {

        // TODO: Move out of the Field
        for (stage in stages) {
            stage.execute(this)
        }

    }

    fun spawn(body: Substance, consume: Substance, produce: Substance, canMove: Boolean): Organic {
        val position = getRandomFreePosition()

        val bacillus = Organic(
            position = position,
            direction = if (canMove) getRandomFreeDirection(position) else NoDirection,
            size = Mutator.getRandomSize(),
            body = body,
            consume = consume,
            produce = produce,
            canMove = canMove
        )
        add(bacillus)

        return bacillus
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

    fun isOutside(position: Point): Boolean {
        return position.x < 0 || position.x >= width
                || position.y < 0 || position.y >= height
    }

    fun isFree(position: Point): Boolean = isFree(position.x, position.y)
    fun isFree(x: Int, y: Int): Boolean = get(x, y) == null

    operator fun get(position: Point): Something? = get(position.x, position.y)
    operator fun get(x: Int, y: Int): Something? = grid[y][x]

    private fun putOnGrid(position: Point, something: Something?) {
        grid[position.y][position.x] = something
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


    // TODO: Maybe split into field.getFrame and Util.iterateRadial(anchor, frame, action)
    fun iterateRadial(anchor: Point, range: Int, action: (x: Int, y: Int) -> Boolean) {
        for (step in 1..range) {

            val upperY = min(anchor.y + step, height - 1)
            val bottomY = max(anchor.y - step, 0)
            val leftX = max(anchor.x - step, 0)
            val rightX = min(anchor.x + step, width - 1)
            for (x in leftX..rightX) {
                if (!action(x, upperY)) {
                    return
                }
            }

            for (y in bottomY..upperY) {
                if (!action(rightX, y)) {
                    return
                }
            }


            for (x in leftX..rightX) {
                if (!action(x, bottomY)) {
                    return
                }
            }


            for (y in upperY..bottomY) {
                if (!action(leftX, y)) {
                    return
                }
            }
        }
    }

}
