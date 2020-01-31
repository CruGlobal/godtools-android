package org.cru.godtools.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class GlobalDashboardDataModel(application: Application) : AndroidViewModel(application) {

    // TODO:  Create temporary Data to be updated in later PR
    val uniqueUsers = MutableLiveData<Int>()
    val gospelPresentation = MutableLiveData<Int>()
    val sessions = MutableLiveData<Int>()
    val countries = MutableLiveData<Int>()
}
