package org.cru.godtools.tract.animation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BounceInterpolatorTest {
    private static final float DELTA = 0.000001f;

    @Test
    public void verifyDefaultFourBounces75PercentDecay() {
        final BounceInterpolator interpolator = new BounceInterpolator();

        assertEquals(562, interpolator.getTotalDuration(300));

        // first bounce
        assertEquals(0, interpolator.getInterpolation((float) 0), DELTA);
        assertEquals(0.75, interpolator.getInterpolation((float) 2.0 / 15), DELTA);
        assertEquals(1, interpolator.getInterpolation((float) 4.0 / 15), DELTA);
        assertEquals(0.75, interpolator.getInterpolation((float) 6.0 / 15), DELTA);
        assertEquals(0, interpolator.getInterpolation((float) 8.0 / 15), DELTA);

        // second bounce
        assertEquals(0.1875, interpolator.getInterpolation((float) 9.0 / 15), DELTA);
        assertEquals(0.25, interpolator.getInterpolation((float) 10.0 / 15), DELTA);
        assertEquals(0.1875, interpolator.getInterpolation((float) 11.0 / 15), DELTA);
        assertEquals(0, interpolator.getInterpolation((float) 12.0 / 15), DELTA);

        // third bounce
        assertEquals(0.046875, interpolator.getInterpolation((float) 12.5 / 15), DELTA);
        assertEquals(0.0625, interpolator.getInterpolation((float) 13.0 / 15), DELTA);
        assertEquals(0.046875, interpolator.getInterpolation((float) 13.5 / 15), DELTA);
        assertEquals(0, interpolator.getInterpolation((float) 14.0 / 15), DELTA);

        // fourth bounce
        assertEquals(0.01171875, interpolator.getInterpolation((float) 14.25 / 15), DELTA);
        assertEquals(0.015625, interpolator.getInterpolation((float) 14.5 / 15), DELTA);
        assertEquals(0.01171875, interpolator.getInterpolation((float) 14.75 / 15), DELTA);
        assertEquals(0, interpolator.getInterpolation((float) 15.0 / 15), DELTA);
    }

    @Test
    public void verifyFourBouncesNoDecay() {
        final BounceInterpolator interpolator = new BounceInterpolator(4, 0);

        assertEquals(1200, interpolator.getTotalDuration(300));

        // first bounce
        assertEquals(0, interpolator.getInterpolation((float) 0), DELTA);
        assertEquals(0.75, interpolator.getInterpolation((float) 1.0 / 16), DELTA);
        assertEquals(1, interpolator.getInterpolation((float) 2.0 / 16), DELTA);
        assertEquals(0.75, interpolator.getInterpolation((float) 3.0 / 16), DELTA);
        assertEquals(0, interpolator.getInterpolation((float) 4.0 / 16), DELTA);

        // second bounce
        assertEquals(0.75, interpolator.getInterpolation((float) 5.0 / 16), DELTA);
        assertEquals(1, interpolator.getInterpolation((float) 6.0 / 16), DELTA);
        assertEquals(0.75, interpolator.getInterpolation((float) 7.0 / 16), DELTA);
        assertEquals(0, interpolator.getInterpolation((float) 8.0 / 16), DELTA);

        // third bounce
        assertEquals(0.75, interpolator.getInterpolation((float) 9.0 / 16), DELTA);
        assertEquals(1, interpolator.getInterpolation((float) 10.0 / 16), DELTA);
        assertEquals(0.75, interpolator.getInterpolation((float) 11.0 / 16), DELTA);
        assertEquals(0, interpolator.getInterpolation((float) 12.0 / 16), DELTA);

        // fourth bounce
        assertEquals(0.75, interpolator.getInterpolation((float) 13.0 / 16), DELTA);
        assertEquals(1, interpolator.getInterpolation((float) 14.0 / 16), DELTA);
        assertEquals(0.75, interpolator.getInterpolation((float) 15.0 / 16), DELTA);
        assertEquals(0, interpolator.getInterpolation((float) 16.0 / 16), DELTA);
    }

    @Test
    public void verifyThreeBounces75PercentDecay() {
        final BounceInterpolator interpolator = new BounceInterpolator(3, BounceInterpolator.DEFAULT_HEIGHT_DECAY);

        assertEquals(525, interpolator.getTotalDuration(300));

        // first bounce
        assertEquals(0, interpolator.getInterpolation((float) 0), DELTA);
        assertEquals(0.75, interpolator.getInterpolation((float) 1.0 / 7), DELTA);
        assertEquals(1, interpolator.getInterpolation((float) 2.0 / 7), DELTA);
        assertEquals(0.75, interpolator.getInterpolation((float) 3.0 / 7), DELTA);
        assertEquals(0, interpolator.getInterpolation((float) 4.0 / 7), DELTA);

        // second bounce
        assertEquals(0.1875, interpolator.getInterpolation((float) 4.5 / 7), DELTA);
        assertEquals(0.25, interpolator.getInterpolation((float) 5.0 / 7), DELTA);
        assertEquals(0.1875, interpolator.getInterpolation((float) 5.5 / 7), DELTA);
        assertEquals(0, interpolator.getInterpolation((float) 6.0 / 7), DELTA);

        // third bounce
        assertEquals(0.046875, interpolator.getInterpolation((float) 6.25 / 7), DELTA);
        assertEquals(0.0625, interpolator.getInterpolation((float) 6.5 / 7), DELTA);
        assertEquals(0.046875, interpolator.getInterpolation((float) 6.75 / 7), DELTA);
        assertEquals(0, interpolator.getInterpolation((float) 7.0 / 7), DELTA);
    }
}
