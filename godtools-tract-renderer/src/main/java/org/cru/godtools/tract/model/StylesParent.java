package org.cru.godtools.tract.model;

import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;

interface StylesParent {
    @ColorInt
    int getPrimaryColor();

    @ColorInt
    int getPrimaryTextColor();

    @ColorInt
    int getTextColor();

    @ColorInt
    static int getPrimaryColor(@Nullable final StylesParent parent) {
        return parent != null ? parent.getPrimaryColor() : Manifest.getDefaultPrimaryColor();
    }

    @ColorInt
    static int getPrimaryTextColor(@Nullable final StylesParent parent) {
        return parent != null ? parent.getPrimaryTextColor() : Manifest.getDefaultPrimaryTextColor();
    }

    @ColorInt
    static int getTextColor(@Nullable final StylesParent parent) {
        return parent != null ? parent.getTextColor() : Manifest.getDefaultTextColor();
    }
}
