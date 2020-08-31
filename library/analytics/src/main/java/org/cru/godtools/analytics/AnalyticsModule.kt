package org.cru.godtools.analytics

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import dagger.multibindings.IntoSet
import org.ccci.gto.android.common.dagger.eager.EagerSingleton
import org.ccci.gto.android.common.dagger.eager.EagerSingleton.ThreadMode
import org.cru.godtools.analytics.adobe.AdobeAnalyticsService
import org.cru.godtools.analytics.appsflyer.AppsFlyerAnalyticsService
import org.cru.godtools.analytics.facebook.FacebookAnalyticsService
import org.cru.godtools.analytics.firebase.FirebaseAnalyticsService
import org.cru.godtools.analytics.snowplow.SnowplowAnalyticsService
import org.greenrobot.eventbus.meta.SubscriberInfoIndex

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {
    @IntoSet
    @Provides
    @Reusable
    internal fun analyticsEventBusIndex(): SubscriberInfoIndex = AnalyticsEventBusIndex()

    @Provides
    @ElementsIntoSet
    @EagerSingleton(threadMode = ThreadMode.MAIN)
    internal fun mainEagerSingletons(adobe: AdobeAnalyticsService, appsFlyer: AppsFlyerAnalyticsService) =
        setOf<Any>(adobe, appsFlyer)

    @Provides
    @ElementsIntoSet
    @EagerSingleton(threadMode = ThreadMode.MAIN_ASYNC)
    internal fun mainAsyncEagerSingletons(firebase: FirebaseAnalyticsService) = setOf<Any>(firebase)

    @Provides
    @ElementsIntoSet
    @EagerSingleton(threadMode = ThreadMode.ASYNC)
    internal fun backgroundEagerSingletons(facebook: FacebookAnalyticsService, snowplow: SnowplowAnalyticsService) =
        setOf<Any>(facebook, snowplow)
}
