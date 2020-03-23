package org.cru.godtools.base.tool.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.androidx.lifecycle.switchCombineWith
import org.cru.godtools.base.tool.service.ManifestManager
import java.util.Locale

open class LatestPublishedManifestDataModel(application: Application) : AndroidViewModel(application) {
    private val manifestManager = ManifestManager.getInstance(application)

    val toolCode = MutableLiveData<String?>()
    val locale = MutableLiveData<Locale?>()

    val manifest = toolCode.switchCombineWith(locale) { code, locale ->
        when {
            code == null || locale == null -> emptyLiveData()
            else -> manifestManager.getLatestPublishedManifestLiveData(code, locale)
        }
    }
}
