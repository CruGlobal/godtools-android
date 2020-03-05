package org.cru.godtools.analytics

import androidx.annotation.MainThread
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.base.model.Event
import org.cru.godtools.base.util.SingletonHolder
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber

private const val TAG = "AnalyticsService"

class TimberAnalyticsService private constructor() {
    companion object : SingletonHolder<TimberAnalyticsService, Any?>({ TimberAnalyticsService() })

    init {
        EventBus.getDefault().register(this)
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTrackContentEvent(event: Event) {
        Timber.tag(TAG)
            .d("onTrackContentEvent(%s:%s)", event.id.namespace, event.id.name)
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
