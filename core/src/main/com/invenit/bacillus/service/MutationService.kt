package com.invenit.bacillus.service

import com.invenit.bacillus.model.DNA
import com.invenit.bacillus.model.Substance

/**
 * Created by viacheslav.mishcheriakov
 * Created 08.12.2021
 */
interface MutationService {

    fun mutatedSize(size: Int) : Int
    fun mutatedDna(dna: DNA) : DNA

    fun randomBody(): Substance
    fun randomConsume(): Substance
    fun randomProduce(): Substance
    fun randomToxin(): Substance
}