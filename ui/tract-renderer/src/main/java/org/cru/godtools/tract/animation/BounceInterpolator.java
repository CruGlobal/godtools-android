package org.cru.godtools.tract.animation;

import android.view.animation.Interpolator;

public final class BounceInterpolator implements Interpolator {
    @Override
    public float getInterpolation(final float i) {
        // we repeat the same negative quadratic equation 4 times, splitting the duration in half each time. For
        // simplicity we center the equation on the y-axis.
        final double x;
        final double verticalOffset;
        // first bounce
        if (i >= 0 && i < 8.0 / 15) {
            x = i - 4.0 / 15.0;
            verticalOffset = 1;
        }
        // second bounce
        else if (i < 12.0 / 15) {
            x = i - 10.0 / 15;
            verticalOffset = 0.25; // 1/4
        }
        // third bounce
        else if (i < 14.0 / 15) {
            x = i - 13.0 / 15;
            verticalOffset = 0.0625; // 1/16
        }
        // fourth bounce
        else if (i < 1) {
            x = i - 14.5 / 15;
            verticalOffset = 0.015625; // 1/64
        }
        // other
        else {
            return 0;
        }

        return (float) ((x * x * -225.0 / 16.0) + verticalOffset);
    }
}
