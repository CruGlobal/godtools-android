package org.cru.godtools;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.inspector.database.SqliteDatabaseDriver;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.squareup.leakcanary.AndroidExcludedRefs;
import com.squareup.leakcanary.LeakCanary;

import org.ccci.gto.android.common.api.okhttp3.util.OkHttpClientUtil;
import org.ccci.gto.android.common.leakcanary.CrashlyticsLeakService;
import org.ccci.gto.android.common.stetho.db.SQLiteOpenHelperStethoDatabaseProvider;
import org.keynote.godtools.android.db.GodToolsDatabase;

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
    }

    @Override
    protected void attachBaseContext(@NonNull final Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void initLeakCanary() {
        LeakCanary.refWatcher(this)
                .listenerServiceClass(CrashlyticsLeakService.class)
                .excludedRefs(AndroidExcludedRefs.createAppDefaults().build())
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
    }
}
