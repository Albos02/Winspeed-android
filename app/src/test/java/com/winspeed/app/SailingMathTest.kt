package com.winspeed.app

import org.junit.Assert.assertEquals
import org.junit.Test

class SailingMathTest {

    @Test
    fun testMsToKnots() {
        assertEquals(1.94384f, SailingMath.msToKnots(1.0f), 0.0001f)
        assertEquals(0.0f, SailingMath.msToKnots(0.0f), 0.0001f)
    }

    @Test
    fun testNormalizeAngle() {
        assertEquals(10f, SailingMath.normalizeAngle(370f), 0.0001f)
        assertEquals(350f, SailingMath.normalizeAngle(-10f), 0.0001f)
        assertEquals(0f, SailingMath.normalizeAngle(360f), 0.0001f)
        assertEquals(180f, SailingMath.normalizeAngle(180f), 0.0001f)
    }

    @Test
    fun testHeadingDelta() {
        assertEquals(10f, SailingMath.headingDelta(350f, 0f), 0.0001f)
        assertEquals(-10f, SailingMath.headingDelta(0f, 350f), 0.0001f)
        assertEquals(180f, SailingMath.headingDelta(0f, 180f), 0.0001f)
        assertEquals(-170f, SailingMath.headingDelta(0f, 190f), 0.0001f)
    }

    @Test
    fun testCalculateVMG() {
        // Boat at 5 knots, heading 0 (North), Wind from 0 (North) -> VMG should be 5
        assertEquals(5.0f, SailingMath.calculateVMG(5.0f, 0f, 0f), 0.0001f)
        // Boat at 5 knots, heading 180 (South), Wind from 0 (North) -> VMG should be -5
        assertEquals(-5.0f, SailingMath.calculateVMG(5.0f, 180f, 0f), 0.0001f)
        // Boat at 5 knots, heading 90 (East), Wind from 0 (North) -> VMG should be 0
        assertEquals(0.0f, SailingMath.calculateVMG(5.0f, 90f, 0f), 0.0001f)
        // Boat at 5 knots, heading 45 (NE), Wind from 0 (North) -> VMG should be 5 * cos(45) approx 3.535
        assertEquals(3.5355f, SailingMath.calculateVMG(5.0f, 45f, 0f), 0.0001f)
    }

    @Test
    fun testCalculateTWA() {
        assertEquals(45f, SailingMath.calculateTWA(45f, 0f), 0.0001f)
        assertEquals(45f, SailingMath.calculateTWA(0f, 45f), 0.0001f)
        assertEquals(180f, SailingMath.calculateTWA(0f, 180f), 0.0001f)
        assertEquals(90f, SailingMath.calculateTWA(90f, 180f), 0.0001f)
    }

    @Test
    fun testFuseHeading() {
        // Fast speed (> 2.0) -> Use GPS Bearing
        assertEquals(10f, SailingMath.fuseHeading(10f, 50f, 3.0f), 0.0001f)
        // Slow speed (< 0.2) -> Use Magnetic Heading
        assertEquals(50f, SailingMath.fuseHeading(10f, 50f, 0.1f), 0.0001f)
        // Middle speed (Linear blend)
        // 0.2 -> 100% Magnetic (50)
        // 2.0 -> 100% GPS (10)
        // 1.1 -> 50% blend
        // Delta between 50 and 10 is -40. 50 + (-40 * 0.5) = 30
        assertEquals(30f, SailingMath.fuseHeading(10f, 50f, 1.1f), 0.0001f)
    }
}
