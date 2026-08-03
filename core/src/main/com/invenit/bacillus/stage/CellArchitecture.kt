package com.invenit.bacillus.stage

import com.badlogic.gdx.math.MathUtils
import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.Organic
import com.invenit.bacillus.model.Point
import com.invenit.bacillus.model.Something
import com.invenit.bacillus.model.Substance
import kotlin.math.roundToInt

/**
 * Pure DTOs for cell decisions.
 */
sealed class CellDecision {
    object None : CellDecision()
    data class Move(val direction: Point) : CellDecision()
    data class Bite(val target: Point) : CellDecision()
    data class ChangeDirection(val direction: Point) : CellDecision()
    data class ConsumeMinerals(val amount: Int) : CellDecision()
}

/**
 * Read-only interface for cell logic to inspect surroundings.
 */
interface CellEnvironment {
    val cell: Organic
    fun isFree(relative: Point): Boolean
    fun getSomething(relative: Point): Something?
    fun isInside(relative: Point): Boolean
    fun iterateRadial(range: Int, action: (relativeX: Int, relativeY: Int, something: Something?) -> Boolean)
}

/**
 * Logic that produces a decision based on environment.
 */
interface CellLogic {
    fun decide(env: CellEnvironment): CellDecision
}

class BiteLogic : CellLogic {
    override fun decide(env: CellEnvironment): CellDecision {
        val cell = env.cell
        if (!cell.dna.canMove || cell.direction == Point.Zero) return CellDecision.None

        val targetPos = cell.direction
        val something = env.getSomething(targetPos)
        if (something?.body == cell.dna.consume) {
            return CellDecision.Bite(cell.position + targetPos)
        }
        return CellDecision.None
    }
}

class MoveLogic : CellLogic {
    override fun decide(env: CellEnvironment): CellDecision {
        val cell = env.cell
        if (!cell.dna.canMove || cell.direction == Point.Zero) return CellDecision.None

        if (env.isFree(cell.direction)) {
            return CellDecision.Move(cell.direction)
        }
        return CellDecision.None
    }
}

class LookUpLogic : CellLogic {
    override fun decide(env: CellEnvironment): CellDecision {
        if (!env.cell.dna.canMove) return CellDecision.None

        val directionToFood = getDirectionToFood(env)
        val finalDirection = if (directionToFood == com.invenit.bacillus.model.Field.NoDirection) {
            getRandomDirection(env)
        } else {
            directionToFood
        }

        if (finalDirection != com.invenit.bacillus.model.Field.NoDirection) {
            return CellDecision.ChangeDirection(finalDirection)
        }
        return CellDecision.None
    }

    private fun getDirectionToFood(env: CellEnvironment): Point {
        var result = com.invenit.bacillus.model.Field.NoDirection
        var bestSize = 0

        env.iterateRadial(Settings.VisionRange) { dx, dy, something ->
            if (something?.body == env.cell.dna.consume && something.size > bestSize) {
                result = Point(dx, dy).normalized()
                bestSize = something.size
            }
            true
        }
        return result
    }

    private fun getRandomDirection(env: CellEnvironment): Point {
        val direction = Point(
            x = MathUtils.random(-1, 1),
            y = MathUtils.random(-1, 1)
        )
        if (direction == Point.Zero) return com.invenit.bacillus.model.Field.NoDirection
        
        // Use a relative check. We need to know if it's outside.
        // The Environment should probably tell us if it's inside/outside.
        // Let's add isInside to CellEnvironment.
        if (env.isInside(direction)) {
            return direction
        }
        return com.invenit.bacillus.model.Field.NoDirection
    }
}

class PassiveConsumeLogic : CellLogic {
    override fun decide(env: CellEnvironment): CellDecision {
        val cell = env.cell
        if (cell.dna.canMove) return CellDecision.None
        if (cell.dna.consume == Substance.Sun) return CellDecision.None // Sun is world-step

        var result = 0f
        env.iterateRadial(Settings.ConsumingRange) { dx, dy, something ->
            if (something?.body == cell.dna.consume) {
                val rawGain = Integer.min(something.size, Settings.MineralsYield)
                val distance = Point.Zero.distance(dx, dy)

                result += Settings.correctedMineralsYield(rawGain.toFloat(), distance)
                // We can't drain here, decision must be pure.
                // But ConsumeStep drained minerals during iteration.
                // This is a bit tricky. If we return a decision to consume, 
                // the applier should perform the same iteration.
                // Or we can return a decision with a list of points to drain.
                // The original logic stopped when energy + result > MaxSize.
            }
            true
        }
        
        if (result > 0) {
            return CellDecision.ConsumeMinerals(result.roundToInt())
        }
        return CellDecision.None
    }
}

/**
 * Base for world-only steps.
 */
interface WorldStep : Step

/**
 * A step that iterates over all organics and applies their logic.
 */
class CellLogicStep(
    private val logics: List<CellLogic>,
    private val applier: CellDecisionApplier
) : Step {
    override fun execute(field: com.invenit.bacillus.model.Field) {
        val adapter = FieldAdapterImpl(field)
        var i = 0
        while (i < adapter.organics.size) {
            val cell = adapter.organics[i]
            if (cell.energy > 0) { // skip dead
                val env = CellEnvironmentImpl(cell, adapter)
                for (logic in logics) {
                    val decision = logic.decide(env)
                    if (decision != CellDecision.None) {
                        if (applier.apply(cell, decision, adapter)) {
                            break // First non-None valid decision wins
                        }
                    }
                }
            }
            i++
        }
    }
}

class CellEnvironmentImpl(
    override val cell: Organic,
    private val field: FieldAdapter
) : CellEnvironment {
    override fun isFree(relative: Point): Boolean = field.isFree(cell.position + relative)
    override fun getSomething(relative: Point): Something? = field[cell.position + relative]
    override fun isInside(relative: Point): Boolean = field.isInside(cell.position + relative)
    override fun iterateRadial(range: Int, action: (Int, Int, Something?) -> Boolean) {
        field.iterateRadial(cell.position, range) { x, y ->
            action(x - cell.position.x, y - cell.position.y, field[com.invenit.bacillus.model.Point(x, y)])
        }
    }
}

class FieldAdapterImpl(private val f: com.invenit.bacillus.model.Field) : FieldAdapter {
    override val organics: List<Organic>
        get() = f.organics
    override fun isFree(at: Point): Boolean = f.isFree(at)
    override fun isInside(at: Point): Boolean = f.isInside(at.x, at.y)
    override fun get(at: Point): Something? = f[at]
    override fun relocate(something: Something, target: Point) = f.relocate(something, target)
    override fun iterateRadial(anchor: Point, range: Int, action: (Int, Int) -> Boolean) =
        f.iterateRadial(anchor, range, action)
    override fun remove(at: Point) = f.remove(at)
}

/**
 * Applies decisions to the world.
 */
interface CellDecisionApplier {
    fun apply(cell: Organic, decision: CellDecision, field: FieldAdapter): Boolean
}

class CellDecisionApplierImpl : CellDecisionApplier {
    override fun apply(cell: Organic, decision: CellDecision, field: FieldAdapter): Boolean {
        return when (decision) {
            is CellDecision.Move -> {
                if (field.isFree(cell.position + decision.direction)) {
                    field.relocate(cell, cell.position + decision.direction)
                    true
                } else false
            }
            is CellDecision.Bite -> {
                val target = decision.target
                val food = field[target]
                if (food != null && food.body == cell.dna.consume) {
                    val actualDrain = food.drain(Settings.BiteYield)
                    applyConsumption(cell, actualDrain)
                    true
                } else false
            }
            is CellDecision.ChangeDirection -> {
                cell.direction = decision.direction
                cell.energy -= Settings.MoveConsumption
                true
            }
            is CellDecision.ConsumeMinerals -> {
                applyConsumption(cell, decision.amount)
                true
            }
            else -> false
        }
    }

    private fun applyConsumption(cell: Organic, gain: Int) {
        val actualGain = (gain * (1 - Settings.ProductionPerformance)).roundToInt()

        if (cell.energy + actualGain <= cell.size) {
            cell.energy += actualGain
        } else {
            val energyGain = cell.size - cell.energy
            val sizeGain = actualGain - energyGain

            cell.energy += energyGain
            cell.size = Integer.min(cell.size + sizeGain, Settings.MaxSize)
        }
        cell.accumulatedWaste += (gain - actualGain)
    }
}

/**
 * Adapter for Field to be used in CellLogicStep and Applier.
 * This helps in future if we want to change Field implementation.
 */
interface FieldAdapter {
    val organics: List<Organic>
    fun isFree(at: Point): Boolean
    fun isInside(at: Point): Boolean
    operator fun get(at: Point): Something?
    fun relocate(something: Something, target: Point)
    fun iterateRadial(anchor: Point, range: Int, action: (x: Int, y: Int) -> Boolean)
    fun remove(at: Point)
}
