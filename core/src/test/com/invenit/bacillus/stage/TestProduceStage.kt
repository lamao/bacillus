package com.invenit.bacillus.stage

import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 *
 * @author viacheslav.mishcheriakov
 * Created: 13.12.21
 */
@ExtendWith(MockitoExtension::class)
class TestProduceStage {

    private lateinit var stage: ProduceStage

    @BeforeTest
    fun before() {
        stage = ProduceStage()
    }

    @Test
    fun testRegularProduction() {
        TODO("Implement")
    }

    @Test
    fun testWhenNoFreeSpaceLeft() {
        TODO("Implement")
    }

}