package com.invenit.bacillus.model

import com.invenit.bacillus.FieldException

/**
 * Created by viacheslav.mishcheriakov
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
    fun isOutside(x: Int, y: Int): Boolean = x !in 0..<width || y !in 0..<height
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

    // Internal, not private: a public inline function (iterateRadial) can't
    // call a private member (inlining would copy that call to sites outside
    // this class, where a private symbol isn't resolvable) - @PublishedApi
    // internal is the standard escape hatch, keeping these out of Field's
    // public API while still letting the compiler inline through them.
    //
    // No bounds checking - only safe when the whole ring at `range` is
    // guaranteed inside the grid (see isNearSides). Called directly with an
    // anchor that isn't, this throws ArrayIndexOutOfBoundsException.
    @PublishedApi
    internal inline fun iterateRadialSimple(anchor: Point, range: Int, action: (x: Int, y: Int) -> Boolean) {
        for (step in 1..range) {

            val upperY = anchor.y + step
            val bottomY = anchor.y - step
            val leftX = anchor.x - step
            val rightX = anchor.x + step

            for (x in leftX..rightX) {
                if (!action(x, upperY)) {
                    return
                }
            }

            for (y in bottomY until upperY) {
                if (!action(rightX, y)) {
                    return
                }
            }

            for (x in leftX until rightX) {
                if (!action(x, bottomY)) {
                    return
                }
            }

            for (y in bottomY + 1 until upperY) {
                if (!action(leftX, y)) {
                    return
                }
            }
        }
    }

    @PublishedApi
    internal inline fun iterateRadialNearSides(anchor: Point, range: Int, action: (x: Int, y: Int) -> Boolean) {
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

    /**
     * Iterate over the cells in a radial frame around the given anchor point.
     * Marked `inline` so callers' captured accumulator `var`s (e.g. `result`,
     * `waste`, `totalDamage`) stay plain locals instead of being boxed into
     * heap-allocated Ref wrappers - this runs per organic per tick. Dispatches
     * to the bounds-check-free path when the whole ring is guaranteed inside
     * the grid, and to the checked path otherwise.
     * @param anchor The center of the frame.
     * @param range The radius of the frame.
     * @param action The action to perform on each cell. Return false to stop iterating.
     */
    inline fun iterateRadial(anchor: Point, range: Int, action: (x: Int, y: Int) -> Boolean) {
        if (isNearSides(anchor, range)) {
            iterateRadialNearSides(anchor, range, action)
        } else {
            iterateRadialSimple(anchor, range, action)
        }
    }

    @PublishedApi
    internal fun isNearSides(anchor: Point, range: Int): Boolean =
        anchor.x < range || anchor.x + range >= width
            || anchor.y < range || anchor.y + range >= height

}
