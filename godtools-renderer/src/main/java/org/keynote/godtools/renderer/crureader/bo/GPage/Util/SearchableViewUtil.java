package org.keynote.godtools.renderer.crureader.bo.GPage.Util;

import android.view.View;
import android.view.ViewGroup;

import org.keynote.godtools.renderer.crureader.R;

import java.util.ArrayList;

/**
 * Created by rmatt on 1/6/2017.
 */

public class SearchableViewUtil {
    public static ArrayList<View> getViewsByTag(ViewGroup root, String tag) {
        ArrayList<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }
        return views;
    }

    public static View findFallBackPanel(View view) {
        View parentView = (View) view.getParent();
        if (parentView == null) return null;

        if (parentView.getTag(R.string.fallback) != null) {
            return parentView;
        } else {
            return findFallBackPanel(parentView);
        }
    }

}
