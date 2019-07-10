package org.cru.godtools.analytics.facebook

import androidx.annotation.WorkerThread
import com.facebook.appevents.AppEventsLogger
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.base.util.SingletonHolder
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class FacebookAnalyticsService private constructor() {
    private val logger = AppEventsLogger.newLogger(null)

    init {
        EventBus.getDefault().register(this)
    }

    // region Analytics Events
    @WorkerThread
    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onAnalyticsScreenEvent(event: AnalyticsScreenEvent) {
        if (event.isForSystem(AnalyticsSystem.FACEBOOK)) logger.logEvent(event.screen)
    }

    @WorkerThread
    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onAnalyticsActionEvent(event: AnalyticsActionEvent) {
        if (event.isForSystem(AnalyticsSystem.FACEBOOK)) logger.logEvent(event.action)
    }
    // endregion Analytics Events

    companion object : SingletonHolder<FacebookAnalyticsService, Any?>({ FacebookAnalyticsService() })
}
