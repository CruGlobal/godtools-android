package org.cru.godtools.tract

import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import org.greenrobot.eventbus.meta.SubscriberInfoIndex

@Module
@AssistedModule
@InstallIn(SingletonComponent::class)
object TractRendererModule {
    @IntoSet
    @Provides
    @Reusable
    fun tractEventBusIndex(): SubscriberInfoIndex = TractEventBusIndex()
}
