package org.cru.godtools.shortcuts

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import org.ccci.gto.android.common.dagger.eager.EagerSingleton
import org.greenrobot.eventbus.meta.SubscriberInfoIndex

@Module
@InstallIn(SingletonComponent::class)
abstract class ShortcutModule {
    @Binds
    @IntoSet
    @EagerSingleton(threadMode = EagerSingleton.ThreadMode.ASYNC)
    abstract fun shortcutManagerEagerSingleton(dispatcher: GodToolsShortcutManager.Dispatcher): Any

    companion object {
        @IntoSet
        @Provides
        @Reusable
        fun shortcutsEventBusIndex(): SubscriberInfoIndex = ShortcutsEventBusIndex()
    }
}
