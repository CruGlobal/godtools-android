package org.cru.godtools.analytics

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.multibindings.ElementsIntoSet
import dagger.multibindings.IntoSet
import org.ccci.gto.android.common.dagger.eager.EagerSingleton
import org.ccci.gto.android.common.dagger.eager.EagerSingleton.ThreadMode
import org.cru.godtools.analytics.adobe.AdobeAnalyticsService
import org.greenrobot.eventbus.meta.SubscriberInfoIndex

@Module
abstract class AnalyticsModule {
    companion object {
        @IntoSet
        @Provides
        @Reusable
        internal fun analyticsEventBusIndex(): SubscriberInfoIndex = AnalyticsEventBusIndex()

        @Provides
        @ElementsIntoSet
        @EagerSingleton(ThreadMode.MAIN)
        internal fun mainEagerSingletons(adobe: AdobeAnalyticsService) = setOf<Any>(adobe)
    }
}
