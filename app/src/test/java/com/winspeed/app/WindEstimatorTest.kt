package com.winspeed.app

import org.junit.Assert.assertEquals
import org.junit.Test

class WindEstimatorTest {

    @Test
    fun testInitialValue() {
        val estimator = WindEstimator()
        assertEquals(0f, estimator.estimatedWindDirection, 0.0001f)
    }

    @Test
    fun testManualSet() {
        val estimator = WindEstimator()
        estimator.setManualWind(123f)
        assertEquals(123f, estimator.estimatedWindDirection, 0.0001f)
    }

    @Test
    fun testAutoTackEstimation() {
        val estimator = WindEstimator()
        
        // Steady on Tack 1 (40 degrees)
        repeat(30) { estimator.addHeading(40f, 5.0f) }
        
        // Tack to Tack 2 (320 degrees)
        // delta is 320 - 40 = 280 (which is -80 degrees)
        // SailingMath.headingDelta(40, 320) = -80
        // abs(-80) is 80, which is in range 70..110
        estimator.addHeading(320f, 5.0f)
        
        // The bisection logic: 
        // startHeading (40) + delta (-80) / 2 = 40 - 40 = 0
        assertEquals(0f, estimator.estimatedWindDirection, 0.0001f)
    }

    @Test
    fun testLowSpeedFilter() {
        val estimator = WindEstimator()
        
        // Steady on Tack 1
        repeat(30) { estimator.addHeading(40f, 1.0f) } // Speed too low
        
        // Attempt tack
        estimator.addHeading(320f, 1.0f)
        
        // Should NOT have updated (remains 0)
        assertEquals(0f, estimator.estimatedWindDirection, 0.0001f)
    }
}
