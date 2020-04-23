package org.cru.godtools

import androidx.appcompat.app.AppCompatDelegate
import com.google.android.instantapps.InstantApps
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.android.DaggerApplication
import org.ccci.gto.android.common.compat.util.LocaleCompat.toLanguageTag
import org.ccci.gto.android.common.dagger.eager.EagerSingletonInitializer
import org.ccci.gto.android.common.firebase.crashlytics.timber.CrashlyticsTree
import org.ccci.gto.android.common.util.LocaleUtils
import org.cru.godtools.api.GodToolsApi
import org.cru.godtools.config.BuildConfig.MOBILE_CONTENT_API
import org.cru.godtools.dagger.ApplicationModule
import org.cru.godtools.dagger.DaggerApplicationComponent
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

open class GodToolsApplication : DaggerApplication() {
    override fun onCreate() {
        // Enable application monitoring
        initializeCrashlytics()

        // configure components
        configureLanguageFallacks()
        configureApis()

        super.onCreate()

        // enable compat vector images
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    private fun configureApis() = GodToolsApi.configure(MOBILE_CONTENT_API)

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

    // region Dagger
    override fun applicationInjector() =
        DaggerApplicationComponent.builder().applicationModule(ApplicationModule(this)).build()

    @Inject
    internal lateinit var eagerInitializer: EagerSingletonInitializer
    // endregion Dagger
}
