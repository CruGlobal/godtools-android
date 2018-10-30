package org.cru.godtools.base.app;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.google.android.instantapps.InstantApps;
import com.newrelic.agent.android.NewRelic;

import org.ccci.gto.android.common.compat.util.LocaleCompat;
import org.ccci.gto.android.common.crashlytics.timber.CrashlyticsTree;
import org.ccci.gto.android.common.util.LocaleUtils;
import org.cru.godtools.analytics.AdobeAnalyticsService;
import org.cru.godtools.analytics.AnalyticsDispatcher;
import org.cru.godtools.analytics.AnalyticsEventBusIndex;
import org.cru.godtools.analytics.FirebaseAnalyticsService;
import org.cru.godtools.analytics.SnowplowAnalyticsService;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusBuilder;

import java.util.Locale;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.SilentLogger;
import timber.log.Timber;

import static org.cru.godtools.base.app.BuildConfig.NEW_RELIC_API_KEY;

public class BaseGodToolsApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Enable application monitoring
        initializeCrashlytics();
        initializeNewRelic();

        // configure components
        configureLanguageFallacks();
        configureEventBus(EventBus.builder()).installDefaultEventBus();
        configureTheKey();
        configureAnalyticsServices();
        configureApis();

        // start various services
        startServices();
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

    private void initializeNewRelic() {
        NewRelic.withApplicationToken(NEW_RELIC_API_KEY)
                .withCrashReportingEnabled(false)
                .start(this);
    }

    @CallSuper
    protected void configureAnalyticsServices() {
        AdobeAnalyticsService.getInstance(this);
        FirebaseAnalyticsService.getInstance(this);
        SnowplowAnalyticsService.getInstance(this);
        AnalyticsDispatcher.getInstance(this);
    }

    protected void configureApis() {}

    @NonNull
    @CallSuper
    protected EventBusBuilder configureEventBus(@NonNull final EventBusBuilder builder) {
        return builder
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
