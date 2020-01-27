package org.cru.godtools.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import me.thekey.android.TheKey

class GlobalDashboardDataModel : ViewModel() {

    val theKey = MutableLiveData<TheKey>()
    // TODO:  Create temporary Data to be updated in later PR

    val uniqueUsers = MutableLiveData<Int>()
    val gospelPresentation = MutableLiveData<Int>()
    val sessions = MutableLiveData<Int>()
    val countries = MutableLiveData<Int>()
    val accountName = Transformations.map(theKey) { key ->
        String.format("%s %s", key.cachedAttributes.firstName, key.cachedAttributes.lastName)
    }
    val accountDate = MutableLiveData<String>()
}
