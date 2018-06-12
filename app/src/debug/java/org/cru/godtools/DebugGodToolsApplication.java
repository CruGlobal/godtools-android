package org.cru.godtools;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;

import com.adobe.mobile.Config;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.inspector.database.SqliteDatabaseDriver;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.facebook.stetho.timber.StethoTree;
import com.squareup.leakcanary.AndroidExcludedRefs;
import com.squareup.leakcanary.ExcludedRefs;
import com.squareup.leakcanary.LeakCanary;

import org.ccci.gto.android.common.api.okhttp3.util.OkHttpClientUtil;
import org.ccci.gto.android.common.leakcanary.CrashlyticsLeakService;
import org.ccci.gto.android.common.stetho.db.SQLiteOpenHelperStethoDatabaseProvider;
import org.cru.godtools.analytics.AnalyticsDispatcher;
import org.cru.godtools.analytics.TimberAnalyticsService;
import org.keynote.godtools.android.db.GodToolsDatabase;

import timber.log.Timber;

public class DebugGodToolsApplication extends GodToolsApplication {
    @Override
    public void onCreate() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        initLeakCanary();

        initStetho();
        super.onCreate();
        initTimber();
    }

    @Override
    protected void attachBaseContext(@NonNull final Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    protected void configureAnalyticsServices() {
        super.configureAnalyticsServices();

        // enable debug logging for Adobe Analytics
        Config.setDebugLogging(true);
    }

    private void initLeakCanary() {
        final ExcludedRefs excludedRefs = AndroidExcludedRefs.createAppDefaults()
                .staticField("android.view.accessibility.AccessibilityNodeInfo", "sPool").alwaysExclude()
                .reason("AccessibilityNodeInfo.mOriginalText is not cleared when recycling an instance. " +
                                "This can be a Spanned object that holds a reference to a Context object.")
                .build();

        LeakCanary.refWatcher(this)
                .listenerServiceClass(CrashlyticsLeakService.class)
                .excludedRefs(excludedRefs)
                .buildAndInstall();
    }

    private void initStetho() {
        final SqliteDatabaseDriver dbDriver =
                new SQLiteOpenHelperStethoDatabaseProvider(GodToolsDatabase.getInstance(this)).toDatabaseDriver(this);

        final Stetho.InitializerBuilder stethoBuilder = Stetho.newInitializerBuilder(this);
        stethoBuilder.enableDumpapp(Stetho.defaultDumperPluginsProvider(this));
        stethoBuilder.enableWebKitInspector(
                () -> new Stetho.DefaultInspectorModulesBuilder(DebugGodToolsApplication.this)
                        .provideDatabaseDriver(dbDriver)
                        .finish());
        Stetho.initialize(stethoBuilder.build());
        OkHttpClientUtil.addGlobalNetworkInterceptor(new StethoInterceptor());
        Timber.plant(new StethoTree());
    }

    private void initTimber() {
        // plant debug output trees we want
        Timber.plant(new Timber.DebugTree());

        // add TimberAnalyticsService
        AnalyticsDispatcher.getInstance(this).addAnalyticsService(TimberAnalyticsService.getInstance());
    }
}
