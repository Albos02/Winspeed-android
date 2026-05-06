package com.winspeed.app

import kotlin.math.*

object SailingMath {
    /**
     * Converts meters per second to knots.
     */
    fun msToKnots(ms: Float): Float = ms * 1.94384f

    /**
     * Converts degrees to radians.
     */
    fun toRadians(degrees: Float): Double = Math.toRadians(degrees.toDouble())

    /**
     * Calculates Velocity Made Good (VMG) towards a target wind angle.
     * @param boatSpeed Speed of the boat in knots.
     * @param boatHeading Direction boat is pointing (0-360).
     * @param windDirection Direction wind is coming FROM (0-360).
     */
    fun calculateVMG(boatSpeed: Float, boatHeading: Float, windDirection: Float): Float {
        val angleToWind = abs(boatHeading - windDirection)
        val angleInRadians = Math.toRadians(angleToWind.toDouble())
        return (boatSpeed * cos(angleInRadians)).toFloat()
    }

    /**
     * Normalizes an angle to be within [0, 360).
     */
    fun normalizeAngle(angle: Float): Float {
        var normalized = angle % 360
        if (normalized < 0) normalized += 360
        return normalized
    }

    /**
     * Calculates the difference between two headings in degrees [-180, 180].
     */
    fun headingDelta(current: Float, target: Float): Float {
        var diff = target - current
        while (diff < -180) diff += 360
        while (diff > 180) diff -= 360
        return diff
    }
}
