package org.cru.godtools.tract.compat;

import android.os.Build;
import android.widget.RelativeLayout;

public final class RelativeLayoutCompat {
    public static final int ALIGN_PARENT_START =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 ? RelativeLayout.ALIGN_PARENT_START :
                    RelativeLayout.ALIGN_PARENT_LEFT;

    public static final int ALIGN_PARENT_END =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 ? RelativeLayout.ALIGN_PARENT_END :
                    RelativeLayout.ALIGN_PARENT_RIGHT;
}
