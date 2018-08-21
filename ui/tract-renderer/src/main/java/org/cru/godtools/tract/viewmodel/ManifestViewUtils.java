package org.cru.godtools.tract.viewmodel;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.cru.godtools.tract.model.Manifest;
import org.cru.godtools.tract.widget.ScaledPicassoImageView;

public class ManifestViewUtils {
    public static void bindBackgroundImage(@Nullable final Manifest manifest,
                                           @NonNull final ScaledPicassoImageView view) {
        ResourceViewUtils.bindBackgroundImage(view, Manifest.getBackgroundImageResource(manifest),
                                              Manifest.getBackgroundImageScaleType(manifest),
                                              Manifest.getBackgroundImageGravity(manifest));
    }
}
