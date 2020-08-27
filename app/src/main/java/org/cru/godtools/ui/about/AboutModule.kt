package org.cru.godtools.ui.about

import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AboutModule {
    @ContributesAndroidInjector
    internal abstract fun aboutActivityInjector(): AboutActivity
}
