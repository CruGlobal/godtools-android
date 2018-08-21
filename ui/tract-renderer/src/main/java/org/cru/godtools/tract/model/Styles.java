package org.cru.godtools.tract.model;

import android.support.annotation.ColorInt;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.cru.godtools.tract.model.Text.Align;

// XXX: I really want to use default methods for this interface, but default methods are not backwards compatible
public interface Styles {
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

    @NonNull
    Align getTextAlign();

    @ColorInt
    static int getPrimaryColor(@Nullable final Styles styles) {
        return styles != null ? styles.getPrimaryColor() : Manifest.getDefaultPrimaryColor();
    }

    @ColorInt
    static int getPrimaryTextColor(@Nullable final Styles styles) {
        return styles != null ? styles.getPrimaryTextColor() : Manifest.getDefaultPrimaryTextColor();
    }

    @ColorInt
    static int getTextColor(@Nullable final Styles styles) {
        return styles != null ? styles.getTextColor() : Manifest.getDefaultTextColor();
    }

    @ColorInt
    static int getButtonColor(@Nullable final Styles styles) {
        return styles != null ? styles.getButtonColor() : getPrimaryColor(null);
    }

    @DimenRes
    static int getTextSize(@Nullable final Styles styles) {
        return styles != null ? styles.getTextSize() : Manifest.getDefaultTextSize();
    }

    @NonNull
    static Align getTextAlign(@Nullable final Styles styles) {
        return styles != null ? styles.getTextAlign() : Manifest.getDefaultTextAlign();
    }
}
