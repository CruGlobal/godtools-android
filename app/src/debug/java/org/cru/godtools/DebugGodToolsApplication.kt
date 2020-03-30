package org.cru.godtools

import android.content.Context
import androidx.multidex.MultiDex
import com.adobe.mobile.Config
import leakcanary.LeakCanary
import org.ccci.gto.android.common.leakcanary.crashlytics.CrashlyticsOnHeapAnalyzedListener
import org.ccci.gto.android.common.leakcanary.timber.TimberSharkLog
import org.cru.godtools.analytics.TimberAnalyticsService
import org.cru.godtools.dagger.ApplicationModule
import org.cru.godtools.dagger.DaggerDebugApplicationComponent
import shark.SharkLog
import timber.log.Timber

class DebugGodToolsApplication : GodToolsApplication() {
    override fun applicationInjector() =
        DaggerDebugApplicationComponent.builder().applicationModule(ApplicationModule(this)).build()

    override fun onCreate() {
        configLeakCanary()
        initTimber()
        super.onCreate()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    private fun configLeakCanary() {
        SharkLog.logger = TimberSharkLog
        LeakCanary.config = LeakCanary.config.copy(
            onHeapAnalyzedListener = CrashlyticsOnHeapAnalyzedListener
        )
    }

    override fun configureAnalyticsServices() {
        super.configureAnalyticsServices()

        // enable debug logging for various Analytics Services
        Config.setDebugLogging(true)
        TimberAnalyticsService.getInstance(null)
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
    }
}
