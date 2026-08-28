package com.invenit.bacillus.util

/**
 *
 * @author viacheslav.mishcheriakov
 * Created 28.08.2026
 */
class TicCounter {

    var accumulatedTime: Float = 0f
        private set

    companion object {
        // Bounds how much simulated time a single slow/stalled render frame can inject,
        // so a long stall (e.g. minimized window) doesn't force a huge tic catch-up burst.
        private const val MAX_FRAME_TIME = 0.25f
    }

    fun reset() {
        accumulatedTime = 0f
    }

    /**
     * @param maxTps must be > 0; callers are responsible for pausing instead of calling
     * this with maxTps <= 0, since a non-positive value would make ticInterval infinite
     * (or negative) and the catch-up loop below would never terminate.
     */
    fun update(delta: Float, maxTps: Int): Int {
        require(maxTps > 0) { "maxTps must be > 0, was $maxTps" }

        accumulatedTime += delta.coerceAtMost(MAX_FRAME_TIME)

        val ticInterval = 1f / maxTps
        var accumulatedTics = 0
        while (accumulatedTime >= ticInterval) {
            accumulatedTics++
            accumulatedTime -= ticInterval
        }
        return accumulatedTics
    }

}
