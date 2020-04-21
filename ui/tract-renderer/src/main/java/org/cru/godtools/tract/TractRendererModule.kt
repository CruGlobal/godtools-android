package org.cru.godtools.tract

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoSet
import org.cru.godtools.tract.activity.ModalActivity
import org.cru.godtools.tract.activity.TractActivity
import org.greenrobot.eventbus.meta.SubscriberInfoIndex

@Module
abstract class TractRendererModule {
    @ContributesAndroidInjector
    internal abstract fun tractActivityInjector(): TractActivity

    @ContributesAndroidInjector
    internal abstract fun modalActivityInjector(): ModalActivity

    companion object {
        @IntoSet
        @Provides
        @Reusable
        fun tractEventBusIndex(): SubscriberInfoIndex = TractEventBusIndex()
    }
}
