package org.cru.godtools.base.tool.model.view;

import android.content.Context;
import android.graphics.Typeface;

import org.cru.godtools.base.tool.widget.ScaledPicassoImageView;
import org.cru.godtools.base.ui.util.LocaleTypefaceUtils;
import org.cru.godtools.xml.model.Manifest;
import org.cru.godtools.xml.model.ManifestKt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ManifestViewUtils {
    @Nullable
    public static Typeface getTypeface(@Nullable final Manifest manifest, @NonNull final Context context) {
        return manifest != null ? LocaleTypefaceUtils.getTypeface(context, manifest.getLocale()) : null;
    }

    public static void bindBackgroundImage(@Nullable final Manifest manifest,
                                           @NonNull final ScaledPicassoImageView view) {
        ResourceViewUtilsKt.bindBackgroundImage(view, ManifestKt.getBackgroundImageResource(manifest),
                                                ManifestKt.getBackgroundImageScaleType(manifest),
                                                ManifestKt.getBackgroundImageGravity(manifest));
    }
}
