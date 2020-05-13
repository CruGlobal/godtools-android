package org.cru.godtools.base.tool.activity

import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.androidx.lifecycle.switchCombineWith
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.tool.viewmodel.LatestPublishedManifestDataModel
import org.keynote.godtools.android.db.GodToolsDao
import javax.inject.Inject

internal class BaseSingleToolActivityDataModel @Inject constructor(
    manifestManager: ManifestManager,
    private val dao: GodToolsDao
) : LatestPublishedManifestDataModel(manifestManager) {
    val translation = toolCode.switchCombineWith(locale) { code, locale ->
        when {
            code == null || locale == null -> emptyLiveData()
            else -> dao.getLatestTranslationLiveData(code, locale, trackAccess = true)
        }
    }
}
