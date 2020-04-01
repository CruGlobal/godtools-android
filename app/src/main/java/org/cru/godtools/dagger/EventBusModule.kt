package org.cru.godtools.dagger

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.multibindings.IntoSet
import dagger.multibindings.Multibinds
import org.ccci.gto.android.common.dagger.eager.EagerSingleton
import org.ccci.gto.android.common.dagger.eager.EagerSingleton.ThreadMode
import org.ccci.gto.android.common.eventbus.TimberLogger
import org.cru.godtools.AppEventBusIndex
import org.cru.godtools.download.manager.DownloadManagerEventBusIndex
import org.cru.godtools.model.event.ModelEventEventBusIndex
import org.cru.godtools.shortcuts.ShortcutsEventBusIndex
import org.cru.godtools.tract.TractEventBusIndex
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.meta.SubscriberInfoIndex
import javax.inject.Singleton

@Module
abstract class EventBusModule {
    @Multibinds
    abstract fun eventBusIndexes(): Set<SubscriberInfoIndex>

    // TODO: EventBus doesn't need to be Eager once EventBus is only accessed via Dagger
    @Binds
    @IntoSet
    @EagerSingleton(ThreadMode.MAIN)
    abstract fun eagerEventBus(eventBus: EventBus): Any

    companion object {
        @Provides
        @Singleton
        fun eventBus(indexes: Set<@JvmSuppressWildcards SubscriberInfoIndex>): EventBus = EventBus.builder()
            .logger(TimberLogger())
            .apply { indexes.forEach { addIndex(it) } }
            .addIndex(DownloadManagerEventBusIndex())
            .addIndex(ModelEventEventBusIndex())
            .addIndex(ShortcutsEventBusIndex())
            .addIndex(TractEventBusIndex())
            .installDefaultEventBus()

        @IntoSet
        @Provides
        @Reusable
        fun appEventBusIndex(): SubscriberInfoIndex = AppEventBusIndex()
    }
}
