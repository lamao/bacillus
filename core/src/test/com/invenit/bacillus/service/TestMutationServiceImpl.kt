package com.invenit.bacillus.service

import com.invenit.bacillus.model.DNA
import com.invenit.bacillus.model.Substance
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

/**
 * Created by viacheslav.mishcheriakov
 * Created 08.12.2021
 */
@ExtendWith(MockitoExtension::class)
class TestMutationServiceImpl {

    @Mock
    private lateinit var mockRandomService: RandomService

    private lateinit var mutationService: MutationService

    @BeforeTest
    fun before() {
        mutationService = MutationServiceImpl(mockRandomService)
    }

    @Test
    fun testNoMutation() {
        val original = DNA(
            body = Substance.Blue,
            consume = Substance.Green,
            produce = Substance.Yellow,
            toxin = Substance.White,
            canMove = false
        )

        whenever(mockRandomService.random()).thenReturn(1.0f)

        val mutated = mutationService.mutatedDna(original)
        assertEquals(original, mutated)
    }

    @Test
    fun testMutatedDnaBody() {
        val original = DNA(
            body = Substance.Blue,
            consume = Substance.Sun,
            produce = Substance.Green,
            toxin = Substance.White,
            canMove = true
        )

        whenever(mockRandomService.random()).thenReturn(0.0f)
        whenever(mockRandomService.random(0, DNA.Trait.count() - 1)).thenReturn(0)
        whenever(mockRandomService.random(1, Substance.values().size - 1))
            .thenReturn(Substance.Red.ordinal)

        val mutated = mutationService.mutatedDna(original)

        assertEquals(original.copy(body = Substance.Red), mutated)
    }

    @Test
    fun testMutatedDnaConsume() {
        val original = DNA(
            body = Substance.Blue,
            consume = Substance.Blue,
            produce = Substance.Green,
            toxin = Substance.White,
            canMove = false
        )
        whenever(mockRandomService.random()).thenReturn(0.0f)
        whenever(mockRandomService.random(0, DNA.Trait.count() - 1)).thenReturn(1)
        whenever(mockRandomService.random(0, Substance.values().size - 1))
            .thenReturn(Substance.Sun.ordinal)

        val mutated = mutationService.mutatedDna(original)

        assertEquals(original.copy(consume = Substance.Sun), mutated)
    }

    @Test
    fun testMutatedDnaProduce() {
        val original = DNA(
            body = Substance.Blue,
            consume = Substance.Green,
            produce = Substance.Blue,
            toxin = Substance.White,
            canMove = false
        )
        whenever(mockRandomService.random()).thenReturn(0.0f)
        whenever(mockRandomService.random(0, DNA.Trait.count() - 1)).thenReturn(2)
        whenever(mockRandomService.random(1, Substance.values().size - 1))
            .thenReturn(Substance.Red.ordinal)

        val mutated = mutationService.mutatedDna(original)

        assertEquals(original.copy(produce = Substance.Red), mutated)
    }

    @Test
    fun testMutatedDnaToxin() {
        val original = DNA(
            body = Substance.Blue,
            consume = Substance.Green,
            produce = Substance.Yellow,
            toxin = Substance.Blue,
            canMove = false
        )
        whenever(mockRandomService.random()).thenReturn(0.0f)
        whenever(mockRandomService.random(0, DNA.Trait.count() - 1)).thenReturn(3)
        whenever(mockRandomService.random(1, Substance.values().size - 1))
            .thenReturn(Substance.White.ordinal)

        val mutated = mutationService.mutatedDna(original)

        assertEquals(original.copy(toxin = Substance.White), mutated)
    }

    @Test
    fun testMutatedDna_CanMove() {
        val original = DNA(
            body = Substance.Blue,
            consume = Substance.Green,
            produce = Substance.Yellow,
            toxin = Substance.White,
            canMove = false
        )
        whenever(mockRandomService.random()).thenReturn(0.0f)
        whenever(mockRandomService.random(0, DNA.Trait.count() - 1)).thenReturn(4)
        whenever(mockRandomService.randomBoolean()).thenReturn(true)

        val mutated = mutationService.mutatedDna(original)

        assertEquals(original.copy(canMove = true), mutated)
    }
}