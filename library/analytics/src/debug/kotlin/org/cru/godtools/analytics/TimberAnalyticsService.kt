package org.cru.godtools.analytics

import androidx.annotation.MainThread
import javax.inject.Inject
import javax.inject.Singleton
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber

private const val TAG = "AnalyticsService"

@Singleton
class TimberAnalyticsService @Inject internal constructor(eventBus: EventBus) {
    init {
        eventBus.register(this)
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAnalyticsScreenEvent(event: AnalyticsScreenEvent) {
        Timber.tag(TAG)
            .d("onAnalyticsScreenEvent('%s', '%s')", event.screen, event.locale)
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAnalyticsActionEvent(event: AnalyticsActionEvent) {
        Timber.tag(TAG)
            .d("onAnalyticsActionEvent('%s')", event.action)
    }
}
