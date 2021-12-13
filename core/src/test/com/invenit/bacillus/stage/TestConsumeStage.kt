package com.invenit.bacillus.stage

import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 *
 * @author vyacheslav.mischeryakov
 * Created: 13.12.21
 */
@ExtendWith(MockitoExtension::class)
class TestConsumeStage {

    private lateinit var stage: ConsumeStage

    @BeforeTest
    fun before() {
        stage = ConsumeStage()
    }

    @Test
    fun testConsumeSun() {
        TODO("Implement")
    }

    @Test
    fun testConsumeMineralsWhenNothingToConsume() {
        TODO("Implement")
    }

    @Test
    fun testConsumeMinerals() {
        TODO("Implement")
    }
}