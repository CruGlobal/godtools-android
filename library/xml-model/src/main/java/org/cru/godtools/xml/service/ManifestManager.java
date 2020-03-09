package org.cru.godtools.xml.service;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ManifestManager extends KotlinManifestManager {
    @Nullable
    @SuppressLint("StaticFieldLeak")
    private static ManifestManager sInstance;

    @NonNull
    public static synchronized ManifestManager getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new ManifestManager(context.getApplicationContext());
        }
        return sInstance;
    }

    private ManifestManager(@NonNull final Context context) {
        super(context);
    }
}
