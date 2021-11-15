package com.invenit.bacillus.model

import com.invenit.bacillus.Settings

/**
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */
class Bacillus(var position: Point, var direction: Point) {
    var health: Int = Settings.DefaultHealth
}