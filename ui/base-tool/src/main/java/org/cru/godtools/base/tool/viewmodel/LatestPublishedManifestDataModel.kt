package org.cru.godtools.base.tool.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.androidx.lifecycle.switchCombineWith
import org.cru.godtools.base.tool.service.ManifestManager

@HiltViewModel
open class LatestPublishedManifestDataModel @Inject constructor(manifestManager: ManifestManager) :
    ViewModel() {
    val toolCode = MutableLiveData<String?>()
    val locale = MutableLiveData<Locale?>()

    val manifest = toolCode.switchCombineWith(locale) { code, locale ->
        when {
            code == null || locale == null -> emptyLiveData()
            else -> manifestManager.getLatestPublishedManifestLiveData(code, locale)
        }
    }
}
