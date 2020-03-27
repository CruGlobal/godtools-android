package org.cru.godtools

import android.os.AsyncTask
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.instantapps.InstantApps
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.android.DaggerApplication
import me.thekey.android.core.TheKeyImpl
import me.thekey.android.eventbus.EventBusEventsManager
import org.ccci.gto.android.common.compat.util.LocaleCompat.toLanguageTag
import org.ccci.gto.android.common.eventbus.TimberLogger
import org.ccci.gto.android.common.firebase.crashlytics.timber.CrashlyticsTree
import org.ccci.gto.android.common.util.LocaleUtils
import org.cru.godtools.account.BuildConfig.ACCOUNT_TYPE
import org.cru.godtools.account.BuildConfig.THEKEY_CLIENTID
import org.cru.godtools.analytics.AnalyticsEventBusIndex
import org.cru.godtools.analytics.adobe.AdobeAnalyticsService
import org.cru.godtools.analytics.appsflyer.AppsFlyerAnalyticsService
import org.cru.godtools.analytics.facebook.FacebookAnalyticsService
import org.cru.godtools.analytics.firebase.FirebaseAnalyticsService
import org.cru.godtools.analytics.snowplow.SnowplowAnalyticsService
import org.cru.godtools.api.GodToolsApi
import org.cru.godtools.article.aem.service.AemArticleManger
import org.cru.godtools.config.BuildConfig.MOBILE_CONTENT_API
import org.cru.godtools.dagger.ApplicationModule
import org.cru.godtools.dagger.DaggerApplicationComponent
import org.cru.godtools.download.manager.DownloadManagerEventBusIndex
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.init.content.task.InitialContentTasks
import org.cru.godtools.model.event.ModelEventEventBusIndex
import org.cru.godtools.service.AccountListRegistrationService
import org.cru.godtools.shortcuts.GodToolsShortcutManager
import org.cru.godtools.shortcuts.ShortcutsEventBusIndex
import org.cru.godtools.tract.TractEventBusIndex
import org.cru.godtools.tract.service.FollowupService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.EventBusBuilder
import timber.log.Timber
import java.util.Locale

open class GodToolsApplication : DaggerApplication() {
    override fun onCreate() {
        super.onCreate()

        // Enable application monitoring
        initializeCrashlytics()

        // configure components
        configureLanguageFallacks()
        configureEventBus(EventBus.builder()).installDefaultEventBus()
        configureTheKey()
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
        AdobeAnalyticsService.getInstance(this)
        AppsFlyerAnalyticsService.getInstance(this)
        FacebookAnalyticsService.getInstance(null)
        FirebaseAnalyticsService.getInstance(this)
        SnowplowAnalyticsService.getInstance(this)
    }

    private fun configureApis() = GodToolsApi.configure(MOBILE_CONTENT_API)

    private fun configureEventBus(builder: EventBusBuilder): EventBusBuilder {
        return builder
            .logger(TimberLogger())
            .addIndex(AnalyticsEventBusIndex())
            .addIndex(AppEventBusIndex())
            .addIndex(DownloadManagerEventBusIndex())
            .addIndex(ModelEventEventBusIndex())
            .addIndex(ShortcutsEventBusIndex())
            .addIndex(TractEventBusIndex())
    }

    private fun configureLanguageFallacks() {
        // These fallbacks are used for JesusFilm
        LocaleUtils.addFallback("abs", "ms")
        LocaleUtils.addFallback("pmy", "ms")
    }

    private fun configureTheKey() = TheKeyImpl.configure(theKeyConfiguration())

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
        AemArticleManger.getInstance(this)
        FollowupService.start(this)
    }

    private fun theKeyConfiguration(): TheKeyImpl.Configuration {
        return TheKeyImpl.Configuration.base()
            .accountType(ACCOUNT_TYPE)
            .clientId(THEKEY_CLIENTID)
            .service(EventBusEventsManager())
    }

    // region Dagger
    override fun applicationInjector() =
        DaggerApplicationComponent.builder().applicationModule(ApplicationModule(this)).build()
    // endregion Dagger
}
