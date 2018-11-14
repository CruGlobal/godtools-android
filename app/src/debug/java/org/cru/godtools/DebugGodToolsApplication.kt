package org.cru.godtools

import android.content.Context
import androidx.multidex.MultiDex
import com.adobe.mobile.Config
import com.facebook.stetho.Stetho
import com.facebook.stetho.inspector.database.DatabaseFilesProvider
import com.facebook.stetho.inspector.database.SqliteDatabaseDriver
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.facebook.stetho.timber.StethoTree
import com.squareup.leakcanary.LeakCanary
import org.ccci.gto.android.common.leakcanary.CrashlyticsLeakService
import org.ccci.gto.android.common.okhttp3.util.OkHttpClientUtil
import org.ccci.gto.android.common.stetho.db.SQLiteOpenHelperStethoDatabaseProvider
import org.cru.godtools.analytics.AnalyticsDispatcher
import org.cru.godtools.analytics.TimberAnalyticsService
import org.keynote.godtools.android.db.GodToolsDatabase
import timber.log.Timber
import java.io.File

class DebugGodToolsApplication : GodToolsApplication() {
    internal val db: GodToolsDatabase by lazy { GodToolsDatabase.getInstance(this) }

    override fun onCreate() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        initLeakCanary()

        initStetho()
        super.onCreate()
        initTimber()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun configureAnalyticsServices() {
        super.configureAnalyticsServices()

        // enable debug logging for Adobe Analytics
        Config.setDebugLogging(true)
    }

    private fun initLeakCanary() {
        LeakCanary.refWatcher(this)
            .listenerServiceClass(CrashlyticsLeakService::class.java)
            .buildAndInstall()
    }

    private fun initStetho() {
        Stetho.newInitializerBuilder(this)
            .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
            .enableWebKitInspector {
                Stetho.DefaultInspectorModulesBuilder(this)
                    .provideDatabaseDriver(SQLiteOpenHelperStethoDatabaseProvider(db).toDatabaseDriver(this))
                    .provideDatabaseDriver(SqliteDatabaseDriver(this, GtDatabaseFilesProvider()))
                    .finish()
            }.run { Stetho.initialize(build()) }

        Timber.plant(StethoTree())
        OkHttpClientUtil.addGlobalNetworkInterceptor(StethoInterceptor())
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
        AnalyticsDispatcher.getInstance(this).addAnalyticsService(TimberAnalyticsService.getInstance())
    }

    internal inner class GtDatabaseFilesProvider : DatabaseFilesProvider {
        override fun getDatabaseFiles(): List<File> {
            return databaseList().asSequence()
                .filterNot { it.startsWith(db.databaseName) }
                .map { getDatabasePath(it) }
                .toList()
        }
    }
}
