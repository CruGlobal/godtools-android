package org.cru.godtools.analytics;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.AnyThread;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.adobe.mobile.Analytics;
import com.adobe.mobile.Config;
import com.adobe.mobile.Visitor;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.snowplowanalytics.snowplow.tracker.DevicePlatforms;
import com.snowplowanalytics.snowplow.tracker.Emitter;
import com.snowplowanalytics.snowplow.tracker.emitter.RequestCallback;
import com.snowplowanalytics.snowplow.tracker.events.ScreenView;
import com.snowplowanalytics.snowplow.tracker.utils.LogLevel;

import org.ccci.gto.android.common.compat.util.LocaleCompat;
import org.cru.godtools.base.model.Event;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AnalyticsService {
    /* Screen event names */
    public static final String SCREEN_HOME = "Home";
    public static final String SCREEN_ADD_TOOLS = "Add Tools";
    public static final String SCREEN_TOOL_DETAILS = "Tool Info";
    public static final String SCREEN_LANGUAGE_SETTINGS = "Language Settings";
    public static final String SCREEN_LANGUAGE_SELECTION = "Select Language";
    public static final String SCREEN_MENU = "Menu";
    public static final String SCREEN_ABOUT = "About";
    public static final String SCREEN_HELP = "Help";
    public static final String SCREEN_CONTACT_US = "Contact Us";
    public static final String SCREEN_SHARE_GODTOOLS = "Share App";
    public static final String SCREEN_SHARE_STORY = "Share Story";
    public static final String SCREEN_TERMS_OF_USE = "Terms of Use";
    public static final String SCREEN_PRIVACY_POLICY = "Privacy Policy";
    public static final String SCREEN_COPYRIGHT = "Copyright Info";

    /* Adobe analytics key constants */
    private static final String ADOBE_APP_NAME = "cru.appname";
    private static final String ADOBE_LOGGED_IN_STATUS = "cru.loggedinstatus";
    private static final String ADOBE_MARKETING_CLOUD_ID = "cru.mcid";
    private static final String ADOBE_SCREEN_NAME = "cru.screenname";
    private static final String ADOBE_PREVIOUS_SCREEN_NAME = "cru.previousscreenname";

    /* Adobe analytics value constants */
    private static final String ADOBE_NOT_LOGGED_IN = "not logged in";
    private static final String ADOBE_GODTOOLS = "GodTools";

    /* SnowPlow value constants */
    private static final String SNOWPLOW_APP_ID = "GodTools";
    private static final String SNOWPLOW_NAMESPACE = "GodToolsSnowPlowAndroidTracker";
    private static final String SNOWPLOW_ENDPOINT = "snowplow.cru.org";

    /* Custom dimensions */
    private static final int DIMENSION_TOOL = 1;
    private static final int DIMENSION_LANGUAGE = 2;

    /* Legacy constants */
    public static final String SCREEN_EVERYSTUDENT = "EveryStudent";
    public static final String CATEGORY_MENU = "Menu Event";
    public static final String CATEGORY_CONTENT_EVENT = "Content Event";

    private Tracker mTracker = null;
    private com.snowplowanalytics.snowplow.tracker.Tracker mSnowPlowTracker = null;

    /* Adobe Analytics */
    private final Executor mAdobeAnalyticsExecutor = Executors.newSingleThreadExecutor();
    /* SnowPlow */
    private final Executor mSnowPlowExecutor = Executors.newSingleThreadExecutor();

    @Nullable
    private WeakReference<Activity> mActiveActivity;
    private String mPreviousScreenName;

    private AnalyticsService(@NonNull final Context context) {
        mTracker = GoogleAnalytics.getInstance(context).newTracker(BuildConfig.GOOGLE_ANALYTICS_CLIENT_ID);
        Config.setContext(context);
        EventBus.getDefault().register(this);
        initSnowPlowTracker(context);
    }

    @Nullable
    private static AnalyticsService sInstance;
    @NonNull
    public static synchronized AnalyticsService getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new AnalyticsService(context.getApplicationContext());
        }

        return sInstance;
    }

    public void trackScreen(@NonNull final String screen) {
        mTracker.setScreenName(screen);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        trackScreenViewInAdobe(screen);
        trackScreenViewInSnowPlow(screen);
    }

    public void screenView(@NonNull final String name, @NonNull final String language) {
        mTracker.setScreenName(name);
        mTracker.send(new HitBuilders.ScreenViewBuilder()
                .setCustomDimension(DIMENSION_LANGUAGE, language)
                .build());

        trackScreenViewInAdobe(name);
        trackScreenViewInSnowPlow(name);
    }

    @AnyThread
    private void trackScreenViewInAdobe(@NonNull final String screen) {
        final Activity activity = mActiveActivity != null ? mActiveActivity.get() : null;
        if (activity != null) {
            mAdobeAnalyticsExecutor.execute(() -> {
                final Map<String, Object> adobeContextData = adobeContextData(screen);
                Analytics.trackState(screen, adobeContextData);
                Config.collectLifecycleData(activity, adobeContextData);
                mPreviousScreenName = screen;
            });
        }
    }

    @AnyThread
    private void trackScreenViewInSnowPlow(@NonNull final String screen)
    {
        final Activity activity = mActiveActivity != null ? mActiveActivity.get() : null;
        if (activity != null) {
            mSnowPlowExecutor.execute(() -> {
                mSnowPlowTracker.track(ScreenView.builder().name(screen).build());
                mPreviousScreenName = screen;
            });
        }
    }

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

    public void settingChanged(@NonNull final String category, @NonNull final String event) {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(event)
                .setLabel(event)
                .build());

    }

    public void trackEveryStudentSearch(@NonNull final String query) {
        mTracker.setScreenName("everystudent-search");
        mTracker.send(new HitBuilders.EventBuilder()
                              .setCategory("searchbar")
                              .setAction("tap")
                              .setLabel(query)
                              .build());
    }

    public void menuEvent(@NonNull final String item) {

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(CATEGORY_MENU)
                .setAction(item)
                .setLabel(item)
                .build());

    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void trackContentEvent(@NonNull final Event event) {
        final HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                .setCategory(CATEGORY_CONTENT_EVENT)
                .setAction(event.id.namespace + ":" + event.id.name);
        if (event.locale != null) {
            eventBuilder.setCustomDimension(DIMENSION_LANGUAGE, LocaleCompat.toLanguageTag(event.locale));
        }

        mTracker.send(eventBuilder.build());
    }

    public void startAdobeLifecycleTracking(@NonNull final Activity activity) {
        mActiveActivity = new WeakReference<>(activity);
    }

    public void stopAdobeLifecycleTracking() {
        mAdobeAnalyticsExecutor.execute(Config::pauseCollectingLifecycleData);
    }

    private void initSnowPlowTracker(@NonNull final Context context) {
        com.snowplowanalytics.snowplow.tracker.Tracker.close();

        //The Context is used for caching events in a SQLite database in order to avoid losing events to network related issues.
        Emitter emitter = new Emitter.EmitterBuilder(SNOWPLOW_ENDPOINT, context)
                .callback(getCallback())
                .tick(5) // The interval at which the emitter will check for more events. (seconds)
                .build();

        com.snowplowanalytics.snowplow.tracker.Tracker.init(
                new com.snowplowanalytics.snowplow.tracker.Tracker.TrackerBuilder(
                        emitter,
                        SNOWPLOW_NAMESPACE,
                        SNOWPLOW_APP_ID,
                        context)
                        .level(LogLevel.DEBUG)
                        .base64(false)
                        .platform(DevicePlatforms.Mobile)
                        .threadCount(10)
                        .mobileContext(true)
                        .geoLocationContext(false)
                        .applicationCrash(true)
                        .lifecycleEvents(true)
                        .build());

        mSnowPlowTracker = com.snowplowanalytics.snowplow.tracker.Tracker.instance();
    }

    /**
     * Returns the Emitter Request Callback.
     */
    private RequestCallback getCallback() {
        return new RequestCallback() {
            @Override
            public void onSuccess(final int successCount) {
                // Do something useful
            }

            @Override
            public void onFailure(final int successCount, final int failureCount) {
                // Do something useful
            }
        };
    }
}
