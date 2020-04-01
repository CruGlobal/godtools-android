package org.cru.godtools.tract.animation

import org.junit.Assert.assertEquals
import org.junit.Test

private const val DELTA = 0.000001f

class BounceInterpolatorTest {
    @Test
    fun verifyDefaultFourBounces75PercentDecay() {
        val interpolator = BounceInterpolator()
        assertEquals(562, interpolator.getTotalDuration(300))

        // first bounce
        assertEquals(0f, interpolator.getInterpolation(0f), DELTA)
        assertEquals(0.75f, interpolator.getInterpolation(2f / 15), DELTA)
        assertEquals(1f, interpolator.getInterpolation(4f / 15), DELTA)
        assertEquals(0.75f, interpolator.getInterpolation(6f / 15), DELTA)
        assertEquals(0f, interpolator.getInterpolation(8f / 15), DELTA)

        // second bounce
        assertEquals(0.1875f, interpolator.getInterpolation(9f / 15), DELTA)
        assertEquals(0.25f, interpolator.getInterpolation(10f / 15), DELTA)
        assertEquals(0.1875f, interpolator.getInterpolation(11f / 15), DELTA)
        assertEquals(0f, interpolator.getInterpolation(12f / 15), DELTA)

        // third bounce
        assertEquals(0.046875f, interpolator.getInterpolation(12.5f / 15), DELTA)
        assertEquals(0.0625f, interpolator.getInterpolation(13f / 15), DELTA)
        assertEquals(0.046875f, interpolator.getInterpolation(13.5f / 15), DELTA)
        assertEquals(0f, interpolator.getInterpolation(14f / 15), DELTA)

        // fourth bounce
        assertEquals(0.01171875f, interpolator.getInterpolation(14.25f / 15), DELTA)
        assertEquals(0.015625f, interpolator.getInterpolation(14.5f / 15), DELTA)
        assertEquals(0.01171875f, interpolator.getInterpolation(14.75f / 15), DELTA)
        assertEquals(0f, interpolator.getInterpolation(15f / 15), DELTA)
    }

    @Test
    fun verifyFourBouncesNoDecay() {
        val interpolator = BounceInterpolator(4, 0.0)
        assertEquals(1200, interpolator.getTotalDuration(300))

        // first bounce
        assertEquals(0f, interpolator.getInterpolation(0f), DELTA)
        assertEquals(0.75f, interpolator.getInterpolation(1f / 16), DELTA)
        assertEquals(1f, interpolator.getInterpolation(2f / 16), DELTA)
        assertEquals(0.75f, interpolator.getInterpolation(3f / 16), DELTA)
        assertEquals(0f, interpolator.getInterpolation(4f / 16), DELTA)

        // second bounce
        assertEquals(0.75f, interpolator.getInterpolation(5f / 16), DELTA)
        assertEquals(1f, interpolator.getInterpolation(6f / 16), DELTA)
        assertEquals(0.75f, interpolator.getInterpolation(7f / 16), DELTA)
        assertEquals(0f, interpolator.getInterpolation(8f / 16), DELTA)

        // third bounce
        assertEquals(0.75f, interpolator.getInterpolation(9f / 16), DELTA)
        assertEquals(1f, interpolator.getInterpolation(10f / 16), DELTA)
        assertEquals(0.75f, interpolator.getInterpolation(11f / 16), DELTA)
        assertEquals(0f, interpolator.getInterpolation(12f / 16), DELTA)

        // fourth bounce
        assertEquals(0.75f, interpolator.getInterpolation(13f / 16), DELTA)
        assertEquals(1f, interpolator.getInterpolation(14f / 16), DELTA)
        assertEquals(0.75f, interpolator.getInterpolation(15f / 16), DELTA)
        assertEquals(0f, interpolator.getInterpolation(16f / 16), DELTA)
    }

    @Test
    fun verifyThreeBounces75PercentDecay() {
        val interpolator = BounceInterpolator(3, 0.75)
        assertEquals(525, interpolator.getTotalDuration(300))

        // first bounce
        assertEquals(0f, interpolator.getInterpolation(0f), DELTA)
        assertEquals(0.75f, interpolator.getInterpolation(1f / 7), DELTA)
        assertEquals(1f, interpolator.getInterpolation(2f / 7), DELTA)
        assertEquals(0.75f, interpolator.getInterpolation(3f / 7), DELTA)
        assertEquals(0f, interpolator.getInterpolation(4f / 7), DELTA)

        // second bounce
        assertEquals(0.1875f, interpolator.getInterpolation(4.5f / 7), DELTA)
        assertEquals(0.25f, interpolator.getInterpolation(5f / 7), DELTA)
        assertEquals(0.1875f, interpolator.getInterpolation(5.5f / 7), DELTA)
        assertEquals(0f, interpolator.getInterpolation(6f / 7), DELTA)

        // third bounce
        assertEquals(0.046875f, interpolator.getInterpolation(6.25f / 7), DELTA)
        assertEquals(0.0625f, interpolator.getInterpolation(6.5f / 7), DELTA)
        assertEquals(0.046875f, interpolator.getInterpolation(6.75f / 7), DELTA)
        assertEquals(0f, interpolator.getInterpolation(7.0f / 7), DELTA)
    }
}
