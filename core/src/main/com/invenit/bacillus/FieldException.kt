package com.invenit.bacillus

/**
 * Created by vyacheslav.mischeryakov
 * Created 19.11.2021
 */
class FieldException(
    override val message: String
) : RuntimeException(message) {
}