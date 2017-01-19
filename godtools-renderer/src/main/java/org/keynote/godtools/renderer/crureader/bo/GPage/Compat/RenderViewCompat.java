 package org.keynote.godtools.renderer.crureader.bo.GPage.Compat;

import android.os.Build;
import android.widget.TextView;

import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderConstants;

import java.util.concurrent.atomic.AtomicInteger;

public class RenderViewCompat {

    public static final boolean SDK_LOLLIPOP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;  //21

    public static final boolean SDK_JELLY_BEAN = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2; //18

    public static final boolean SDK_KIT_KAT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT; //18

    public static final boolean SDK_ICS_OR_PRIOR = Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public static int generateViewId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF)
                newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    public static void textViewAlign(TextView textView, String textAlign) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textView.setTextAlignment(RenderConstants.getTextAlign(textAlign));
        } else {
            textView.setGravity(RenderConstants.getGravityFromAlign(textAlign));
        }

    }

}
