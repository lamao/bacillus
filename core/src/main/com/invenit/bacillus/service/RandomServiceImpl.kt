package com.invenit.bacillus.service

import com.badlogic.gdx.math.MathUtils

/**
 * Created by vyacheslav.mischeryakov
 * Created 02.12.2021
 */
class RandomServiceImpl : RandomService {
    override fun random(start: Int, end: Int): Int = MathUtils.random(start, end)
    override fun random(): Float = MathUtils.random()
    override fun randomBoolean(): Boolean  = MathUtils.randomBoolean()

}