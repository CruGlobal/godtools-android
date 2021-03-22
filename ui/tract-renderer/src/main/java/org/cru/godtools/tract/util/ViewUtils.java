package org.cru.godtools.tract.util;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

public class ViewUtils {
    public static int getTopOffset(@NonNull final ViewGroup root, @NonNull final View descendant) {
        final Rect bounds = new Rect();
        descendant.getDrawingRect(bounds);
        root.offsetDescendantRectToMyCoords(descendant, bounds);
        return bounds.top;
    }
}
