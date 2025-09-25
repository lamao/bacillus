package com.invenit.bacillus.service

import com.badlogic.gdx.math.MathUtils
import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.DNA
import com.invenit.bacillus.model.Substance

/**
 * Created by viacheslav.mishcheriakov
 * Created 08.12.2021
 */
class MutationServiceImpl(
    private val randomService: RandomService
) : MutationService {

    override fun mutatedSize(size: Int): Int =
        size + MathUtils.random(-size / 4, size / 4)

    override fun mutatedDna(dna: DNA): DNA {
        if (randomService.random() >= Settings.MutationRate) return dna

        return when (DNA.Trait.values()[randomService.random(0, DNA.Trait.count() - 1)]) {
            DNA.Trait.Body -> dna.copy(body = randomBody())
            DNA.Trait.Consume -> dna.copy(consume = randomConsume())
            DNA.Trait.Produce -> dna.copy(produce = randomProduce())
            DNA.Trait.Toxin -> dna.copy(toxin = randomToxin())
            DNA.Trait.CanMove -> dna.copy(canMove = randomService.randomBoolean())
        }
    }

    override fun randomBody() = Substance.values()[randomService.random(1, Substance.values().size - 1)]
    override fun randomConsume() = Substance.values()[randomService.random(0, Substance.values().size - 1)]
    override fun randomProduce() = Substance.values()[randomService.random(1, Substance.values().size - 1)]
    override fun randomToxin() = Substance.values()[randomService.random(1, Substance.values().size - 1)]
}