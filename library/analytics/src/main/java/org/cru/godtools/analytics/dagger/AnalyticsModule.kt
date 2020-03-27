package org.cru.godtools.analytics.dagger

import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import org.ccci.gto.android.common.dagger.eager.EagerSingleton
import org.ccci.gto.android.common.dagger.eager.EagerSingleton.ThreadMode
import org.cru.godtools.analytics.adobe.AdobeAnalyticsService

@Module
abstract class AnalyticsModule {
    companion object {
        @Provides
        @ElementsIntoSet
        @EagerSingleton(ThreadMode.MAIN)
        internal fun mainEagerSingletons(adobe: AdobeAnalyticsService) = setOf<Any>(adobe)
    }
}
