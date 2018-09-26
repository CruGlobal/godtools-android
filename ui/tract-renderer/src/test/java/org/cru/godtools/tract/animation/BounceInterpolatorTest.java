package org.cru.godtools.tract.animation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BounceInterpolatorTest {
    private static final float DELTA = 0.000001f;

    private final BounceInterpolator mInterpolator = new BounceInterpolator();

    @Test
    public void verifyFirstBounce() {
        assertEquals(0, mInterpolator.getInterpolation((float) 0), DELTA);
        assertEquals(0.75, mInterpolator.getInterpolation((float) 2.0 / 15), DELTA);
        assertEquals(1, mInterpolator.getInterpolation((float) 4.0 / 15), DELTA);
        assertEquals(0.75, mInterpolator.getInterpolation((float) 6.0 / 15), DELTA);
        assertEquals(0, mInterpolator.getInterpolation((float) 8.0 / 15), DELTA);
    }

    @Test
    public void verifySecondBounce() {
        assertEquals(0, mInterpolator.getInterpolation((float) 8.0 / 15), DELTA);
        assertEquals(0.1875, mInterpolator.getInterpolation((float) 9.0 / 15), DELTA);
        assertEquals(0.25, mInterpolator.getInterpolation((float) 10.0 / 15), DELTA);
        assertEquals(0.1875, mInterpolator.getInterpolation((float) 11.0 / 15), DELTA);
        assertEquals(0, mInterpolator.getInterpolation((float) 12.0 / 15), DELTA);
    }

    @Test
    public void verifyThirdBounce() {
        assertEquals(0, mInterpolator.getInterpolation((float) 12.0 / 15), DELTA);
        assertEquals(0.046875, mInterpolator.getInterpolation((float) 12.5 / 15), DELTA);
        assertEquals(0.0625, mInterpolator.getInterpolation((float) 13.0 / 15), DELTA);
        assertEquals(0.046875, mInterpolator.getInterpolation((float) 13.5 / 15), DELTA);
        assertEquals(0, mInterpolator.getInterpolation((float) 14.0 / 15), DELTA);
    }

    @Test
    public void verifyFourthBounce() {
        assertEquals(0, mInterpolator.getInterpolation((float) 14.0 / 15), DELTA);
        assertEquals(0.01171875, mInterpolator.getInterpolation((float) 14.25 / 15), DELTA);
        assertEquals(0.015625, mInterpolator.getInterpolation((float) 14.5 / 15), DELTA);
        assertEquals(0.01171875, mInterpolator.getInterpolation((float) 14.75 / 15), DELTA);
        assertEquals(0, mInterpolator.getInterpolation((float) 15.0 / 15), DELTA);
    }
}
