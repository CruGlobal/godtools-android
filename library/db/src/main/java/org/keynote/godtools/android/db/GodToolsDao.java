package org.keynote.godtools.android.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class GodToolsDao extends GodToolsDaoKotlin {
    private GodToolsDao(@NonNull final Context context) {
        super(context);
    }

    @Nullable
    private static GodToolsDao sInstance;

    @NonNull
    public static GodToolsDao getInstance(@NonNull final Context context) {
        synchronized (GodToolsDao.class) {
            if (sInstance == null) {
                sInstance = new GodToolsDao(context.getApplicationContext());
            }
        }

        return sInstance;
    }
}
