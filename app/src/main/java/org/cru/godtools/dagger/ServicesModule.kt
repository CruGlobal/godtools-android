package org.cru.godtools.dagger

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import dagger.multibindings.IntoSet
import me.thekey.android.TheKey
import me.thekey.android.core.TheKeyImpl
import me.thekey.android.eventbus.EventBusEventsManager
import org.ccci.gto.android.common.dagger.eager.EagerSingleton
import org.ccci.gto.android.common.dagger.eager.EagerSingleton.ThreadMode
import org.ccci.gto.android.common.eventbus.TimberLogger
import org.cru.godtools.AppEventBusIndex
import org.cru.godtools.account.BuildConfig
import org.cru.godtools.analytics.AnalyticsEventBusIndex
import org.cru.godtools.download.manager.DownloadManagerEventBusIndex
import org.cru.godtools.model.event.ModelEventEventBusIndex
import org.cru.godtools.shortcuts.ShortcutsEventBusIndex
import org.cru.godtools.tract.TractEventBusIndex
import org.greenrobot.eventbus.EventBus
import javax.inject.Singleton

@Module
abstract class ServicesModule {
    // TODO: TheKey doesn't need to be Eager once TheKey is only accessed via Dagger
    @Binds
    @IntoSet
    @EagerSingleton(ThreadMode.MAIN)
    abstract fun eagerTheKey(theKey: TheKey): Any

    companion object {
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

        @Provides
        @Singleton
        fun theKey(context: Context, eventBus: EventBus): TheKey {
            TheKeyImpl.configure(
                TheKeyImpl.Configuration.base()
                    .accountType(BuildConfig.ACCOUNT_TYPE)
                    .clientId(BuildConfig.THEKEY_CLIENTID)
                    .service(EventBusEventsManager(eventBus))
            )
            return TheKey.getInstance(context)
        }

        // TODO: EventBus doesn't need to be Eager once EventBus is only accessed via Dagger
        @Provides
        @ElementsIntoSet
        @EagerSingleton(ThreadMode.MAIN)
        internal fun mainEagerSingletons(eventBus: EventBus) = setOf(eventBus)
    }
}
