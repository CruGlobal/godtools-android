package org.keynote.godtools.android.util;

import android.support.annotation.Nullable;
import android.widget.TextView;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.model.Resource;

public final class ViewUtils {
    public static void bindShares(@Nullable final TextView view, @Nullable final Resource resource) {
        bindShares(view, resource != null ? resource.getShares() : 0);
    }

    public static void bindShares(@Nullable final TextView view, final int shares) {
        if (view != null) {
            view.setText(view.getResources().getQuantityString(R.plurals.label_resources_shares, shares, shares));
        }
    }
}
