package org.cru.godtools.base.tool.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
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
) : BaseToolRendererViewModel() {
    val toolCode = MutableStateFlow<String?>(null)
    val locale = MutableLiveData<Locale?>()

    private val toolCodeLiveData = toolCode.asLiveData()
    val manifest = toolCodeLiveData.switchCombineWith(locale) { code, locale ->
        when {
            code == null || locale == null -> emptyLiveData()
            else -> manifestManager.getLatestPublishedManifestLiveData(code, locale)
        }
    }

    val translation = toolCodeLiveData.switchCombineWith(locale) { code, locale ->
        when {
            code == null || locale == null -> emptyLiveData()
            else -> translationsRepository.getLatestTranslationLiveData(code, locale, trackAccess = true)
        }
    }

    val downloadProgress = toolCodeLiveData.switchCombineWith(locale) { tool, locale ->
        when {
            tool == null || locale == null -> emptyLiveData()
            else -> downloadManager.getDownloadProgressLiveData(tool, locale)
        }
    }
}
