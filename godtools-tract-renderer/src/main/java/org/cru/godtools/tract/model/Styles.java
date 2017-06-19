package org.cru.godtools.tract.model;

import android.support.annotation.ColorInt;
import android.support.annotation.DimenRes;
import android.support.annotation.Nullable;

import org.cru.godtools.tract.R;

// XXX: I really want to use default methods for this interface, but default methods are not backwards compatible
interface Styles {
    @ColorInt
    int getPrimaryColor();

    @ColorInt
    int getPrimaryTextColor();

    @ColorInt
    int getTextColor();

    @ColorInt
    int getButtonColor();

    @DimenRes
    int getTextSize();

    @ColorInt
    static int getPrimaryColor(@Nullable final Styles parent) {
        return parent != null ? parent.getPrimaryColor() : Manifest.getDefaultPrimaryColor();
    }

    @ColorInt
    static int getPrimaryTextColor(@Nullable final Styles parent) {
        return parent != null ? parent.getPrimaryTextColor() : Manifest.getDefaultPrimaryTextColor();
    }

    @ColorInt
    static int getTextColor(@Nullable final Styles parent) {
        return parent != null ? parent.getTextColor() : Manifest.getDefaultTextColor();
    }

    @ColorInt
    static int getButtonColor(@Nullable final Styles parent) {
        return parent != null ? parent.getButtonColor() : getPrimaryColor(null);
    }

    @DimenRes
    static int getTextSize(@Nullable final Styles parent) {
        return parent != null ? parent.getTextSize() : R.dimen.text_size_base;
    }
}
