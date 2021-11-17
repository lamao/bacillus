package com.invenit.bacillus.model

import com.badlogic.gdx.math.MathUtils
import com.invenit.bacillus.Settings
import java.lang.Integer.max
import java.lang.Integer.min
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */
class Field(val width: Int, val height: Int) {

    companion object {
        val NoDirection: Point = Point(0, 0)
    }

    val grid: Array<Array<Organic?>> = Array(height) { arrayOfNulls<Organic?>(width) }

    val organics: MutableList<Organic> = mutableListOf()

    fun doTic() {

        organics
            .filter { it.energy < 0 || it.age >= Settings.MaxAge || MathUtils.random() < Settings.UnexpectedDeathRate }
            .forEach(this::kill)

        reproduceOrganics()
        moveOrganics()

        organics.filter { it.consume == Substance.Nothing }
            .forEach { it.energy = min(it.energy + 2, Settings.MaxHealth) }
        organics.forEach {
            it.energy = min(it.energy + it.getSuitableProducedSubstanceAmount(), Settings.MaxHealth)
        }

        organics.forEach {
            it.energy--
            val directionToFood = it.getDirectionToFood()
            it.direction = if (directionToFood == NoDirection) {
                getRandomFreeDirection(it.position, it.canMove)
            } else {
                directionToFood
            }

            if (it.direction != NoDirection) {
                it.energy--
            }
        }

        organics.forEach {
            it.age++
        }

    }

    private fun Organic.getDirectionToFood(): Point {
        if (!this.canMove) {
            return NoDirection
        }

        for (x in max(this.position.x - Settings.VisionRange, 0)..min(
            this.position.x + Settings.VisionRange,
            width - 1
        )) {
            for (y in max(this.position.y - Settings.VisionRange, 0)..min(
                this.position.y + Settings.VisionRange,
                height - 1
            )) {
                if (grid[y][x]?.body == this.consume && (x != this.position.x || y != this.position.y)) {
                    return Point(x - this.position.x, y - this.position.y)
                }
            }
        }

        return NoDirection
    }

    private fun Organic.getSuitableProducedSubstanceAmount(): Int {

        var result = 0f

        for (x in max(this.position.x - Settings.ConsumingRange, 0)..min(
            this.position.x + Settings.ConsumingRange,
            width - 1
        )) {
            for (y in max(this.position.y - Settings.ConsumingRange, 0)..min(
                this.position.y + Settings.ConsumingRange,
                height - 1
            )) {
                val something = grid[y][x]
                if (something?.produce == this.consume) {
                    val distance = max(abs(x - this.position.x), abs(y - this.position.y))
                    result += 1f / 2f.pow(distance - 1)
                }
            }
        }

        return result.roundToInt()
    }


    private fun reproduceOrganics() {

        val offspings = organics
            .filter { it.energy >= Settings.ReproductionThreshold }
            .mapNotNull { it.split() }
            .toList()

        organics.addAll(offspings)
    }

    fun spawn(body: Substance, consume: Substance, produce: Substance, movable: Boolean): Organic {
        val position = getRandomFreePosition()

        val bacillus = Organic(
            position = position,
            direction = getRandomFreeDirection(position, movable),
            energy = getRandomHealth(),
            body = body,
            consume = consume,
            produce = produce,
            canMove = movable
        )
        organics.add(bacillus)
        grid[position.y][position.x] = bacillus

        return bacillus
    }

    private fun Organic.split(): Organic? {
        val offspringOffset = Point(
            MathUtils.random(-Settings.ReproductionRange, Settings.ReproductionRange),
            MathUtils.random(-Settings.ReproductionRange, Settings.ReproductionRange)
        )

        val offspingHealth = getRandomHealth()

        this.energy -= offspingHealth

        val offspingPosition = this.position + offspringOffset
        if (isOutside(offspingPosition) || !isFree(offspingPosition)) {
            this.energy += (offspingHealth * Settings.ReturnHealthWhenReproductionFails).roundToInt()
            return null
        }

        val offsping = cloneWithMutation(offspingPosition, offspingHealth)
        grid[offspingPosition.y][offspingPosition.x] = offsping
        return offsping
    }

    private fun Organic.cloneWithMutation(
        offspingPosition: Point,
        offspingHealth: Int
    ): Organic {


        var body = this.body
        var consume = this.consume
        var produce = this.produce
        var canMove = this.canMove
        if (MathUtils.random() < Settings.MutationRate) {
            // TODO: Refactor
            when (MathUtils.random(0, 3)) {
                0 -> {
                    body = Substance.getRandomBody()
                }
                1 -> {
                    do {
                        consume = Substance.getRandomConsume()
                    } while (consume == produce)
                }
                2 -> {
                    do {
                        produce = Substance.getRandomProduce()
                    } while (produce == consume)
                }
                3 -> {
                    canMove = MathUtils.randomBoolean()
                }
            }

        }

        return Organic(
            position = offspingPosition,
            direction = getRandomFreeDirection(offspingPosition, canMove),
            energy = offspingHealth,
            body = body,
            consume = consume,
            produce = produce,
            canMove = canMove
        )
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

    private fun getRandomFreeDirection(position: Point, movable: Boolean): Point {
        if (!movable) {
            return Point(0, 0)
        }

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

    private fun moveOrganics() {
        val offspings = mutableListOf<Organic>()
        for (cell in organics) {
            var newPosition = cell.position + cell.direction
            newPosition = when {
                isFree(newPosition) -> {
                    newPosition
                }
                getSomething(newPosition)?.body == cell.consume -> {
                    val food = getSomething(newPosition)!!
                    food.energy -= Settings.AttackDamage
                    cell.energy = min(cell.energy + Settings.AttackDamage, Settings.MaxHealth)

                    cell.position
                }
                else -> {
                    cell.position
                }
            }

            grid[cell.position.y][cell.position.x] = null
            grid[newPosition.y][newPosition.x] = cell
            cell.position = newPosition
        }

        organics.addAll(offspings)
    }

    private fun isFree(position: Point): Boolean = getSomething(position) == null

    private fun getSomething(position: Point) = grid[position.y][position.x]

    private fun kill(organic: Organic) {
        organics.remove(organic)
        grid[organic.position.y][organic.position.x] = null
    }

}
