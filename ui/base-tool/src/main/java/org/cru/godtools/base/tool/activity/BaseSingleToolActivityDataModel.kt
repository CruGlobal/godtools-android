package org.cru.godtools.base.tool.activity

import javax.inject.Inject
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.androidx.lifecycle.switchCombineWith
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.tool.viewmodel.LatestPublishedManifestDataModel
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.keynote.godtools.android.db.GodToolsDao

internal class BaseSingleToolActivityDataModel @Inject constructor(
    manifestManager: ManifestManager,
    private val dao: GodToolsDao,
    private val downloadManager: GodToolsDownloadManager
) : LatestPublishedManifestDataModel(manifestManager) {
    val translation = toolCode.switchCombineWith(locale) { code, locale ->
        when {
            code == null || locale == null -> emptyLiveData()
            else -> dao.getLatestTranslationLiveData(code, locale, trackAccess = true)
        }
    }

    val downloadProgress = toolCode.switchCombineWith(locale) { tool, locale ->
        when {
            tool == null || locale == null -> emptyLiveData()
            else -> downloadManager.getDownloadProgressLiveData(tool, locale)
        }
    }
}
