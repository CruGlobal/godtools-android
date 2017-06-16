package org.cru.godtools.tract.model;

import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;

// XXX: I really want to use default methods for this interface, but default methods are not backwards compatible
interface StylesParent {
    @ColorInt
    int getPrimaryColor();

    @ColorInt
    int getPrimaryTextColor();

    @ColorInt
    int getTextColor();

    @ColorInt
    int getButtonColor();

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

    @ColorInt
    static int getButtonColor(@Nullable final StylesParent parent) {
        return parent != null ? parent.getButtonColor() : getPrimaryColor(null);
    }
}
