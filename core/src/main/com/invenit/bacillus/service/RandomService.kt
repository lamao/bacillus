package com.invenit.bacillus.service

/**
 * Created by vyacheslav.mischeryakov
 * Created 02.12.2021
 */
interface RandomService {
    fun random(start: Int, end: Int): Int
    fun random(): Float
    fun randomBoolean(): Boolean
}