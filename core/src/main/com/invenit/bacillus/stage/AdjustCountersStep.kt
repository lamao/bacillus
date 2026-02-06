package com.invenit.bacillus.stage

import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.Field

/**
 * Created by viacheslav.mishcheriakov
 * Created 21.11.2021
 */
class AdjustCountersStep : Step {
    override fun execute(field: Field) {
        field.organics.forEach {
            it.energy -= Settings.PermanentConsumption
            it.age++
        }
        field.minerals.forEach {
            it.size -= Settings.MineralDegradation
        }
    }
}