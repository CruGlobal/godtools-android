package org.cru.godtools.tract.util;

import android.graphics.Rect;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

public class ViewUtils {
    public static int getTopOffset(@NonNull final ViewGroup root, @IdRes final int id) {
        final View descendant = root.findViewById(id);
        return descendant != null ? getTopOffset(root, descendant) : 0;
    }

//    public static int getTopOffset(@NonNull final ViewGroup root, @NonNull final View descendant) {
//        if (root == descendant) {
//            return 0;
//        } else {
//            final int offset = descendant.getTop();
//            final ViewParent parent = descendant.getParent();
//            if (parent instanceof View) {
//                return getTopOffset(root, (View) parent) + offset;
//            }
//            return offset;
//        }
//    }

    public static int getTopOffset(@NonNull final ViewGroup root, @NonNull final View descendant) {
        final Rect bounds = new Rect();
        descendant.getDrawingRect(bounds);
        root.offsetDescendantRectToMyCoords(descendant, bounds);
        return bounds.top;
    }
}
