package org.cru.godtools.analytics;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.adobe.mobile.Analytics;
import com.adobe.mobile.Config;
import com.adobe.mobile.Visitor;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class AdobeAnalyticsService implements AnalyticsService {
    /* Property Keys */
    private static final String ADOBE_APP_NAME = "cru.appname";
    private static final String ADOBE_LOGGED_IN_STATUS = "cru.loggedinstatus";
    private static final String ADOBE_MARKETING_CLOUD_ID = "cru.mcid";
    private static final String ADOBE_SCREEN_NAME = "cru.screenname";
    private static final String ADOBE_PREVIOUS_SCREEN_NAME = "cru.previousscreenname";

    /* Value constants */
    private static final String ADOBE_NOT_LOGGED_IN = "not logged in";
    private static final String ADOBE_GODTOOLS = "GodTools";

    /**
     * Single thread executor to serialize events on a background thread.
     */
    private final Executor mAnalyticsExecutor = Executors.newSingleThreadExecutor();

    @NonNull
    private WeakReference<Activity> mActiveActivity = new WeakReference<>(null);
    private String mPreviousScreenName;

    private AdobeAnalyticsService(@NonNull final Context context) {
        Config.setContext(context);
    }

    @Nullable
    private static AdobeAnalyticsService sInstance;
    @NonNull
    static synchronized AnalyticsService getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new AdobeAnalyticsService(context.getApplicationContext());
        }

        return sInstance;
    }

    /* BEGIN tracking methods */

    @Override
    public void onActivityResume(@NonNull final Activity activity) {
        mActiveActivity = new WeakReference<>(activity);
    }

    @Override
    public void onActivityPause(@NonNull final Activity activity) {
        mAnalyticsExecutor.execute(Config::pauseCollectingLifecycleData);
    }

    @Override
    public void onTrackScreen(@NonNull final String screen, @Nullable final String language) {
        final Activity activity = mActiveActivity.get();
        if (activity != null) {
            mAnalyticsExecutor.execute(() -> {
                final Map<String, Object> adobeContextData = adobeContextData(screen);
                Analytics.trackState(screen, adobeContextData);
                Config.collectLifecycleData(activity, adobeContextData);
                mPreviousScreenName = screen;
            });
        }
    }

    /* END tracking methods */

    /**
     * Visitor.getMarketingCloudId() may be blocking. So, we need to call it on a worker thread.
     */
    @WorkerThread
    private Map<String, Object> adobeContextData(final String screen) {
        Map<String, Object> contextData = new HashMap<>();

        contextData.put(ADOBE_SCREEN_NAME, screen);
        contextData.put(ADOBE_PREVIOUS_SCREEN_NAME, mPreviousScreenName);
        contextData.put(ADOBE_APP_NAME, ADOBE_GODTOOLS);
        contextData.put(ADOBE_MARKETING_CLOUD_ID, Visitor.getMarketingCloudId());
        contextData.put(ADOBE_LOGGED_IN_STATUS, ADOBE_NOT_LOGGED_IN);

        return contextData;
    }
}
