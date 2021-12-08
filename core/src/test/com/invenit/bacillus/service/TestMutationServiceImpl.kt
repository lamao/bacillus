package com.invenit.bacillus.service

import com.invenit.bacillus.model.Substance
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Created by vyacheslav.mischeryakov
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
    fun testMutatedDna() {
       TODO("Implement")
    }

}