package com.invenit.bacillus.util

import com.badlogic.gdx.math.MathUtils
import com.invenit.bacillus.Settings

/**
 * Created by vyacheslav.mischeryakov
 * Created 21.11.2021
 */
// TODO: Refactor
object Mutator {

    fun getRandomSize() =
        Settings.DefaultSize + MathUtils.random(-Settings.DefaultSize / 4, Settings.DefaultSize / 4)

}