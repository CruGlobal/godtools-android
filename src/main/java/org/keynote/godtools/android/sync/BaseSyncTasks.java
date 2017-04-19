package org.keynote.godtools.android.sync;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import org.keynote.godtools.android.api.GodToolsApi;
import org.keynote.godtools.android.db.GodToolsDao;

@WorkerThread
abstract class BaseSyncTasks {
    private final Context mContext;
    final GodToolsApi mApi;
    final GodToolsDao mDao;

    BaseSyncTasks(@NonNull final Context context) {
        mContext = context;
        mApi = GodToolsApi.getInstance(mContext);
        mDao = GodToolsDao.getInstance(mContext);
    }
}
