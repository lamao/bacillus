package com.invenit.bacillus.model

import com.badlogic.gdx.math.MathUtils
import com.invenit.bacillus.FieldException
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

    val grid: Array<Array<Something?>> = Array(height) { arrayOfNulls<Something?>(width) }

    val organics: MutableList<Organic> = mutableListOf()
    val minerals: MutableList<Mineral> = mutableListOf()

    fun doTic() {

        organics
            .filter { it.energy <= 0 || it.size <= 0 || it.age >= Settings.MaxAge || MathUtils.random() < Settings.UnexpectedDeathRate }
            .forEach { it.kill() }
        minerals.filter { it.size <= 0 }
            .forEach { remove(it.position) }

        organics.filter { it.canMove }
            .forEach { it.move() }

        organics.addAll(
            organics
                .filter { it.energy >= Settings.ReproductionThreshold }
                .mapNotNull { it.split() }
                .toList()
        )

        organics.forEach { it.energy -= Settings.PermanentConsumption }
        organics.forEach { it.age++ }

        organics.filter { it.consume == Substance.Sun }
            .forEach { it.consume(Settings.SunYield) }
        organics.forEach { it.consume(it.getSuitableProducedSubstanceAmount()) }


        organics.filter { it.canMove }
            .forEach { it.lookUp() }


    }

    private fun Organic.lookUp() {

        val directionToFood = this.getDirectionToFood()
        this.direction = if (directionToFood == NoDirection) {
            getRandomFreeDirection(this.position)
        } else {
            directionToFood
        }

        if (this.direction != NoDirection) {
            this.energy -= Settings.MoveConsumption
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
                if (get(x, y)?.body == this.consume && (x != this.position.x || y != this.position.y)) {
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
                val something = get(x, y)
                // TODO: Refactor
                if (something is Organic && something.produce == this.consume) {
                    val distance = max(abs(x - this.position.x), abs(y - this.position.y))
                    result += 1f / 2f.pow(distance - 1)
                }
            }
        }

        return result.roundToInt()
    }


    fun spawn(body: Substance, consume: Substance, produce: Substance, canMove: Boolean): Organic {
        val position = getRandomFreePosition()

        val bacillus = Organic(
            position = position,
            direction = if (canMove) getRandomFreeDirection(position) else NoDirection,
            size = getRandomSize(),
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

    private fun Organic.split(): Organic? {
        val offspringOffset = Point(
            MathUtils.random(-Settings.ReproductionRange, Settings.ReproductionRange),
            MathUtils.random(-Settings.ReproductionRange, Settings.ReproductionRange)
        )

        val offspingSize = getRandomSize()

        this.energy -= offspingSize

        val offspingPosition = this.position + offspringOffset
        if (isOutside(offspingPosition) || !isFree(offspingPosition)) {
            this.energy += (offspingSize * Settings.ReturnHealthWhenReproductionFails).roundToInt()
            return null
        }

        this.size -= offspingSize
        val offsping = cloneWithMutation(offspingPosition, offspingSize)
        putOnGrid(offspingPosition, offsping)
        return offsping
    }

    private fun Organic.cloneWithMutation(
        offspingPosition: Point,
        offspingSize: Int
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
            direction = if (canMove) getRandomFreeDirection(offspingPosition) else NoDirection,
            size = offspingSize,
            body = body,
            consume = consume,
            produce = produce,
            canMove = canMove
        )
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

    private fun getRandomSize() =
        Settings.DefaultSize + MathUtils.random(-Settings.DefaultSize / 4, Settings.DefaultSize / 4)

    private fun isOutside(position: Point): Boolean {
        return position.x < 0 || position.x >= width
                || position.y < 0 || position.y >= height
    }

    private fun Organic.move() {
        val cell = this
        var newPosition = cell.position + cell.direction
        newPosition = when {
            isFree(newPosition) -> {
                newPosition
            }
            get(newPosition)?.body == cell.consume -> {
                val food = get(newPosition)!!
                food.drain(Settings.BiteYield)
                cell.consume(Settings.BiteYield)

                cell.position
            }
            else -> {
                cell.position
            }
        }

        putOnGrid(cell.position, null)
        putOnGrid(newPosition, cell)
        cell.position = newPosition
    }

    fun isFree(position: Point): Boolean = isFree(position.x, position.y)
    fun isFree(x: Int, y: Int): Boolean = get(x, y) == null

    operator fun get(position: Point): Something? = get(position.x, position.y)
    operator fun get(x: Int, y: Int): Something? = grid[y][x]

    private fun putOnGrid(position: Point, something: Something?) {
        grid[position.y][position.x] = something
    }

    private fun Organic.kill() {
        remove(this.position)
        if (this.size > 0) {
            val corps = Mineral(
                this.position,
                this.size,
                this.body
            )
            add(corps)
        }
    }

}
