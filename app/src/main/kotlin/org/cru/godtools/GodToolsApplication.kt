package org.cru.godtools

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.android.instantapps.InstantApps
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.work.TimberLogger
import org.ccci.gto.android.common.dagger.eager.EagerSingletonInitializer
import org.ccci.gto.android.common.firebase.crashlytics.timber.CrashlyticsTree
import org.ccci.gto.android.common.util.LocaleUtils
import timber.log.Timber

@HiltAndroidApp
open class GodToolsApplication : Application(), Configuration.Provider {
    @Inject
    internal lateinit var eagerInitializer: EagerSingletonInitializer

    override fun onCreate() {
        // Enable application monitoring
        initializeCrashlytics()

        // configure components
        configureLanguageFallbacks()

        super.onCreate()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        SplitCompat.install(this)
    }

    private fun configureLanguageFallbacks() {
        // These fallbacks are used for JesusFilm
        LocaleUtils.addFallback("abs", "ms")
        LocaleUtils.addFallback("pmy", "ms")
    }

    private fun initializeCrashlytics() {
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("InstantApp", InstantApps.isInstantApp(this@GodToolsApplication))
            setCustomKey("SystemLanguageRaw", Locale.getDefault().toString())
            setCustomKey("SystemLanguage", Locale.getDefault().toLanguageTag())
        }
        Timber.plant(CrashlyticsTree())
    }

    // region WorkManager Configuration.Provider
    init {
        TimberLogger(Log.ERROR).install()
    }

    @Inject
    internal lateinit var workerFactory: Lazy<HiltWorkerFactory>
    override val workManagerConfiguration by lazy {
        Configuration.Builder().setWorkerFactory(workerFactory.get()).build()
    }
    // endregion WorkManager Configuration.Provider
}
