package org.cru.godtools.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.ccci.gto.android.common.lifecycle.combineWith

class GlobalDashboardDataModel : ViewModel() {

    val firstName = MutableLiveData<String>()
    val lastName = MutableLiveData<String>()
    // TODO:  Create temporary Data to be updated in later PR

    val uniqueUsers = MutableLiveData<Int>()
    val gospelPresentation = MutableLiveData<Int>()
    val sessions = MutableLiveData<Int>()
    val countries = MutableLiveData<Int>()
    val accountName = firstName.combineWith(lastName) { first, last ->
        "$first $last"
    }
    val accountDate = MutableLiveData<String>()
}
