package org.keynote.godtools.android.db;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.keynote.godtools.android.dao.DBAdapter;

public class GodToolsDao extends DBAdapter {
    private GodToolsDao(@NonNull final Context context) {
        super(GodToolsDatabase.getInstance(context));
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
