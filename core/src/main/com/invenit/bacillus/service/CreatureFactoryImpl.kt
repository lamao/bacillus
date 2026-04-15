package com.invenit.bacillus.service

import com.invenit.bacillus.Settings
import com.invenit.bacillus.model.*


/**
 * Created by Junie
 * Logic for creating and adding creatures to the field.
 * Keeps track of the last used parameters.
 */
class CreatureFactoryImpl : CreatureFactory {
    override var lastDNA: DNA = DNA(Substance.Green, Substance.Sun, Substance.White, Substance.Red, false)
    override var lastSize: Int = Settings.DefaultSize

    /**
     * Create a new organic creature at the given position.
     * Uses the last configured parameters and no direction
     *
     * @param position Position of the creature.
     */
    override fun createOrganic(position: Point) : Organic {
        return Organic(
            position,
            lastSize,
            Point(0, 0),
            lastDNA
        )
    }

}
