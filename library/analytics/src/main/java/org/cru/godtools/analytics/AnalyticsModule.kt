package org.cru.godtools.analytics

import androidx.lifecycle.ViewModel
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import org.ccci.gto.android.common.dagger.eager.EagerSingleton
import org.ccci.gto.android.common.dagger.eager.EagerSingleton.ThreadMode
import org.ccci.gto.android.common.dagger.viewmodel.AssistedSavedStateViewModelFactory
import org.ccci.gto.android.common.dagger.viewmodel.ViewModelKey
import org.cru.godtools.analytics.adobe.AdobeAnalyticsService
import org.cru.godtools.analytics.appsflyer.AppsFlyerAnalyticsService
import org.cru.godtools.analytics.facebook.FacebookAnalyticsService
import org.cru.godtools.analytics.firebase.FirebaseAnalyticsService
import org.cru.godtools.analytics.snowplow.SnowplowAnalyticsService
import org.greenrobot.eventbus.meta.SubscriberInfoIndex

@AssistedModule
@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {
    @Binds
    @IntoMap
    @ViewModelKey(LaunchTrackingViewModel::class)
    abstract fun launchTrackingViewModel(f: LaunchTrackingViewModel.Factory):
        AssistedSavedStateViewModelFactory<out ViewModel>

    companion object {
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
}
