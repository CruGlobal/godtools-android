package org.cru.godtools.tract.util;

public class BounceUtil {

    public static float getBounceInOut(float input){
        if (input < .5f){
            return getBounceIn(input * 2f) * 0.5f;
        }
        return getBounceOut(input * 2f -1f) * 0.5f + 0.5f;
    }


    private static float getBounceIn(float input) {
        return 1f - getBounceOut(1f - input);
    }

    private static float getBounceOut(float input) {
        if (input < 1 / 2.75) {
            return (float) (7.5625 * input * input);
        } else if (input < 2 / 2.75) {
            return (float) (7.5625 * (input -= 1.5 / 2.75) * input + 0.75);
        } else if (input < 2.5 / 2.75) {
            return (float) (7.5625 * (input -= 2.25 / 2.75) * input + 0.9375);
        } else {
            return (float) (7.5625 * (input -= 2.625 / 2.75) * input + 0.984375);
        }
    }
}
