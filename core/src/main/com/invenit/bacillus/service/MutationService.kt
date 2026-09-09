package com.invenit.bacillus.service

import com.invenit.bacillus.model.DNA
import com.invenit.bacillus.model.Substance

/**
 * Created by viacheslav.mishcheriakov
 * Created 08.12.2021
 */
interface MutationService {

    /** Count of mutations (#12) that have rerolled a Decision Matrix state, since this service was created. */
    val dmMutationCount: Int

    /** Count of mutations (#12) that have rerolled a body/consume/produce/toxin trait, since this service was created. */
    val traitMutationCount: Int

    fun mutatedSize(size: Int) : Int
    fun mutatedDna(dna: DNA) : DNA

    fun randomBody(): Substance
    fun randomConsume(): Substance
    fun randomProduce(): Substance
    fun randomToxin(): Substance
}