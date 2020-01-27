package org.cru.godtools.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GlobalDashboardDataModel : ViewModel() {
    // TODO:  Create temporary Data to be updated in later PR

    val uniqueUsers = MutableLiveData<Int>()
    val gospelPresentation = MutableLiveData<Int>()
    val sessions = MutableLiveData<Int>()
    val countries = MutableLiveData<Int>()
    val accountName = MutableLiveData<String>()
    val accountDate = MutableLiveData<String>()
}
