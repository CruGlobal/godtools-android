package org.cru.godtools

import android.os.AsyncTask
import me.thekey.android.core.TheKeyImpl
import me.thekey.android.eventbus.EventBusEventsManager
import org.cru.godtools.account.BuildConfig.ACCOUNT_TYPE
import org.cru.godtools.account.BuildConfig.THEKEY_CLIENTID
import org.cru.godtools.api.GodToolsApi
import org.cru.godtools.article.aem.service.AemArticleManger
import org.cru.godtools.base.app.BaseGodToolsApplication
import org.cru.godtools.config.BuildConfig.MOBILE_CONTENT_API
import org.cru.godtools.download.manager.DownloadManagerEventBusIndex
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.init.content.task.InitialContentTasks
import org.cru.godtools.model.event.ModelEventEventBusIndex
import org.cru.godtools.model.loader.ModelLoaderEventBusIndex
import org.cru.godtools.service.AccountListRegistrationService
import org.cru.godtools.shortcuts.GodToolsShortcutManager
import org.cru.godtools.shortcuts.ShortcutsEventBusIndex
import org.cru.godtools.tract.TractEventBusIndex
import org.cru.godtools.tract.service.FollowupService
import org.greenrobot.eventbus.EventBusBuilder

open class GodToolsApplication : BaseGodToolsApplication() {
    override fun onCreate() {
        super.onCreate()

        // install any missing initial content
        AsyncTask.THREAD_POOL_EXECUTOR.execute(InitialContentTasks(this))
    }

    override fun configureApis() = GodToolsApi.configure(MOBILE_CONTENT_API)

    override fun configureEventBus(builder: EventBusBuilder): EventBusBuilder {
        return super.configureEventBus(builder)
            .addIndex(AppEventBusIndex())
            .addIndex(DownloadManagerEventBusIndex())
            .addIndex(ModelEventEventBusIndex())
            .addIndex(ModelLoaderEventBusIndex())
            .addIndex(ShortcutsEventBusIndex())
            .addIndex(TractEventBusIndex())
    }

    override fun configureTheKey() = TheKeyImpl.configure(theKeyConfiguration())

    override fun startServices() {
        super.startServices()
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
}
