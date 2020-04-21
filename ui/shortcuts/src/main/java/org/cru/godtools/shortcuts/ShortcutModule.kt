package org.cru.godtools.shortcuts

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoSet
import org.ccci.gto.android.common.dagger.eager.EagerSingleton
import org.greenrobot.eventbus.meta.SubscriberInfoIndex

@Module
abstract class ShortcutModule {
    @Binds
    @IntoSet
    @EagerSingleton(threadMode = EagerSingleton.ThreadMode.ASYNC)
    abstract fun shortcutManagerEagerSingleton(shortcutManager: GodToolsShortcutManager): Any

    @ContributesAndroidInjector
    internal abstract fun localeUpdateBroadcastReceiverInjector(): LocaleUpdateBroadcastReceiver

    companion object {
        @Provides
        fun shortcutManager(context: Context) = GodToolsShortcutManager.getInstance(context)

        @IntoSet
        @Provides
        @Reusable
        fun shortcutsEventBusIndex(): SubscriberInfoIndex = ShortcutsEventBusIndex()
    }
}
