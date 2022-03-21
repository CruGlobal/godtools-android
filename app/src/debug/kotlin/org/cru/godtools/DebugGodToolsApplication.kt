package org.cru.godtools

import com.facebook.flipper.plugins.leakcanary2.FlipperLeakListener
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
            copy(
                eventListeners = eventListeners + CrashlyticsEventListener,
                onHeapAnalyzedListener = FlipperLeakListener()
            )
        }
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
    }
}
