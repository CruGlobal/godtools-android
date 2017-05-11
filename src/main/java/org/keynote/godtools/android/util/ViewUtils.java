package org.keynote.godtools.android.util;

import android.support.annotation.Nullable;
import android.widget.TextView;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.model.Tool;

public final class ViewUtils {
    public static void bindShares(@Nullable final TextView view, @Nullable final Tool tool) {
        bindShares(view, tool != null ? tool.getShares() : 0);
    }

    public static void bindShares(@Nullable final TextView view, final int shares) {
        if (view != null) {
            view.setText(view.getResources().getQuantityString(R.plurals.label_tools_shares, shares, shares));
        }
    }
}
