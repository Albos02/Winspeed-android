package com.winspeed.app

import kotlin.math.*

class WindEstimator {
    private val headings = mutableListOf<Float>()
    private var lastTackHeading: Float? = null
    
    // Estimated wind direction
    private var _estimatedWindDirection = 0f
    val estimatedWindDirection: Float get() = _estimatedWindDirection

    /**
     * Feed new heading data into the estimator.
     * @param heading Current fused heading.
     * @param speed Speed in knots (estimation only works when moving).
     */
    fun addHeading(heading: Float, speed: Float) {
        if (speed < 3.0f) return // Only estimate when moving at a decent speed

        headings.add(heading)
        if (headings.size > 100) headings.removeAt(0)

        // Basic tack detection: if we turn > 60 degrees over the last 30 samples
        if (headings.size >= 30) {
            val startHeading = headings[headings.size - 30]
            val delta = SailingMath.headingDelta(startHeading, heading)
            
            if (abs(delta) in 70.0..110.0) {
                // Potential tack detected
                if (lastTackHeading != null) {
                    // We have two steady states to bisect
                    val wind = SailingMath.normalizeAngle(startHeading + delta / 2f)
                    _estimatedWindDirection = wind
                }
                lastTackHeading = startHeading
                headings.clear()
            }
        }
    }
    
    fun setManualWind(direction: Float) {
        _estimatedWindDirection = direction
    }
}
