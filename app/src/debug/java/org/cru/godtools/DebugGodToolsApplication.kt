package org.cru.godtools

import android.content.Context
import androidx.multidex.MultiDex
import com.adobe.mobile.Config
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.soloader.SoLoader
import com.facebook.stetho.Stetho
import com.facebook.stetho.inspector.database.DatabaseFilesProvider
import com.facebook.stetho.inspector.database.SqliteDatabaseDriver
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.facebook.stetho.timber.StethoTree
import leakcanary.LeakCanary
import org.ccci.gto.android.common.leakcanary.CrashlyticsOnHeapAnalyzedListener
import org.ccci.gto.android.common.okhttp3.util.addGlobalNetworkInterceptor
import org.ccci.gto.android.common.stetho.db.SQLiteOpenHelperStethoDatabaseProvider
import org.cru.godtools.analytics.TimberAnalyticsService
import org.keynote.godtools.android.db.GodToolsDatabase
import timber.log.Timber
import java.io.File

class DebugGodToolsApplication : GodToolsApplication() {
    internal val db: GodToolsDatabase by lazy { GodToolsDatabase.getInstance(this) }

    override fun onCreate() {
        configLeakCanary()
        initFlipper()
        initStetho()
        super.onCreate()
        initTimber()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    private fun configLeakCanary() {
        LeakCanary.config = LeakCanary.config.copy(
            onHeapAnalyzedListener = CrashlyticsOnHeapAnalyzedListener()
        )
    }

    override fun configureAnalyticsServices() {
        super.configureAnalyticsServices()

        // enable debug logging for various Analytics Services
        Config.setDebugLogging(true)
    }

    private fun initFlipper() {
        if (FlipperUtils.shouldEnableFlipper(this)) {
            SoLoader.init(this, false)
            AndroidFlipperClient.getInstance(this).also {
                it.addPlugin(InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()))
            }.start()
        }
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
        addGlobalNetworkInterceptor(StethoInterceptor())
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
        TimberAnalyticsService.start()
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
