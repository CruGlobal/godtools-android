package org.cru.godtools.tract.animation;

import android.view.animation.Interpolator;

public final class BounceInterpolator implements Interpolator {
    @Override
    public float getInterpolation(final float i) {
        // we repeat the same negative quadratic equation 4 times, splitting the duration in half each time. For
        // simplicity we center the equation on the y-axis.
        final double x;
        final double verticalOffset;
        if (i >= 0 && i < 8.0 / 15) {
            // first bounce
            x = i - 4.0 / 15.0;
            verticalOffset = 1;
        } else if (i < 12.0 / 15) {
            // second bounce
            x = i - 10.0 / 15;
            verticalOffset = 0.25; // 1/4
        } else if (i < 14.0 / 15) {
            // third bounce
            x = i - 13.0 / 15;
            verticalOffset = 0.0625; // 1/16
        } else if (i < 1) {
            // fourth bounce
            x = i - 14.5 / 15;
            verticalOffset = 0.015625; // 1/64
        } else {
            // other
            return 0;
        }

        return (float) ((x * x * -225.0 / 16.0) + verticalOffset);
    }
}
