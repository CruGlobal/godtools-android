package org.cru.godtools.tract

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import org.greenrobot.eventbus.meta.SubscriberInfoIndex

@Module
@InstallIn(SingletonComponent::class)
object TractRendererModule {
    @IntoSet
    @Provides
    @Reusable
    fun tractEventBusIndex(): SubscriberInfoIndex = TractEventBusIndex()
}
