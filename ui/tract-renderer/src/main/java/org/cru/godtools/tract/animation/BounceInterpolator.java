package org.cru.godtools.tract.animation;

import android.view.animation.Interpolator;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class BounceInterpolator implements Interpolator {
    public static final int DEFAULT_BOUNCES = 4;
    public static final double DEFAULT_HEIGHT_DECAY = 0.75;

    private final int mBounces;
    private final double mHeightDecay;
    private final double mTimeDecay;

    private final double mTotalTime;

    public BounceInterpolator() {
        this(DEFAULT_BOUNCES, DEFAULT_HEIGHT_DECAY);
    }

    public BounceInterpolator(final int bounces, final double heightDecay) {
        mBounces = bounces;
        mHeightDecay = 1 - heightDecay;
        mTimeDecay = Math.sqrt(mHeightDecay);

        double bounceTime = 1;
        double totalTime = 0;
        for (int i = 0; i < mBounces; i++) {
            totalTime += bounceTime;
            bounceTime *= mTimeDecay;
        }
        mTotalTime = totalTime;
    }

    @Override
    public float getInterpolation(final float input) {
        // bounds check
        if (input <= 0 || input >= 1) {
            return 0;
        }

        // determine which bounce this is (and the x offset)
        int bounce;
        double inputOffset = 0;
        for (bounce = 0; bounce < mBounces; bounce++) {
            double bounceDuration = Math.pow(mTimeDecay, bounce) / mTotalTime;
            if (input <= inputOffset + bounceDuration) {
                // current bounce, center the quadratic for this bounce and quit looping
                inputOffset += bounceDuration / 2;
                break;
            }

            inputOffset += bounceDuration;
        }

        // Our base quadratic equation is "-4x^2", it is a negative equation that can be shifted to completely fill the
        // (0,0) - (1,1) area of an interpolator curve.
        // we shift & scale this quadratic equation to make each bounce curve in this interpolator.
        // x is our input value shifted to center the curve on the y axis
        final double x = input - inputOffset;
        final double q = -4 * x * x;

        // we scale the quadratic equation to match the width of the first bounce. we do this by dividing the quadratic
        // by the first bounce width squared.
        // width = 1 / totalTime
        // q / width^2 ≡ q * totalTime^2
        final double output = q * mTotalTime * mTotalTime;

        // we calculate the output offset by decaying the full bounce by the number of bounces
        final double outputOffset = Math.pow(mHeightDecay, bounce);

        return (float) (output + outputOffset);
    }
}
