package org.cru.godtools.analytics;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.adobe.mobile.Analytics;
import com.adobe.mobile.Config;
import com.adobe.mobile.Visitor;

import org.cru.godtools.analytics.model.AnalyticsActionEvent;
import org.cru.godtools.analytics.model.AnalyticsBaseEvent;
import org.cru.godtools.analytics.model.AnalyticsScreenEvent;
import org.cru.godtools.analytics.model.AnalyticsSystem;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import me.thekey.android.TheKey;

import static me.thekey.android.Attributes.ATTR_GR_MASTER_PERSON_ID;
import static org.ccci.gto.android.common.compat.util.LocaleCompat.toLanguageTag;

public final class AdobeAnalyticsService implements AnalyticsService, Application.ActivityLifecycleCallbacks {
    /* Property Keys */
    private static final String KEY_APP_NAME = "cru.appname";
    private static final String KEY_MARKETING_CLOUD_ID = "cru.mcid";
    private static final String KEY_SSO_GUID = "cru.ssoguid";
    private static final String KEY_GR_MASTER_PERSON_ID = "cru.grmpid";
    private static final String KEY_LOGGED_IN_STATUS = "cru.loggedinstatus";
    private static final String KEY_SCREEN_NAME = "cru.screenname";
    private static final String KEY_SCREEN_NAME_PREVIOUS = "cru.previousscreenname";
    private static final String KEY_CONTENT_LANGUAGE = "cru.contentlanguage";
    public static final String KEY_CONTENT_LANGUAGE_SECONDARY = "cru.contentlanguagesecondary";
    private static final String KEY_EXIT_LINK = "cru.mobileexitlink";
    private static final String KEY_SHARE_CONTENT = "cru.shareiconengaged";
    private static final String KEY_SITE_SECTION = "cru.sitesection";
    private static final String KEY_SITE_SUB_SECTION = "cru.sitesubsection";

    /* Value constants */
    private static final String VALUE_GODTOOLS = "GodTools";
    private static final String VALUE_LOGGED_IN = "logged in";
    private static final String VALUE_NOT_LOGGED_IN = "not logged in";

    private final Context mContext;
    private final TheKey mTheKey;
    /**
     * Single thread executor to serialize events on a background thread.
     */
    private final Executor mAnalyticsExecutor = Executors.newSingleThreadExecutor();

    @NonNull
    private WeakReference<Activity> mActiveActivity = new WeakReference<>(null);
    private String mPreviousScreenName;

    private AdobeAnalyticsService(@NonNull final Context context) {
        mContext = context;
        mTheKey = TheKey.getInstance(mContext);
        Config.setContext(context);

        EventBus.getDefault().register(this);
        ((Application) mContext.getApplicationContext()).registerActivityLifecycleCallbacks(this);
    }

    @Nullable
    @SuppressLint("StaticFieldLeak")
    private static AdobeAnalyticsService sInstance;

    @NonNull
    public static synchronized AdobeAnalyticsService getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new AdobeAnalyticsService(context.getApplicationContext());
        }

        return sInstance;
    }

    // region Tracking Events

    @WorkerThread
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onAnalyticsActionEvent(@NonNull final AnalyticsActionEvent event) {
        if (event.isForSystem(AnalyticsSystem.ADOBE)) {
            trackAction(event.getAction(), event, event.getAdobeAttributes());
        }
    }

    @WorkerThread
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onAnalyticsScreenEvent(@NonNull final AnalyticsScreenEvent event) {
        if (event.isForSystem(AnalyticsSystem.ADOBE)) {
            trackState(event);
        }
    }

    @Override
    public void onTrackShareAction() {
        trackAction(ACTION_SHARE, null, Collections.singletonMap(KEY_SHARE_CONTENT, 1));
    }

    @Override
    public void onTrackExitUrl(@NonNull final Uri url) {
        trackAction(ACTION_EXIT_LINK, null, Collections.singletonMap(KEY_EXIT_LINK, url.toString()));
    }

    // region ActivityLifecycleCallbacks

    @Override
    public void onActivityCreated(final Activity activity, final Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(final Activity activity) {}

    @Override
    @MainThread
    public void onActivityResumed(final Activity activity) {
        final String guid = mTheKey.getDefaultSessionGuid();
        mActiveActivity = new WeakReference<>(activity);
        mAnalyticsExecutor.execute(() -> Config.collectLifecycleData(activity, baseContextData(guid, null)));
    }

    @Override
    public void onActivityPaused(final Activity activity) {
        if (mActiveActivity.get() == activity) {
            mActiveActivity = new WeakReference<>(null);
            mAnalyticsExecutor.execute(Config::pauseCollectingLifecycleData);
        }
    }

    @Override
    public void onActivityStopped(final Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(final Activity activity, final Bundle outState) {}

    @Override
    public void onActivityDestroyed(final Activity activity) {}

    // endregion ActivityLifecycleCallbacks

    // endregion Tracking Events

    @AnyThread
    private void trackAction(@NonNull final String action, @Nullable final AnalyticsActionEvent event,
                             @Nullable final Map<String, ?> attributes) {
        final String guid = mTheKey.getDefaultSessionGuid();
        mAnalyticsExecutor.execute(() -> {
            final Map<String, Object> data = baseContextData(guid, event);
            if (mPreviousScreenName != null) {
                data.put(KEY_SCREEN_NAME, mPreviousScreenName);
            }
            if (attributes != null) {
                data.putAll(attributes);
            }
            Analytics.trackAction(action, data);
        });
    }

    @AnyThread
    private void trackState(@NonNull final AnalyticsScreenEvent event) {
        final String guid = mTheKey.getDefaultSessionGuid();
        mAnalyticsExecutor.execute(() -> {
            Analytics.trackState(event.getScreen(), stateContextData(guid, event));
            mPreviousScreenName = event.getScreen();
        });
    }

    /**
     * Visitor.getMarketingCloudId() may be blocking. So, we need to call it on a worker thread.
     */
    @WorkerThread
    private Map<String, Object> baseContextData(@Nullable final String guid, @Nullable final AnalyticsBaseEvent event) {
        final Map<String, Object> data = new HashMap<>();
        data.put(KEY_APP_NAME, VALUE_GODTOOLS);
        data.put(KEY_MARKETING_CLOUD_ID, Visitor.getMarketingCloudId());

        // login state
        data.put(KEY_LOGGED_IN_STATUS, guid != null ? VALUE_LOGGED_IN : VALUE_NOT_LOGGED_IN);
        if (guid != null) {
            data.put(KEY_SSO_GUID, guid);
            final String grMasterPersonId = mTheKey.getAttributes(guid).getAttribute(ATTR_GR_MASTER_PERSON_ID);
            if (grMasterPersonId != null) {
                data.put(KEY_GR_MASTER_PERSON_ID, grMasterPersonId);
            }
        }

        if (event != null) {
            final Locale contentLocale = event.getLocale();
            if (contentLocale != null) {
                data.put(KEY_CONTENT_LANGUAGE, toLanguageTag(contentLocale));
            }

            final String siteSection = event.getAdobeSiteSection();
            if (siteSection != null) {
                data.put(KEY_SITE_SECTION, siteSection);
            }
            final String siteSubSection = event.getAdobeSiteSubSection();
            if (siteSubSection != null) {
                data.put(KEY_SITE_SUB_SECTION, siteSubSection);
            }
        }

        return data;
    }

    /**
     * This method created the Data to be passed to Adobe my converting it to a HashMap
     *
     * @param guid  user id from key
     * @param event The analytics event we are generating context data for
     * @return The data to be tracked by Adobe Analytics
     */
    @WorkerThread
    private Map<String, Object> stateContextData(@Nullable final String guid,
                                                 @NonNull final AnalyticsScreenEvent event) {
        final Map<String, Object> data = baseContextData(guid, event);
        data.put(KEY_SCREEN_NAME_PREVIOUS, mPreviousScreenName);
        data.put(KEY_SCREEN_NAME, event.getScreen());
        return data;
    }
}
