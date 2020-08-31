package org.cru.godtools

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.instantapps.InstantApps
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale
import javax.inject.Inject
import org.ccci.gto.android.common.compat.util.LocaleCompat.toLanguageTag
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

        // configure components
        configureLanguageFallacks()

        super.onCreate()

        // enable compat vector images
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    private fun configureLanguageFallacks() {
        // These fallbacks are used for JesusFilm
        LocaleUtils.addFallback("abs", "ms")
        LocaleUtils.addFallback("pmy", "ms")
    }

    private fun initializeCrashlytics() {
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCustomKey("InstantApp", InstantApps.isInstantApp(this))
        crashlytics.setCustomKey("SystemLanguageRaw", Locale.getDefault().toString())
        crashlytics.setCustomKey("SystemLanguage", toLanguageTag(Locale.getDefault()))
        Timber.plant(CrashlyticsTree())
    }
}
