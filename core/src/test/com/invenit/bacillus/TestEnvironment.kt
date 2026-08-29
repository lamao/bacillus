package com.invenit.bacillus

import com.invenit.bacillus.model.DNA
import com.invenit.bacillus.model.Field
import com.invenit.bacillus.model.Organic
import com.invenit.bacillus.model.Point
import com.invenit.bacillus.model.Substance
import com.invenit.bacillus.model.matrix.DefaultDecisionMatrixFactory
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class TestEnvironment {

    @Test
    fun testDoTicRunsTheFullStepPipelineWithoutCrashing() {
        val field = Field(10, 10)
        val dna = DNA(
            Substance.Green,
            Substance.Sun,
            Substance.White,
            Substance.Red,
            DefaultDecisionMatrixFactory().initial()
        )
        field.add(Organic(Point(5, 5), Settings.DefaultSize, Point.Zero, dna))

        Environment().doTic(field)

        assertTrue(field.organics.isNotEmpty())
    }
}
