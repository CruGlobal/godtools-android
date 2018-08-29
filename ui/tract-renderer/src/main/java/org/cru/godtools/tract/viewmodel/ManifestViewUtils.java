package org.cru.godtools.tract.viewmodel;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.cru.godtools.base.tool.widget.ScaledPicassoImageView;
import org.cru.godtools.base.ui.util.LocaleTypefaceUtils;
import org.cru.godtools.xml.model.Manifest;

public class ManifestViewUtils {
    @Nullable
    public static Typeface getTypeface(@Nullable final Manifest manifest, @NonNull final Context context) {
        return manifest != null ? LocaleTypefaceUtils.getTypeface(context, manifest.getLocale()) : null;
    }

    public static void bindBackgroundImage(@Nullable final Manifest manifest,
                                           @NonNull final ScaledPicassoImageView view) {
        ResourceViewUtils.bindBackgroundImage(view, Manifest.getBackgroundImageResource(manifest),
                                              Manifest.getBackgroundImageScaleType(manifest),
                                              Manifest.getBackgroundImageGravity(manifest));
    }
}
