package com.invenit.bacillus.model

/**
 *
 * @author viacheslav.mishcheriakov
 * Created: 16.11.21
 */
data class Mineral(
    override var position: Point,
    override var size: Int,
    override val body: Substance
) : Something {
}