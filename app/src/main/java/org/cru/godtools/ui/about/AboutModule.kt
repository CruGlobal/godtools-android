package org.cru.godtools.ui.about

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AboutModule {
    @ContributesAndroidInjector
    internal abstract fun aboutActivityInjector(): AboutActivity
}
