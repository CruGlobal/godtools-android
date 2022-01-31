package org.cru.godtools.ui.tools

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ToolsContainerFragmentDataModel() : ViewModel() {
    // TODO: Check if has Spotlight Tools
    val hasSpotlight = MutableLiveData(false)
}
