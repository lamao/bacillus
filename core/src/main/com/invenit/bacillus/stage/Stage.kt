package com.invenit.bacillus.stage

import com.invenit.bacillus.model.Field

/**
 * Created by vyacheslav.mischeryakov
 * Created 21.11.2021
 */
interface Stage {

    fun execute(field: Field)
}