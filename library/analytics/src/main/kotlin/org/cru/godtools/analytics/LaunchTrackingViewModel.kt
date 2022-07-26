package org.cru.godtools.analytics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.lifecycle.delegate
import org.cru.godtools.analytics.model.LaunchAnalyticsActionEvent
import org.cru.godtools.base.Settings
import org.greenrobot.eventbus.EventBus

@HiltViewModel
class LaunchTrackingViewModel @Inject constructor(
    private val eventBus: EventBus,
    private val settings: Settings,
    state: SavedStateHandle
) : ViewModel() {
    private var launchTracked by state.delegate(ifNull = false)

    fun trackLaunch() {
        // short-circuit if we have already tracked this launch
        if (launchTracked) return

        // trigger launch action event
        settings.trackLaunch()
        eventBus.post(LaunchAnalyticsActionEvent(settings.launches))
        launchTracked = true
    }
}
