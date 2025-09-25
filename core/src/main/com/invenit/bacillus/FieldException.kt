package com.invenit.bacillus

/**
 * Created by viacheslav.mishcheriakov
 * Created 19.11.2021
 */
class FieldException(
    override val message: String
) : RuntimeException(message) {
}