package org.cru.godtools.analytics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import org.ccci.gto.android.common.dagger.viewmodel.AssistedSavedStateViewModelFactory
import org.cru.godtools.analytics.model.LaunchAnalyticsActionEvent
import org.cru.godtools.base.Settings
import org.greenrobot.eventbus.EventBus

class LaunchTrackingViewModel @AssistedInject constructor(
    private val eventBus: EventBus,
    private val settings: Settings,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {
    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<LaunchTrackingViewModel>

    private var launchTracked: Boolean
        get() = state.get<Boolean>("launchTracked") ?: false
        set(value) {
            state.set("launchTracked", value)
        }

    fun trackLaunch() {
        // short-circuit if we have already tracked this launch
        if (launchTracked) return

        // trigger launch action event
        settings.trackLaunch()
        eventBus.post(LaunchAnalyticsActionEvent(settings.launches))
        launchTracked = true
    }
}
