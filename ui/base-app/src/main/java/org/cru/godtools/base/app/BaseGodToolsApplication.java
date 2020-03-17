package org.cru.godtools.base.app;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.google.android.instantapps.InstantApps;

import org.ccci.gto.android.common.compat.util.LocaleCompat;
import org.ccci.gto.android.common.crashlytics.timber.CrashlyticsTree;
import org.ccci.gto.android.common.eventbus.TimberLogger;
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
import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.SilentLogger;
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
        Fabric.with(new Fabric.Builder(this)
                            .logger(new SilentLogger())
                            .kits(new Crashlytics())
                            .build());
        Crashlytics.setBool("InstantApp", InstantApps.isInstantApp(this));
        Crashlytics.setString("SystemLanguageRaw", Locale.getDefault().toString());
        Crashlytics.setString("SystemLanguage", LocaleCompat.toLanguageTag(Locale.getDefault()));

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
