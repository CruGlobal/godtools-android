package org.cru.godtools.analytics;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

import com.adobe.mobile.Analytics;
import com.adobe.mobile.Config;
import com.adobe.mobile.Visitor;

import org.ccci.gto.android.common.compat.util.LocaleCompat;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.ccci.gto.android.common.compat.util.LocaleCompat.toLanguageTag;
import static org.cru.godtools.analytics.AnalyticsService.tractPageToScreenName;

public final class AdobeAnalyticsService implements AnalyticsService {
    /* Property Keys */
    private static final String KEY_APP_NAME = "cru.appname";
    private static final String KEY_MARKETING_CLOUD_ID = "cru.mcid";
    private static final String KEY_LOGGED_IN_STATUS = "cru.loggedinstatus";
    private static final String KEY_SCREEN_NAME = "cru.screenname";
    private static final String KEY_SCREEN_NAME_PREVIOUS = "cru.previousscreenname";
    private static final String KEY_CONTENT_LANGUAGE = "cru.contentlanguage";
    private static final String KEY_CONTENT_LANGUAGE_SECONDARY = "cru.contentlanguagesecondary";
    private static final String KEY_EXIT_LINK = "cru.mobileexitlink";
    private static final String KEY_SHARE_CONTENT = "cru.shareiconengaged";
    private static final String KEY_TOGGLE_LANGUAGE = "cru.parallellanguagetoggle";

    /* Value constants */
    private static final String VALUE_GODTOOLS = "GodTools";
    private static final String VALUE_NOT_LOGGED_IN = "not logged in";

    private final Context mContext;
    /**
     * Single thread executor to serialize events on a background thread.
     */
    private final Executor mAnalyticsExecutor = Executors.newSingleThreadExecutor();

    @NonNull
    private WeakReference<Activity> mActiveActivity = new WeakReference<>(null);
    private String mPreviousScreenName;

    private AdobeAnalyticsService(@NonNull final Context context) {
        mContext = context;
        Config.setContext(context);
    }

    @Nullable
    private static AdobeAnalyticsService sInstance;
    @NonNull
    public static synchronized AdobeAnalyticsService getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new AdobeAnalyticsService(context.getApplicationContext());
        }

        return sInstance;
    }

    /* BEGIN tracking methods */

    @UiThread
    @Override
    public void onActivityResume(@NonNull final Activity activity) {
        mActiveActivity = new WeakReference<>(activity);
        mAnalyticsExecutor.execute(() -> Config.collectLifecycleData(activity, baseContextData()));
    }

    @UiThread
    @Override
    public void onActivityPause(@NonNull final Activity activity) {
        if (mActiveActivity.get() == activity) {
            mActiveActivity = new WeakReference<>(null);
            mAnalyticsExecutor.execute(Config::pauseCollectingLifecycleData);
        }
    }

    public void onProcessReferrer(@NonNull final Intent intent) {
        mAnalyticsExecutor.execute(() -> Analytics.processReferrer(mContext, intent));
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

    @Override
    public void onTrackShareAction() {
        trackAction(ACTION_SHARE, Collections.singletonMap(KEY_SHARE_CONTENT, null));
    }

    @Override
    public void onTrackExitUrl(@NonNull final Uri url) {
        trackAction(ACTION_EXIT_LINK, Collections.singletonMap(KEY_EXIT_LINK, url.toString()));
    }

    @Override
    public void onTrackToggleLanguage(@NonNull final Locale newLocale) {
        final Map<String, Object> attrs = new HashMap<>();
        attrs.put(KEY_TOGGLE_LANGUAGE, null);
        attrs.put(KEY_CONTENT_LANGUAGE_SECONDARY, LocaleCompat.toLanguageTag(newLocale));
        trackAction(ACTION_TOGGLE_LANGUAGE, attrs);
    }

    /* END tracking methods */

    @AnyThread
    private void trackAction(@NonNull final String action, @Nullable final Map<String, Object> attributes) {
        mAnalyticsExecutor.execute(() -> {
            final Map<String, Object> data = baseContextData();
            if (mPreviousScreenName != null) {
                data.put(KEY_SCREEN_NAME, mPreviousScreenName);
            }
            if (attributes != null) {
                data.putAll(attributes);
            }
            Analytics.trackAction(action, baseContextData());
        });
    }

    @AnyThread
    private void trackState(@NonNull final String screen, @Nullable final Locale contentLocale) {
        mAnalyticsExecutor.execute(() -> {
            final Map<String, Object> adobeContextData = stateContextData(screen, contentLocale);
            Analytics.trackState(screen, adobeContextData);
            mPreviousScreenName = screen;
        });
    }

    /**
     * Visitor.getMarketingCloudId() may be blocking. So, we need to call it on a worker thread.
     */
    @WorkerThread
    private Map<String, Object> baseContextData() {
        final Map<String, Object> data = new HashMap<>();
        data.put(KEY_APP_NAME, VALUE_GODTOOLS);
        data.put(KEY_MARKETING_CLOUD_ID, Visitor.getMarketingCloudId());
        data.put(KEY_LOGGED_IN_STATUS, VALUE_NOT_LOGGED_IN);
        return data;
    }

    @WorkerThread
    private Map<String, Object> stateContextData(final String screen, @Nullable final Locale contentLocale) {
        final Map<String, Object> data = baseContextData();
        data.put(KEY_SCREEN_NAME_PREVIOUS, mPreviousScreenName);
        data.put(KEY_SCREEN_NAME, screen);
        if (contentLocale != null) {
            data.put(KEY_CONTENT_LANGUAGE, toLanguageTag(contentLocale));
        }
        return data;
    }
}
