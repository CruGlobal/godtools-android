package org.cru.godtools.tract.activity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

private const val STATE_LIVE_SHARE_TUTORIAL_SHOWN = "liveShareTutorialShown"

class TractActivitySavedState(private val savedState: SavedStateHandle) : ViewModel() {
    var liveShareTutorialShown: Boolean
        get() = savedState[STATE_LIVE_SHARE_TUTORIAL_SHOWN] ?: false
        set(value) {
            savedState[STATE_LIVE_SHARE_TUTORIAL_SHOWN] = value
        }
}
