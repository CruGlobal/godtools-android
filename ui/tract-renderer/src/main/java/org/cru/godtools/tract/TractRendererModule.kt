package org.cru.godtools.tract

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.cru.godtools.tract.activity.ModalActivity
import org.cru.godtools.tract.activity.TractActivity

@Module
abstract class TractRendererModule {
    @ContributesAndroidInjector
    internal abstract fun tractActivityInjector(): TractActivity

    @ContributesAndroidInjector
    internal abstract fun modalActivityInjector(): ModalActivity
}
