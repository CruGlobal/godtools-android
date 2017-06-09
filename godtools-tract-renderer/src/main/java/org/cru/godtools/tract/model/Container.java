package org.cru.godtools.tract.model;

import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;

interface Container {
    @ColorInt
    int getPrimaryColor();

    @ColorInt
    int getPrimaryTextColor();

    @ColorInt
    int getTextColor();

    @ColorInt
    static int getPrimaryColor(@Nullable final Container container) {
        return container != null ? container.getPrimaryColor() : Manifest.getDefaultPrimaryColor();
    }

    @ColorInt
    static int getPrimaryTextColor(@Nullable final Container container) {
        return container != null ? container.getPrimaryTextColor() : Manifest.getDefaultPrimaryTextColor();
    }

    @ColorInt
    static int getTextColor(@Nullable final Container container) {
        return container != null ? container.getTextColor() : Manifest.getDefaultTextColor();
    }
}
