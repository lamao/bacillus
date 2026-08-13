package com.invenit.bacillus

import com.invenit.bacillus.model.matrix.DefaultDecisionMatrixFactory
import com.invenit.bacillus.service.CreatureFactoryImpl
import com.invenit.bacillus.service.MutationServiceImpl
import com.invenit.bacillus.service.RandomServiceImpl

/**
 * Created by viacheslav.mishcheriakov
 * Created 02.12.2021
 */
object ServiceContext {

    val randomService = RandomServiceImpl()

    val mutationService = MutationServiceImpl(randomService)
    val decisionMatrixFactory = DefaultDecisionMatrixFactory()
    val creationFactory = CreatureFactoryImpl(decisionMatrixFactory)
}