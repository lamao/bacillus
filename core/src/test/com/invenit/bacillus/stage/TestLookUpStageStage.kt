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
class TestLookUpStageStage {

    private lateinit var stage: LookUpStage

    @BeforeTest
    fun before() {
        stage = LookUpStage()
    }

    @Test
    fun testWhenNoSuitableFoodAndNoMovingSelected() {
        TODO("Implement")
    }

    @Test
    fun testWhenNoSuitableFoodAndMovingIsSelected() {
        TODO("Implement")
    }

    @Test
    fun testWhenSingleFoodCellFound() {
        TODO("Implement")
    }

    @Test
    fun testWhenMultipleFoodCellsFound() {
        TODO("Implement")
    }

}