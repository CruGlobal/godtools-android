package org.cru.godtools.tract.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class JavaPageContentLayout extends PageContentLayout {

    // region Initialization
    public JavaPageContentLayout(@NonNull final Context context) {
        this(context, null);
    }

    public JavaPageContentLayout(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JavaPageContentLayout(@NonNull final Context context, @Nullable final AttributeSet attrs,
                                 final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public JavaPageContentLayout(@NonNull final Context context, @Nullable final AttributeSet attrs,
                                 final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    // endregion Initialization
}
