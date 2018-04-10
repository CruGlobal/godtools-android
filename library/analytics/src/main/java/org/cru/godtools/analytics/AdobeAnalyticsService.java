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
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.ccci.gto.android.common.compat.util.LocaleCompat.toLanguageTag;
import static org.cru.godtools.analytics.AnalyticsService.tractPageToScreenName;

class AdobeAnalyticsService implements AnalyticsService {
    /* Property Keys */
    private static final String KEY_APP_NAME = "cru.appname";
    private static final String KEY_MARKETING_CLOUD_ID = "cru.mcid";
    private static final String KEY_LOGGED_IN_STATUS = "cru.loggedinstatus";
    private static final String KEY_SCREEN_NAME = "cru.screenname";
    private static final String KEY_SCREEN_NAME_PREVIOUS = "cru.previousscreenname";
    private static final String KEY_CONTENT_LANGUAGE = "cru.contentlanguage";

    /* Value constants */
    private static final String VALUE_GODTOOLS = "GodTools";
    private static final String VALUE_NOT_LOGGED_IN = "not logged in";

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
    public void onTrackScreen(@NonNull final String screen, @Nullable final Locale locale) {
        trackState(screen, locale);
    }

    @Override
    public void onTrackTractPage(@NonNull final String tract, @NonNull final Locale locale, final int page,
                                 @Nullable final Integer card) {
        trackState(tractPageToScreenName(tract, page, card), locale);
    }

    /* END tracking methods */

    private void trackState(@NonNull final String screen, @Nullable final Locale contentLocale) {
        final Activity activity = mActiveActivity.get();
        if (activity != null) {
            mAnalyticsExecutor.execute(() -> {
                final Map<String, Object> adobeContextData = adobeContextData(screen, contentLocale);
                Analytics.trackState(screen, adobeContextData);
                Config.collectLifecycleData(activity, adobeContextData);
                mPreviousScreenName = screen;
            });
        }
    }

    /**
     * Visitor.getMarketingCloudId() may be blocking. So, we need to call it on a worker thread.
     */
    @WorkerThread
    private Map<String, Object> adobeContextData(final String screen, @Nullable final Locale contentLocale) {
        Map<String, Object> contextData = new HashMap<>();

        contextData.put(KEY_APP_NAME, VALUE_GODTOOLS);
        contextData.put(KEY_MARKETING_CLOUD_ID, Visitor.getMarketingCloudId());
        contextData.put(KEY_LOGGED_IN_STATUS, VALUE_NOT_LOGGED_IN);
        contextData.put(KEY_SCREEN_NAME_PREVIOUS, mPreviousScreenName);
        contextData.put(KEY_SCREEN_NAME, screen);
        if (contentLocale != null) {
            contextData.put(KEY_CONTENT_LANGUAGE, toLanguageTag(contentLocale));
        }

        return contextData;
    }
}
