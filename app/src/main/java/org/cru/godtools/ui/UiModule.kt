package org.cru.godtools.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.keynote.godtools.android.activity.MainActivity

@Module
@InstallIn(SingletonComponent::class)
abstract class UiModule {
    @ContributesAndroidInjector
    internal abstract fun mainActivityInjector(): MainActivity
}
