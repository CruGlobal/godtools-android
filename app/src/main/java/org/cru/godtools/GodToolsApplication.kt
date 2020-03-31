package org.cru.godtools

import android.os.AsyncTask
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.instantapps.InstantApps
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.android.DaggerApplication
import org.ccci.gto.android.common.compat.util.LocaleCompat.toLanguageTag
import org.ccci.gto.android.common.dagger.eager.EagerSingletonInitializer
import org.ccci.gto.android.common.firebase.crashlytics.timber.CrashlyticsTree
import org.ccci.gto.android.common.util.LocaleUtils
import org.cru.godtools.analytics.appsflyer.AppsFlyerAnalyticsService
import org.cru.godtools.analytics.firebase.FirebaseAnalyticsService
import org.cru.godtools.analytics.snowplow.SnowplowAnalyticsService
import org.cru.godtools.api.GodToolsApi
import org.cru.godtools.article.aem.service.AemArticleManager
import org.cru.godtools.config.BuildConfig.MOBILE_CONTENT_API
import org.cru.godtools.dagger.ApplicationModule
import org.cru.godtools.dagger.DaggerApplicationComponent
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.init.content.task.InitialContentTasks
import org.cru.godtools.service.AccountListRegistrationService
import org.cru.godtools.shortcuts.GodToolsShortcutManager
import org.cru.godtools.tract.service.FollowupService
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

open class GodToolsApplication : DaggerApplication() {
    override fun onCreate() {
        // Enable application monitoring
        initializeCrashlytics()

        super.onCreate()

        // configure components
        configureLanguageFallacks()
        configureAnalyticsServices()
        configureApis()

        // start various services
        startServices()

        // enable compat vector images
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        // install any missing initial content
        AsyncTask.THREAD_POOL_EXECUTOR.execute(InitialContentTasks(this))
    }

    @CallSuper
    protected open fun configureAnalyticsServices() {
        AppsFlyerAnalyticsService.getInstance(this)
        FirebaseAnalyticsService.getInstance(this)
        SnowplowAnalyticsService.getInstance(this)
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

    private fun startServices() {
        GodToolsDownloadManager.getInstance(this)
        GodToolsShortcutManager.getInstance(this)
        AccountListRegistrationService.getInstance(this)
        AemArticleManager.getInstance(this)
        FollowupService.getInstance(this)
    }

    // region Dagger
    override fun applicationInjector() =
        DaggerApplicationComponent.builder().applicationModule(ApplicationModule(this)).build()

    @Inject
    internal lateinit var eagerInitializer: EagerSingletonInitializer
    // endregion Dagger
}
