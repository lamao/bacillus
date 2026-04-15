package com.invenit.bacillus.service

import com.invenit.bacillus.model.*

/**
 * Created by Junie
 * Logic for creating and adding creatures to the field.
 * Keeps track of the last used parameters.
 */
interface CreatureFactory {
    var lastDNA: DNA
    var lastSize: Int

    /**
     * Create a new organic creature at the given position.
     * Uses the last configured parameters and no direction
     *
     * @param position Position of the creature.
     */
    fun createOrganic(position: Point) : Organic
}
