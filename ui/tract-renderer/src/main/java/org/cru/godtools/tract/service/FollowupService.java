package org.cru.godtools.tract.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import org.ccci.gto.android.common.util.NumberUtils;
import org.cru.godtools.base.model.Event;
import org.cru.godtools.model.Followup;
import org.cru.godtools.sync.task.FollowupSyncTasks;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.keynote.godtools.android.db.GodToolsDao;

import java.io.IOException;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import static org.cru.godtools.base.model.Event.Id.FOLLOWUP_EVENT;

public final class FollowupService {
    private static final String FIELD_NAME = "name";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_DESTINATION = "destination_id";

    private final Context mContext;
    private final GodToolsDao mDao;

    private FollowupService(@NonNull final Context context) {
        mContext = context;
        mDao = GodToolsDao.Companion.getInstance(context);
        EventBus.getDefault().register(this);

        // sync any currently pending followups
        AsyncTask.THREAD_POOL_EXECUTOR.execute(this::syncPendingFollowups);
    }

    @Nullable
    @SuppressLint("StaticFieldLeak")
    private static FollowupService sInstance;

    @NonNull
    @MainThread
    public static FollowupService start(@NonNull final Context context) {
        synchronized (FollowupService.class) {
            if (sInstance == null) {
                sInstance = new FollowupService(context.getApplicationContext());
            }
        }

        return sInstance;
    }

    @WorkerThread
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onContentEvent(@NonNull final Event event) {
        if (FOLLOWUP_EVENT.equals(event.id)) {
            final Followup followup = new Followup();
            followup.setName(event.fields.get(FIELD_NAME));
            followup.setEmail(event.fields.get(FIELD_EMAIL));
            followup.setLanguageCode(event.locale);
            followup.setDestination(NumberUtils.toLong(event.fields.get(FIELD_DESTINATION), null));

            // only store this followup if it's valid
            if (followup.isValid()) {
                mDao.insertNew(followup);
                syncPendingFollowups();
            }
        }
    }

    @WorkerThread
    private void syncPendingFollowups() {
        try {
            new FollowupSyncTasks(mContext).syncFollowups();
        } catch (final IOException ignored) {
        }
    }
}
