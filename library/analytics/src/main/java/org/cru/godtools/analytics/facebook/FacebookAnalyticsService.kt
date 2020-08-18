package org.cru.godtools.analytics.facebook

import android.content.Context
import androidx.annotation.WorkerThread
import com.facebook.appevents.AppEventsLogger
import javax.inject.Inject
import javax.inject.Singleton
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Singleton
class FacebookAnalyticsService @Inject internal constructor(context: Context, eventBus: EventBus) {
    private val logger = AppEventsLogger.newLogger(context)

    init {
        eventBus.register(this)
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
}
