package org.cru.godtools.sync.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import org.ccci.gto.android.common.util.NumberUtils;
import org.cru.godtools.base.model.Event;
import org.cru.godtools.model.Followup;
import org.cru.godtools.sync.GodToolsSyncService;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.keynote.godtools.android.db.GodToolsDao;

import static org.cru.godtools.base.model.Event.Id.FOLLOWUP_EVENT;

public final class FollowupService {
    private static final String FIELD_NAME = "name";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_DESTINATION = "destination_id";

    private final Context mContext;
    private final GodToolsDao mDao;

    private FollowupService(@NonNull final Context context) {
        mContext = context;
        mDao = GodToolsDao.getInstance(context);
        EventBus.getDefault().register(this);
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
            mDao.insertNew(followup);

            // trigger a sync of followups
            GodToolsSyncService.syncFollowups(mContext).run();
        }
    }
}
