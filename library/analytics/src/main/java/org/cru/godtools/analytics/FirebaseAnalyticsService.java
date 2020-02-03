package org.cru.godtools.analytics;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.common.wrappers.InstantApps;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.cru.godtools.analytics.model.AnalyticsScreenEvent;
import org.cru.godtools.analytics.model.AnalyticsSystem;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.thekey.android.TheKey;
import me.thekey.android.eventbus.event.TheKeyEvent;

public class FirebaseAnalyticsService implements Application.ActivityLifecycleCallbacks {
    private static final String USER_PROP_APP_TYPE = "godtools_app_type";
    private static final String VALUE_APP_TYPE_INSTANT = "instant";
    private static final String VALUE_APP_TYPE_INSTALLED = "installed";

    private final FirebaseAnalytics mFirebaseAnalytics;
    private final TheKey mTheKey;

    @Nullable
    private Reference<Activity> mCurrentActivity;

    @MainThread
    private FirebaseAnalyticsService(@NonNull final Context context) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        mTheKey = TheKey.getInstance(context);
        initFirebase(context);

        ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(this);
        EventBus.getDefault().register(this);
    }

    @Nullable
    private static FirebaseAnalyticsService sInstance;

    @NonNull
    public static synchronized FirebaseAnalyticsService getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new FirebaseAnalyticsService(context.getApplicationContext());
        }

        return sInstance;
    }

    // region Tracking Events

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onAnalyticsEvent(@NonNull final AnalyticsScreenEvent event) {
        if (event.isForSystem(AnalyticsSystem.FIREBASE)) {
            handleScreenEvent(event);
        }
    }

    @AnyThread
    @Subscribe
    public void onTheKeyEvent(@NonNull final TheKeyEvent event) {
        updateUser();
    }

    // region ActivityLifecycleCallbacks

    @Override
    public void onActivityCreated(final Activity activity, final Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(final Activity activity) {}

    @Override
    public void onActivityResumed(final Activity activity) {
        mCurrentActivity = new WeakReference<>(activity);
    }

    @Override
    public void onActivityPaused(final Activity activity) {
        if (mCurrentActivity != null && mCurrentActivity.get() == activity) {
            mCurrentActivity = null;
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

    private void initFirebase(@NonNull final Context context) {
        updateUser();
        mFirebaseAnalytics.setUserProperty(USER_PROP_APP_TYPE,
                                           InstantApps.isInstantApp(context) ? VALUE_APP_TYPE_INSTANT :
                                                   VALUE_APP_TYPE_INSTALLED);
    }

    @AnyThread
    private void updateUser() {
        mFirebaseAnalytics.setUserId(mTheKey.getDefaultSessionGuid());
    }

    @MainThread
    private void handleScreenEvent(@NonNull final AnalyticsScreenEvent event) {
        final Activity activity = mCurrentActivity != null ? mCurrentActivity.get() : null;
        if (activity != null) {
            mFirebaseAnalytics.setCurrentScreen(activity, event.getScreen(), null);
        }
    }
}
