package org.cru.godtools.dagger

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import dagger.multibindings.Multibinds
import javax.inject.Singleton
import org.ccci.gto.android.common.dagger.eager.EagerSingleton
import org.ccci.gto.android.common.dagger.eager.EagerSingleton.ThreadMode
import org.ccci.gto.android.common.eventbus.TimberLogger
import org.cru.godtools.AppEventBusIndex
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.meta.SubscriberInfoIndex

@Module
@InstallIn(SingletonComponent::class)
abstract class EventBusModule {
    @Multibinds
    abstract fun eventBusIndexes(): Set<SubscriberInfoIndex>

    // TODO: EventBus doesn't need to be Eager once EventBus is only accessed via Dagger
    @Binds
    @IntoSet
    @EagerSingleton(threadMode = ThreadMode.MAIN)
    abstract fun eagerEventBus(eventBus: EventBus): Any

    companion object {
        @Provides
        @Singleton
        fun eventBus(indexes: Set<@JvmSuppressWildcards SubscriberInfoIndex>): EventBus = EventBus.builder()
            .logger(TimberLogger)
            .apply { indexes.forEach { addIndex(it) } }
            .installDefaultEventBus()

        @IntoSet
        @Provides
        @Reusable
        fun appEventBusIndex(): SubscriberInfoIndex = AppEventBusIndex()
    }
}
