package org.cru.godtools.tract.util;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

public final class AutoAttachingGlobalLayoutListener
        implements View.OnAttachStateChangeListener, OnGlobalLayoutListener {
    @NonNull
    private final OnGlobalLayoutListener mDelegate;

    private AutoAttachingGlobalLayoutListener(@NonNull final OnGlobalLayoutListener delegate) {
        mDelegate = delegate;
    }

    public static void attach(@NonNull final View view, @NonNull final OnGlobalLayoutListener delegate) {
        view.addOnAttachStateChangeListener(new AutoAttachingGlobalLayoutListener(delegate));
    }

    @NonNull
    public static AutoAttachingGlobalLayoutListener wrap(@NonNull final OnGlobalLayoutListener delegate) {
        return new AutoAttachingGlobalLayoutListener(delegate);
    }

    @Override
    public final void onViewAttachedToWindow(final View v) {
        v.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public final void onViewDetachedFromWindow(final View v) {
        v.getViewTreeObserver().removeGlobalOnLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        mDelegate.onGlobalLayout();
    }
}
