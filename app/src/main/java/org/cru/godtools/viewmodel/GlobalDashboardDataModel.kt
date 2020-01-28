package org.cru.godtools.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import me.thekey.android.TheKey
import org.ccci.gto.android.common.lifecycle.combineWith

class GlobalDashboardDataModel(application: Application) : AndroidViewModel(application) {
    init {
        TheKey.getInstance(getApplication()).let {
            firstName.value = it.cachedAttributes.firstName
            lastName.value = it.cachedAttributes.lastName
        }
    }

    private val firstName = MutableLiveData<String>()
    private val lastName = MutableLiveData<String>()

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
