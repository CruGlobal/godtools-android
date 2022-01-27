package org.cru.godtools.user.data

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import dagger.multibindings.IntoSet
import org.ccci.gto.android.common.dagger.eager.EagerSingleton
import org.ccci.gto.android.common.dagger.eager.EagerSingleton.ThreadMode
import org.cru.godtools.user.data.analytics.UserAnalyticsService
import org.greenrobot.eventbus.meta.SubscriberInfoIndex

@Module
@InstallIn(SingletonComponent::class)
object UserDataModule {
    @IntoSet
    @Provides
    @Reusable
    internal fun userDataEventBusIndex(): SubscriberInfoIndex = UserEventBusIndex()

    @Provides
    @ElementsIntoSet
    @EagerSingleton(threadMode = ThreadMode.ASYNC)
    internal fun backgroundEagerSingletons(analyticsService: UserAnalyticsService) = setOf<Any>(analyticsService)
}
