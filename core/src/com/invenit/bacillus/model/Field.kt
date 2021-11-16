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

    val organics: MutableList<Organic> = mutableListOf()

    fun doTic() {

        organics
            .filter { it.energy < 0 }
            .forEach(this::kill)


        moveOrganics()

        organics.filter { it.body == Substance.Protein }
            .forEach {
                it.energy--
                it.direction = getRandomFreeDirection(it.position, it.body)
            }
        organics.filter { it.body == Substance.Cellulose }
            .forEach { it.energy = min(it.energy + 1, Settings.MaxHealth) }

        if (MathUtils.random(1f) < Settings.ProbabilityToSpawnBacillus) {
            spawn(Substance.Protein)
        }
        if (MathUtils.random(1f) < Settings.ProbabilityToSpawnFood) {
            spawn(Substance.Cellulose)
        }
    }

    fun spawn(body: Substance): Organic {
        val position = getRandomFreePosition()

        val bacillus = Organic(
            position = position,
            direction = getRandomFreeDirection(position, body),
            energy = getRandomHealth(),
            body = body
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

        val offsping = Organic(
            position = offspingPosition,
            direction = getRandomFreeDirection(offspingPosition, this.body),
            energy = offspingHealth,
            body = this.body
        )
        grid[offspingPosition.y][offspingPosition.x] = offsping
        return offsping
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

    private fun getRandomFreeDirection(position: Point, body: Substance): Point {
        if (body == Substance.Cellulose) {
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
                getSomething(newPosition)?.body == Substance.Cellulose -> {
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

            if (cell.energy >= Settings.ReproductionThreshold) {
                val offsping = cell.split()
                if (offsping != null) {
                    offspings.add(offsping)
                }
            }
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
