package org.cru.godtools

import leakcanary.LeakCanary
import org.ccci.gto.android.common.leakcanary.crashlytics.CrashlyticsOnHeapAnalyzedListener
import org.ccci.gto.android.common.leakcanary.timber.TimberSharkLog
import shark.SharkLog
import timber.log.Timber

class DebugGodToolsApplication : GodToolsApplication() {
    override fun onCreate() {
        configLeakCanary()
        initTimber()
        super.onCreate()
    }

    private fun configLeakCanary() {
        SharkLog.logger = TimberSharkLog
        LeakCanary.config = LeakCanary.config.copy(
            onHeapAnalyzedListener = CrashlyticsOnHeapAnalyzedListener
        )
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
    }
}
