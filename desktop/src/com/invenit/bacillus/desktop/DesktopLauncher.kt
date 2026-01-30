package com.invenit.bacillus.desktop

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.invenit.bacillus.BacillusGdxGame
import com.invenit.bacillus.Settings
import java.util.*

/**
 * Created by viacheslav.mishcheriakov
 * Created 15.11.2021
 */

fun main() {

    Locale.setDefault(Locale.US)

    val config = Lwjgl3ApplicationConfiguration()
    config.setTitle("Bacillus")
    config.setWindowedMode(Settings.TotalWidth, Settings.Height)
    config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 4)

    val applicationAdapter = BacillusGdxGame()
    Lwjgl3Application(applicationAdapter, config)


}