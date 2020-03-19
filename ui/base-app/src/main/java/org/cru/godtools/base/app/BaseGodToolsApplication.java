package org.cru.godtools.base.app;

import android.app.Application;

import com.google.android.instantapps.InstantApps;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.ccci.gto.android.common.compat.util.LocaleCompat;
import org.ccci.gto.android.common.eventbus.TimberLogger;
import org.ccci.gto.android.common.firebase.crashlytics.timber.CrashlyticsTree;
import org.ccci.gto.android.common.util.LocaleUtils;
import org.cru.godtools.analytics.AnalyticsEventBusIndex;
import org.cru.godtools.analytics.adobe.AdobeAnalyticsService;
import org.cru.godtools.analytics.appsflyer.AppsFlyerAnalyticsService;
import org.cru.godtools.analytics.facebook.FacebookAnalyticsService;
import org.cru.godtools.analytics.firebase.FirebaseAnalyticsService;
import org.cru.godtools.analytics.snowplow.SnowplowAnalyticsService;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusBuilder;

import java.util.Locale;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import timber.log.Timber;

public class BaseGodToolsApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Enable application monitoring
        initializeCrashlytics();

        // configure components
        configureLanguageFallacks();
        configureEventBus(EventBus.builder()).installDefaultEventBus();
        configureTheKey();
        configureAnalyticsServices();
        configureApis();

        // start various services
        startServices();

        // enable compat vector images
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private void initializeCrashlytics() {
        final FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.setCustomKey("InstantApp", InstantApps.isInstantApp(this));
        crashlytics.setCustomKey("SystemLanguageRaw", Locale.getDefault().toString());
        crashlytics.setCustomKey("SystemLanguage", LocaleCompat.toLanguageTag(Locale.getDefault()));

        Timber.plant(new CrashlyticsTree());
    }

    @CallSuper
    protected void configureAnalyticsServices() {
        AdobeAnalyticsService.Companion.getInstance(this);
        AppsFlyerAnalyticsService.Companion.getInstance(this);
        FacebookAnalyticsService.Companion.getInstance(null);
        FirebaseAnalyticsService.Companion.getInstance(this);
        SnowplowAnalyticsService.Companion.getInstance(this);
    }

    protected void configureApis() {}

    @NonNull
    @CallSuper
    protected EventBusBuilder configureEventBus(@NonNull final EventBusBuilder builder) {
        return builder
                .logger(new TimberLogger())
                .addIndex(new AnalyticsEventBusIndex());
    }

    private void configureLanguageFallacks() {
        // These fallbacks are used for JesusFilm
        LocaleUtils.addFallback("abs", "ms");
        LocaleUtils.addFallback("pmy", "ms");
    }

    protected void configureTheKey() {}

    @CallSuper
    protected void startServices() {}
}
