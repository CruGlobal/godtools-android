package org.cru.godtools.sync;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.SimpleArrayMap;

import org.cru.godtools.api.GodToolsApi;
import org.cru.godtools.model.Base;
import org.greenrobot.eventbus.EventBus;
import org.keynote.godtools.android.db.GodToolsDao;

import java.util.Collection;

import static android.content.ContentResolver.SYNC_EXTRAS_MANUAL;

@WorkerThread
abstract class BaseSyncTasks {
    private final Context mContext;
    final GodToolsApi mApi;
    final GodToolsDao mDao;
    private final EventBus mEventBus;

    BaseSyncTasks(@NonNull final Context context) {
        mContext = context;
        mApi = GodToolsApi.getInstance(mContext);
        mDao = GodToolsDao.getInstance(mContext);
        mEventBus = EventBus.getDefault();
    }

    static boolean isForced(@NonNull final Bundle extras) {
        return extras.getBoolean(SYNC_EXTRAS_MANUAL, false);
    }

    @NonNull
    static <E extends Base> LongSparseArray<E> index(@NonNull final Collection<E> items) {
        final LongSparseArray<E> index = new LongSparseArray<>();
        for (final E item : items) {
            index.put(item.getId(), item);
        }
        return index;
    }

    static void coalesceEvent(@NonNull final SimpleArrayMap<Class<?>, Object> events, @NonNull final Object event) {
        final Class<?> type = event.getClass();
        Object currEvent = events.get(type);
        if (currEvent != null) {
            // coalesce any events that need to be coalesced
        } else {
            currEvent = event;
        }
        events.put(type, currEvent);
    }

    void sendEvents(@NonNull final SimpleArrayMap<Class<?>, Object> events) {
        for (int i = 0; i < events.size(); i++) {
            mEventBus.post(events.valueAt(i));
        }
        events.clear();
    }
}
