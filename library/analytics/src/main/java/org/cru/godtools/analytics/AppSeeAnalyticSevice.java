package org.cru.godtools.analytics;

import com.appsee.Appsee;

import org.cru.godtools.analytics.model.AnalyticsScreenEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

public class AppSeeAnalyticSevice implements AnalyticsService {
    private AppSeeAnalyticSevice() {
        EventBus.getDefault().register(this);
    }

    @Nullable
    private static AppSeeAnalyticSevice sInstance;

    @NonNull
    public static synchronized AppSeeAnalyticSevice getInstance() {
        if (sInstance == null) {
            sInstance = new AppSeeAnalyticSevice();
        }

        return sInstance;
    }

    @UiThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAnalyticScreenEvent(@NonNull final AnalyticsScreenEvent event) {
        Appsee.startScreen(event.getScreen());
    }
}
