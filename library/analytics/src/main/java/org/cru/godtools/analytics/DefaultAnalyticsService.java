package org.cru.godtools.analytics;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.cru.godtools.base.model.Event;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public final class DefaultAnalyticsService implements AnalyticsService {
    private final List<AnalyticsService> mServices = new ArrayList<>();

    private DefaultAnalyticsService(@NonNull final Context context) {
        mServices.add(GoogleAnalyticsService.getInstance(context));
        mServices.add(AdobeAnalyticsService.getInstance(context));
        mServices.add(SnowplowAnalyticsService.getInstance(context));

        EventBus.getDefault().register(this);
    }

    @Nullable
    private static DefaultAnalyticsService sInstance;
    @NonNull
    static synchronized DefaultAnalyticsService getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new DefaultAnalyticsService(context.getApplicationContext());
        }

        return sInstance;
    }

    /* BEGIN tracking methods */

    @Override
    public void onActivityResume(@NonNull final Activity activity) {
        for (final AnalyticsService service : mServices) {
            service.onActivityResume(activity);
        }
    }

    @Override
    public void onActivityPause(@NonNull final Activity activity) {
        for (final AnalyticsService service : mServices) {
            service.onActivityPause(activity);
        }
    }

    @Override
    public void onTrackScreen(@NonNull final String screen) {
        for (final AnalyticsService service : mServices) {
            service.onTrackScreen(screen);
        }
    }

    @Override
    public void onTrackScreen(@NonNull final String screen, @Nullable final String language) {
        for (final AnalyticsService service : mServices) {
            service.onTrackScreen(screen, language);
        }
    }

    @Override
    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTrackContentEvent(@NonNull final Event event) {
        for (final AnalyticsService service : mServices) {
            service.onTrackContentEvent(event);
        }
    }

    @Override
    public void onTrackEveryStudentSearch(@NonNull final String query) {
        for (final AnalyticsService service : mServices) {
            service.onTrackEveryStudentSearch(query);
        }
    }

    /* END tracking methods */
}
