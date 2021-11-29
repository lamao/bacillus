package com.invenit.bacillus.model

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Created by vyacheslav.mischeryakov
 * Created 22.11.2021
 */
class DNATest {

    val NumberOfIterations = 100_000
    lateinit var dna: DNA

    @BeforeTest
    fun before() {
        dna = DNA(Substance.Green, Substance.Sun, Substance.White, Substance.Red, false)
    }

    @Test
    fun testMutate() {
        val bodyDistribution = IntArray(Substance.values().size)

        for (i in 1..NumberOfIterations) {
            val mutatedDna = dna.mutated()
            assertNotEquals(Substance.Sun, mutatedDna.produce, "Can't produce Sun")
            assertNotEquals(Substance.Sun, mutatedDna.body, "Can't consist of Sun")

            bodyDistribution[mutatedDna.body.ordinal]++
        }

        assertEquals(0, bodyDistribution[Substance.Sun.ordinal], "Body can't be Sun")
    }

}