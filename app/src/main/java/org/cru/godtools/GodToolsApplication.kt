package org.cru.godtools

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.instantapps.InstantApps
import com.google.android.play.core.missingsplits.MissingSplitsManagerFactory
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale
import javax.inject.Inject
import org.ccci.gto.android.common.dagger.eager.EagerSingletonInitializer
import org.ccci.gto.android.common.firebase.crashlytics.timber.CrashlyticsTree
import org.ccci.gto.android.common.util.LocaleUtils
import timber.log.Timber

@HiltAndroidApp
open class GodToolsApplication : Application() {
    @Inject
    internal lateinit var eagerInitializer: EagerSingletonInitializer

    override fun onCreate() {
        // Enable application monitoring
        initializeCrashlytics()

        // TODO: remove this logic once the minimum Android version is Android 10 or higher.
        //       Also ensure we are no longer seeing missing splits errors showing up in Crashlytics.
        if (MissingSplitsManagerFactory.create(this).disableAppIfMissingRequiredSplits()) {
            Timber.tag("GodToolsApplication")
                .e(IllegalStateException("Missing Splits"), "App is missing splits, it was probably sideloaded.")
            return
        }

        // configure components
        configureLanguageFallbacks()

        super.onCreate()

        // enable compat vector images
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
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
}
