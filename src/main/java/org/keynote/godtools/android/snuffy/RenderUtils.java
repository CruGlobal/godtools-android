package org.keynote.godtools.android.snuffy;

import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.widget.TextView;

import org.keynote.godtools.android.utils.TypedValueUtils;

public class RenderUtils {
    @NonNull
    public static <V extends TextView> V scaleTextSize(@NonNull final V view, final double scale) {
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (view.getTextSize() * scale));
        return view;
    }

    @NonNull
    public static <V extends TextView> V unapplyTextSizeUnit(@NonNull final V view,
                                                             final int unit) {
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, TypedValueUtils
                .unapplyDimension(unit, view.getTextSize(), view.getResources().getDisplayMetrics()));
        return view;
    }
}
