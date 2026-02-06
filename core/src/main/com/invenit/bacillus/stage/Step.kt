package com.invenit.bacillus.stage

import com.invenit.bacillus.model.Field

/**
 * Created by viacheslav.mishcheriakov
 * Created 21.11.2021
 */
interface Step {

    fun execute(field: Field)
}