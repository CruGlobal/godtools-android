package org.cru.godtools.analytics.user

import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.cru.godtools.analytics.model.AnalyticsBaseEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.user.data.Counters
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber

private const val TAG = "UserAnalyticsService"

@Singleton
internal class UserAnalyticsService @VisibleForTesting internal constructor(
    eventBus: EventBus,
    private val userCounters: Counters,
    private val coroutineScope: CoroutineScope
) {
    @Inject
    internal constructor(
        eventBus: EventBus,
        userCounters: Counters
    ) : this(eventBus, userCounters, CoroutineScope(SupervisorJob()))

    // region Tracking Events
    init {
        eventBus.register(this)
    }

    @WorkerThread
    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onAnalyticsEvent(event: AnalyticsBaseEvent) {
        if (!event.isForSystem(AnalyticsSystem.USER)) return

        val counterName = event.userCounterName ?: return
        if (!userCounters.isValidCounterName(counterName)) {
            Timber.tag(TAG).e(IllegalArgumentException(), "Invalid User analytics event name: %s", counterName)
            return
        }

        coroutineScope.launch { userCounters.updateCounter(counterName) }
    }
    // endregion Tracking Events
}
