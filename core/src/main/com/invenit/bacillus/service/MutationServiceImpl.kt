package com.invenit.bacillus.service

import com.badlogic.gdx.math.MathUtils
import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.DNA
import com.invenit.bacillus.model.Substance

/**
 * Created by vyacheslav.mischeryakov
 * Created 08.12.2021
 */
class MutationServiceImpl(
    private val randomService: RandomService
) : MutationService {

    override fun mutatedSize(size: Int): Int =
        size + MathUtils.random(-size / 4, size / 4)

    override fun mutatedDna(dna: DNA): DNA {
        var body = dna.body
        var consume = dna.consume
        var produce = dna.produce
        var poison = dna.toxin
        var canMove = dna.canMove
        if (randomService.random() < Settings.MutationRate) {
            // TODO: Refactor
            when (randomService.random(0, DNA.Trait.count() - 1)) {
                DNA.Trait.Body.ordinal -> {
                    body = randomBody()
                }
                DNA.Trait.Consume.ordinal -> {
                    consume = randomConsume()
                }
                DNA.Trait.Produce.ordinal -> {
                    produce = randomProduce()
                }
                DNA.Trait.Toxin.ordinal -> {
                    poison = randomToxin()
                }
                DNA.Trait.CanMove.ordinal -> {
                    canMove = randomService.randomBoolean()
                }
            }
        }

        return DNA(body, consume, produce, poison, canMove)
    }

    override fun randomBody() = Substance.values()[randomService.random(1, Substance.values().size - 1)]
    override fun randomConsume() = Substance.values()[randomService.random(0, Substance.values().size - 1)]
    override fun randomProduce() = Substance.values()[randomService.random(1, Substance.values().size - 1)]
    override fun randomToxin() = Substance.values()[randomService.random(1, Substance.values().size - 1)]
}