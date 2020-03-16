package org.cru.godtools

import android.content.Context
import androidx.multidex.MultiDex
import com.adobe.mobile.Config
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.databases.impl.SqliteDatabaseDriver
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.facebook.soloader.SoLoader
import leakcanary.LeakCanary
import org.ccci.gto.android.common.facebook.flipper.plugins.databases.DefaultSqliteDatabaseProvider
import org.ccci.gto.android.common.facebook.flipper.plugins.databases.SQLiteOpenHelperDatabaseConnectionProvider
import org.ccci.gto.android.common.leakcanary.CrashlyticsOnHeapAnalyzedListener
import org.ccci.gto.android.common.okhttp3.util.addGlobalNetworkInterceptor
import org.cru.godtools.analytics.TimberAnalyticsService
import org.keynote.godtools.android.db.GodToolsDatabase
import timber.log.Timber

class DebugGodToolsApplication : GodToolsApplication() {
    internal val db: GodToolsDatabase by lazy { GodToolsDatabase.getInstance(this) }

    override fun onCreate() {
        configLeakCanary()
        initTimber()
        super.onCreate()
        initFlipper()
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
        TimberAnalyticsService.getInstance(null)
    }

    private fun initFlipper() {
        if (FlipperUtils.shouldEnableFlipper(this)) {
            SoLoader.init(this, false)
            AndroidFlipperClient.getInstance(this).apply {
                val context = this@DebugGodToolsApplication
                addPlugin(
                    DatabasesFlipperPlugin(
                        SqliteDatabaseDriver(
                            context,
                            DefaultSqliteDatabaseProvider(context),
                            SQLiteOpenHelperDatabaseConnectionProvider(context, dbs = *arrayOf(db))
                        )
                    )
                )
                addPlugin(InspectorFlipperPlugin(context, DescriptorMapping.withDefaults()))
                addPlugin(SharedPreferencesFlipperPlugin(context))

                val networkPlugin = NetworkFlipperPlugin()
                addPlugin(networkPlugin)
                addGlobalNetworkInterceptor(FlipperOkhttpInterceptor(networkPlugin))
            }.start()
        }
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
    }
}
