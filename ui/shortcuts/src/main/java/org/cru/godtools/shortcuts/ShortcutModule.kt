package org.cru.godtools.shortcuts

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ShortcutModule {
    @ContributesAndroidInjector
    internal abstract fun localeUpdateBroadcastReceiverInjector(): LocaleUpdateBroadcastReceiver
}
