package org.cru.godtools.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import org.cru.godtools.analytics.model.LaunchAnalyticsActionEvent
import org.cru.godtools.base.Settings
import org.greenrobot.eventbus.EventBus

class LaunchTrackingViewModel(app: Application, private val state: SavedStateHandle) : AndroidViewModel(app) {
    private val settings = Settings.getInstance(app)

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
        EventBus.getDefault().post(LaunchAnalyticsActionEvent(settings.launches))
        launchTracked = true
    }
}
