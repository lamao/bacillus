package com.invenit.bacillus.model

import com.badlogic.gdx.math.MathUtils
import com.invenit.bacillus.Settings
import java.lang.Integer.min
import kotlin.math.roundToInt

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

        val foodOffsprings = mutableListOf<Something>()
        for (food in foods) {
            food.health = min(food.health + 1, Settings.MaxHealth)
            if (food.health >= Settings.ReproductionThreshold) {
                val offspring = food.split()
                if (offspring != null) {
                    foodOffsprings.add(offspring)
                }
            }
        }
        foods.addAll(foodOffsprings)

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

    private fun Bacillus.split(): Bacillus? {
        val offspringOffset = Point(
            MathUtils.random(-Settings.ReproductionRange, Settings.ReproductionRange),
            MathUtils.random(-Settings.ReproductionRange, Settings.ReproductionRange)
        )

        val offspingHealth = getRandomHealth()

        this.health -= offspingHealth

        val offspingPosition = this.position + offspringOffset
        if (isOutside(offspingPosition) || !isFree(offspingPosition)) {
            this.health += (offspingHealth * Settings.ReturnHealthWhenReproductionFails).roundToInt()
            return null
        }

        val offsping = Bacillus(
            position = offspingPosition,
            direction = getRandomFreeDirection(offspingPosition),
            health = offspingHealth
        )
        grid[offspingPosition.y][offspingPosition.x] = offsping
        return offsping
    }

    private fun Something.split(): Something? {
        val offspringOffset = Point(
            MathUtils.random(-Settings.ReproductionRange, Settings.ReproductionRange),
            MathUtils.random(-Settings.ReproductionRange, Settings.ReproductionRange)
        )

        val offspingHealth = getRandomHealth()

        this.health -= offspingHealth
        val offspingPosition = this.position + offspringOffset
        if (isOutside(offspingPosition) || !isFree(offspingPosition)) {
            this.health += (offspingHealth * Settings.ReturnHealthWhenReproductionFails).roundToInt()
            return null
        }

        val offsping = Something(
            position = offspingPosition,
            health = offspingHealth
        )
        grid[offspingPosition.y][offspingPosition.x] = offsping
        return offsping
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
        if (isOutside(newPosition)) {
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
        val offspings = mutableListOf<Bacillus>()
        for (bacillus in bacilli) {
            var newPosition = bacillus.position + bacillus.direction
            newPosition = when {
                isFree(newPosition) -> {
                    newPosition
                }
                getSomething(newPosition) !is Bacillus -> {
                    val food = getSomething(newPosition)!!
                    food.health -= Settings.AttackDamage
                    bacillus.health = min(bacillus.health + Settings.AttackDamage, Settings.MaxHealth)

                    bacillus.position
                }
                else -> {
                    bacillus.position
                }
            }

            grid[bacillus.position.y][bacillus.position.x] = null
            grid[newPosition.y][newPosition.x] = bacillus
            bacillus.position = newPosition

            if (bacillus.health >= Settings.ReproductionThreshold) {
                val offsping = bacillus.split()
                if (offsping != null) {
                    offspings.add(offsping)
                }
            }
        }

        bacilli.addAll(offspings)
    }

    private fun isFree(position: Point): Boolean = getSomething(position) == null

    private fun getSomething(position: Point) = grid[position.y][position.x]

    private fun killBacillus(bacillus: Bacillus) {
        bacilli.remove(bacillus)
        grid[bacillus.position.y][bacillus.position.x] = null
    }

    private fun killFood(food: Something) {
        foods.remove(food)
        grid[food.position.y][food.position.x] = null
    }
}
