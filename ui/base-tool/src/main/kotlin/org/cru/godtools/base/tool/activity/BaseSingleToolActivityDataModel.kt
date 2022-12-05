package org.cru.godtools.base.tool.activity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.emitAll
import org.ccci.gto.android.common.kotlin.coroutines.flow.combineTransformLatest
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.user.activity.UserActivityManager
import org.keynote.godtools.android.db.repository.TranslationsRepository

@HiltViewModel
open class BaseSingleToolActivityDataModel @Inject constructor(
    downloadManager: GodToolsDownloadManager,
    manifestManager: ManifestManager,
    translationsRepository: TranslationsRepository,
    userActivityManager: UserActivityManager,
    savedState: SavedStateHandle,
) : BaseToolRendererViewModel(downloadManager, manifestManager, userActivityManager, savedState) {
    val translation = toolCode.combineTransformLatest(locale) { tool, locale ->
        when {
            tool == null || locale == null -> emit(null)
            else -> emitAll(translationsRepository.getLatestTranslationFlow(tool, locale, trackAccess = true))
        }
    }.asLiveData()
}
