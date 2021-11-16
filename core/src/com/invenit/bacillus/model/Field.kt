package com.invenit.bacillus.model

import com.badlogic.gdx.math.MathUtils
import com.invenit.bacillus.Settings

/**
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */
class Field(val width: Int, val height: Int) {

    val grid: Array<Array<Bacillus?>> = Array(height) { arrayOfNulls<Bacillus?>(width) }

    val bacilli: MutableList<Bacillus> = mutableListOf()

    fun doTic() {

        bacilli.filter { it.health < 0 }
            .forEach(this::killBacillus)

        moveBacilli()

        for (bacillus in bacilli) {
            bacillus.health--
            bacillus.direction = getRandomFreeDirection(bacillus.position)
        }

        if (MathUtils.random(1f) < Settings.ProbabilityToSpawn) {
            spawnCreature()
        }
    }

    fun spawnCreature(): Bacillus {
        val position = getRandomFreePosition()

        val bacillus = Bacillus(
            position = position,
            direction = getRandomFreeDirection(position)
        )
        bacilli.add(bacillus)
        grid[position.y][position.x] = bacillus

        return bacillus
    }

    private fun getRandomFreePosition(): Point {
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

    private fun getRandomFreeDirection(position: Point): Point {
        val direction = Point(
            x = MathUtils.random(-1, 1),
            y = MathUtils.random(-1, 1)
        )

        val newPosition = position + direction
        if (isOutside(newPosition) || !isFree(newPosition)) {
            return Point(0, 0)
        }

        return direction
    }

    private fun isOutside(position: Point): Boolean {
        return position.x < 0 || position.x >= width
                || position.y < 0 || position.y >= height
    }

    private fun fitInside(position: Point): Point {
        val x = when {
            position.x < 0 -> {
                0
            }
            position.x >= width -> {
                width - 1
            }
            else -> {
                position.x
            }
        }

        val y = when {
            position.y < 0 -> {
                0
            }
            position.y >= height -> {
                height - 1
            }
            else -> {
                position.y
            }
        }

        return Point(x, y)
    }

    private fun moveBacilli() {
        for (bacillus in bacilli) {
            var newPosition = bacillus.position + bacillus.direction
            newPosition = when {
                isFree(newPosition) -> {
                    newPosition
                }
                else -> {
                    bacillus.position
                }
            }

            grid[bacillus.position.y][bacillus.position.x] = null
            grid[newPosition.y][newPosition.x] = bacillus
            bacillus.position = newPosition
        }
    }

    private fun isFree(position: Point): Boolean = grid[position.y][position.x] == null

    private fun killBacillus(bacillus: Bacillus) {
        bacilli.remove(bacillus)
        grid[bacillus.position.y][bacillus.position.x] = null
    }
}