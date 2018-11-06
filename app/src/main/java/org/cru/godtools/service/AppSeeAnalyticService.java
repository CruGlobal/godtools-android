package org.cru.godtools.service;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.appsee.Appsee;
import com.appsee.AppseeListener;
import com.appsee.AppseeScreenDetectedInfo;
import com.appsee.AppseeSessionEndedInfo;
import com.appsee.AppseeSessionEndingInfo;
import com.appsee.AppseeSessionStartedInfo;
import com.appsee.AppseeSessionStartingInfo;
import com.crashlytics.android.Crashlytics;

import org.cru.godtools.analytics.model.AnalyticsActionEvent;
import org.cru.godtools.analytics.model.AnalyticsScreenEvent;
import org.cru.godtools.analytics.model.AnalyticsSystem;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import static org.cru.godtools.analytics.model.AnalyticsScreenEvent.SITE_SECTION_MENU;
import static org.cru.godtools.analytics.model.AnalyticsScreenEvent.SITE_SECTION_TOOLS;

public class AppSeeAnalyticService implements Application.ActivityLifecycleCallbacks, AppseeListener {
    @Nullable
    private static AppSeeAnalyticService sInstance;

    private AppSeeAnalyticService(Application application) {
        EventBus.getDefault().register(this);
        application.registerActivityLifecycleCallbacks(this);
        Appsee.addAppseeListener(this);
    }

    @NonNull
    public static synchronized AppSeeAnalyticService start(Application application) {
        if (sInstance == null) {
            sInstance = new AppSeeAnalyticService(application);
        }
        return sInstance;
    }

    @UiThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAnalyticScreenEvent(@NonNull final AnalyticsScreenEvent event) {
        if (!event.isForSystem(AnalyticsSystem.APPSEE)) {
            return;
        }
        // AppSee should match Adobe for integration
        String siteSection = (SITE_SECTION_TOOLS.equals(event.getAdobeSiteSection()) ||
                SITE_SECTION_MENU.equals(event.getAdobeSiteSection())) ? event.getAdobeSiteSection() :
                event.getScreen();

        String siteSubSection = event.getAdobeSiteSubSection();

        String screen = siteSubSection != null ? String.format("%s - %s", siteSection, siteSubSection) : siteSection;

        Appsee.startScreen(screen);
    }

    @Subscribe
    public void onAnalyticActionEvent(@NonNull final AnalyticsActionEvent event) {
        if (!event.isForSystem(AnalyticsSystem.APPSEE)) {
            return;
        }

        Map<String, Object> data = (Map<String, Object>) event.getAttributes();
        if (data != null) {
            Appsee.addEvent(event.getAction(), data);
        } else {
            Appsee.addEvent(event.getAction());
        }
    }

    //region LifeCycle Callbacks
    @Override
    public void onActivityCreated(final Activity activity, final Bundle savedInstanceState) {
        Appsee.start();
    }

    @Override
    public void onActivityStarted(final Activity activity) {}

    @Override
    public void onActivityResumed(final Activity activity) {}

    @Override
    public void onActivityPaused(final Activity activity) {}

    @Override
    public void onActivityStopped(final Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(final Activity activity, final Bundle outState) {}

    @Override
    public void onActivityDestroyed(final Activity activity) {}
    //endregion LifeCycle Callbacks

    //region AppSee LifeCycle Callbacks
    @Override
    public void onAppseeSessionStarting(final AppseeSessionStartingInfo appseeSessionStartingInfo) {
        String crashlyticsAppSeeId = Appsee.generate3rdPartyId("Crashlytics", false);
        Crashlytics.setString(
                "AppseeSessionUrl",
                String.format("https://dashboard.appsee.com/3rdparty/crashlytics/%s", crashlyticsAppSeeId));
    }

    @Override
    public void onAppseeSessionStarted(final AppseeSessionStartedInfo appseeSessionStartedInfo) {}

    @Override
    public void onAppseeSessionEnding(final AppseeSessionEndingInfo appseeSessionEndingInfo) {}

    @Override
    public void onAppseeSessionEnded(final AppseeSessionEndedInfo appseeSessionEndedInfo) {}

    @Override
    public void onAppseeScreenDetected(final AppseeScreenDetectedInfo appseeScreenDetectedInfo) {}
    //endregion AppSee LifeCycle Callbacks
}
