package org.cru.godtools

import leakcanary.LeakCanary
import org.ccci.gto.android.common.leakcanary.crashlytics.CrashlyticsEventListener
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
        LeakCanary.config = LeakCanary.config.run {
            copy(eventListeners = eventListeners + CrashlyticsEventListener)
        }
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
    }
}
