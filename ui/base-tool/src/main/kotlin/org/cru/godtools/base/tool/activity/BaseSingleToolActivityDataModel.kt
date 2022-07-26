package org.cru.godtools.base.tool.activity

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.androidx.lifecycle.switchCombineWith
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.tool.viewmodel.LatestPublishedManifestDataModel
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.keynote.godtools.android.db.repository.TranslationsRepository

@HiltViewModel
open class BaseSingleToolActivityDataModel @Inject constructor(
    manifestManager: ManifestManager,
    downloadManager: GodToolsDownloadManager,
    translationsRepository: TranslationsRepository
) : LatestPublishedManifestDataModel(manifestManager) {
    val translation = toolCode.switchCombineWith(locale) { code, locale ->
        when {
            code == null || locale == null -> emptyLiveData()
            else -> translationsRepository.getLatestTranslationLiveData(code, locale, trackAccess = true)
        }
    }

    val downloadProgress = toolCode.switchCombineWith(locale) { tool, locale ->
        when {
            tool == null || locale == null -> emptyLiveData()
            else -> downloadManager.getDownloadProgressLiveData(tool, locale)
        }
    }
}
