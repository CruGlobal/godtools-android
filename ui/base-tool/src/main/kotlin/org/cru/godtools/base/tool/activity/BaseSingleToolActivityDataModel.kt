package org.cru.godtools.base.tool.activity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.emitAll
import org.ccci.gto.android.common.kotlin.coroutines.flow.combineTransformLatest
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.keynote.godtools.android.db.repository.TranslationsRepository

@HiltViewModel
open class BaseSingleToolActivityDataModel @Inject constructor(
    manifestManager: ManifestManager,
    downloadManager: GodToolsDownloadManager,
    translationsRepository: TranslationsRepository,
    savedState: SavedStateHandle,
) : BaseToolRendererViewModel(savedState) {
    val manifest = toolCode.combineTransformLatest(locale) { tool, locale ->
        when {
            tool == null || locale == null -> emit(null)
            else -> emitAll(manifestManager.getLatestPublishedManifestFlow(tool, locale))
        }
    }.asLiveData()

    val translation = toolCode.combineTransformLatest(locale) { tool, locale ->
        when {
            tool == null || locale == null -> emit(null)
            else -> emitAll(translationsRepository.getLatestTranslationFlow(tool, locale, trackAccess = true))
        }
    }.asLiveData()

    val downloadProgress = toolCode.combineTransformLatest(locale) { tool, locale ->
        when {
            tool == null || locale == null -> emit(null)
            else -> emitAll(downloadManager.getDownloadProgressFlow(tool, locale))
        }
    }.asLiveData()
}
