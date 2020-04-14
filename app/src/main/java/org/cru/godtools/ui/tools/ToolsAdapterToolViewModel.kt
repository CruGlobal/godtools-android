package org.cru.godtools.ui.tools

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class ToolsAdapterToolViewModel @Inject constructor() : ViewModel() {
    val toolCode = MutableLiveData<String?>()
}
