package org.cru.godtools.base.app;

import android.app.Application;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.google.android.instantapps.InstantApps;
import com.newrelic.agent.android.NewRelic;

import org.ccci.gto.android.common.compat.util.LocaleCompat;
import org.ccci.gto.android.common.crashlytics.timber.CrashlyticsTree;
import org.ccci.gto.android.common.util.LocaleUtils;
import org.cru.godtools.analytics.AdobeAnalyticsService;
import org.cru.godtools.analytics.AnalyticsDispatcher;
import org.cru.godtools.analytics.AnalyticsEventBusIndex;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusBuilder;

import java.util.Locale;

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
        NewRelic.withApplicationToken(NEW_RELIC_API_KEY).start(this);

        // configure eventbus
        configureEventBus(EventBus.builder()).installDefaultEventBus();

        // configure analytics
        configureAnalyticsServices();

        // configure some language fallbacks
        configureLanguageFallacks();
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

    protected void configureAnalyticsServices() {
        AdobeAnalyticsService.getInstance(this);
        AnalyticsDispatcher.getInstance(this);
    }

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
}
