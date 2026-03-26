package org.cru.godtools.base.tool.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.emitAll
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.androidx.lifecycle.switchCombineWith
import org.ccci.gto.android.common.kotlin.coroutines.flow.combineTransformLatest
import org.cru.godtools.base.tool.service.ManifestManager

@HiltViewModel
open class LatestPublishedManifestDataModel @Inject constructor(manifestManager: ManifestManager) : ViewModel() {
    val toolCode = MutableLiveData<String?>()
    val locale = MutableLiveData<Locale?>()

    val manifest = toolCode.switchCombineWith(locale) { code, locale ->
        when {
            code == null || locale == null -> emptyLiveData()
            else -> manifestManager.getLatestPublishedManifestLiveData(code, locale)
        }
    }

    val manifestFlow = toolCode.asFlow().combineTransformLatest(locale.asFlow()) { code, locale ->
        when {
            code == null || locale == null -> emit(null)
            else -> emitAll(manifestManager.getLatestPublishedManifestFlow(code, locale))
        }
    }
}
