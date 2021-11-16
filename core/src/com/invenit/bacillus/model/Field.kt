package com.invenit.bacillus.model

import com.badlogic.gdx.math.MathUtils
import com.invenit.bacillus.Settings

/**
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */
class Field(val width: Int, val height: Int) {

    val grid: Array<Array<Something?>> = Array(height) { arrayOfNulls<Something?>(width) }

    val bacilli: MutableList<Bacillus> = mutableListOf()

    val foods: MutableList<Something> = mutableListOf()

    fun doTic() {

        bacilli.filter { it.health < 0 }
            .forEach(this::killBacillus)

        foods.filter { it.health < 0 }
            .forEach(this::killFood)

        moveBacilli()

        for (bacillus in bacilli) {
            bacillus.health--
            bacillus.direction = getRandomFreeDirection(bacillus.position)
        }

        for (food in foods) {
            food.health--
        }

        if (MathUtils.random(1f) < Settings.ProbabilityToSpawnBacillus) {
            spawnBacilli()
        }
        if (MathUtils.random(1f) < Settings.ProbabilityToSpawnFood) {
            spawnFood()
        }
    }

    fun spawnBacilli(): Bacillus {
        val position = getRandomFreePosition()

        val bacillus = Bacillus(
            position = position,
            direction = getRandomFreeDirection(position),
            health = getRandomHealth()
        )
        bacilli.add(bacillus)
        grid[position.y][position.x] = bacillus

        return bacillus
    }

    fun spawnFood(): Something {
        val position = getRandomFreePosition()
        val food = Something(
            position = position,
            health = getRandomHealth()
        )
        foods.add(food)
        grid[position.y][position.x] = food

        return food
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

    private fun getRandomHealth() =
        Settings.DefaultHealth + MathUtils.random(-Settings.DefaultHealth / 4, Settings.DefaultHealth / 4)

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
    private fun killFood(food: Something) {
        foods.remove(food)
        grid[food.position.y][food.position.x] = null
    }

}