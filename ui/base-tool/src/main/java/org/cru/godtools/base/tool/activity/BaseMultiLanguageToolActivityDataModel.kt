package org.cru.godtools.base.tool.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class BaseMultiLanguageToolActivityDataModel : ViewModel() {
    val toolCode = MutableLiveData<String?>()
}
