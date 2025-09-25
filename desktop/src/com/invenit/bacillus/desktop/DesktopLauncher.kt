package com.invenit.bacillus.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.invenit.bacillus.BacillusGdxGame
import com.invenit.bacillus.Settings
import java.util.*

/**
 * Created by viacheslav.mishcheriakov
 * Created 15.11.2021
 */

fun main(args: Array<String>) {

    Locale.setDefault(Locale.US)

    val config = LwjglApplicationConfiguration()
    config.title = "Bacillus"
    config.width = Settings.TotalWidth
    config.height = Settings.Height

    config.samples = 4

    val applicationAdapter = BacillusGdxGame()
    LwjglApplication(applicationAdapter, config)


}