package com.invenit.bacillus.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.invenit.bacillus.BacillusGdxGame
import com.invenit.bacillus.Settings

/**
 * Created by vyacheslav.mischeryakov
 * Created 15.11.2021
 */

fun main(args: Array<String>) {
    val config = LwjglApplicationConfiguration()
    config.title = "Bacillus"
    config.width = Settings.Width
    config.height = Settings.Height

    config.samples = 4

    val applicationAdapter = BacillusGdxGame()
    LwjglApplication(applicationAdapter, config)


}