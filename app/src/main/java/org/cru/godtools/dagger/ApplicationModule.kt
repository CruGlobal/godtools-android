package org.cru.godtools.dagger

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import org.ccci.gto.android.common.dagger.eager.EagerModule
import org.ccci.gto.android.common.dagger.eager.EagerSingleton
import org.ccci.gto.android.common.eventbus.TimberLogger
import org.cru.godtools.AppEventBusIndex
import org.cru.godtools.analytics.AnalyticsEventBusIndex
import org.cru.godtools.download.manager.DownloadManagerEventBusIndex
import org.cru.godtools.model.event.ModelEventEventBusIndex
import org.cru.godtools.shortcuts.ShortcutsEventBusIndex
import org.cru.godtools.tract.TractEventBusIndex
import org.greenrobot.eventbus.EventBus
import javax.inject.Singleton

@Module(includes = [EagerModule::class])
class ApplicationModule(private val app: Application) {
    @Provides
    fun context(): Context = app

    @Provides
    @Singleton
    fun eventBus(): EventBus = EventBus.builder()
        .logger(TimberLogger())
        .addIndex(AnalyticsEventBusIndex())
        .addIndex(AppEventBusIndex())
        .addIndex(DownloadManagerEventBusIndex())
        .addIndex(ModelEventEventBusIndex())
        .addIndex(ShortcutsEventBusIndex())
        .addIndex(TractEventBusIndex())
        .installDefaultEventBus()

    companion object {
        // TODO: EventBus doesn't need to be Eager once EventBus is only accessed via Dagger
        @Provides
        @ElementsIntoSet
        @EagerSingleton(EagerSingleton.ThreadMode.MAIN)
        internal fun mainEagerSingletons(eventBus: EventBus) = setOf<Any>(eventBus)
    }
}
