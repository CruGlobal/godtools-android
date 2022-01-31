package org.cru.godtools.ui.dashboard.tools

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ToolsFragmentDataModel() : ViewModel() {
    // TODO: Check if has Spotlight Tools
    val hasSpotlight = MutableLiveData(false)
}
