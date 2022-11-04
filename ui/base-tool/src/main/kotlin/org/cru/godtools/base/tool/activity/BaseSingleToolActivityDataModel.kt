package org.cru.godtools.base.tool.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.androidx.lifecycle.switchCombineWith
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.keynote.godtools.android.db.repository.TranslationsRepository

@HiltViewModel
open class BaseSingleToolActivityDataModel @Inject constructor(
    manifestManager: ManifestManager,
    downloadManager: GodToolsDownloadManager,
    translationsRepository: TranslationsRepository
) : ViewModel() {
    val toolCode = MutableLiveData<String?>()
    val locale = MutableLiveData<Locale?>()

    val manifest = toolCode.switchCombineWith(locale) { code, locale ->
        when {
            code == null || locale == null -> emptyLiveData()
            else -> manifestManager.getLatestPublishedManifestLiveData(code, locale)
        }
    }

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
