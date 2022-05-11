package org.cru.godtools.ui.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.androidx.lifecycle.orEmpty
import org.ccci.gto.android.common.androidx.lifecycle.switchCombineWith
import org.ccci.gto.android.common.db.findAsFlow
import org.ccci.gto.android.common.db.findLiveData
import org.cru.godtools.base.Settings
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.GodToolsDao

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ToolsAdapterViewModel @Inject constructor(
    private val dao: GodToolsDao,
    private val downloadManager: GodToolsDownloadManager,
    private val settings: Settings
) : ViewModel() {
    private val toolViewModels = mutableMapOf<String, ToolViewModel>()
    fun getToolViewModel(tool: String) = toolViewModels.getOrPut(tool) { ToolViewModel(tool) }

    inner class ToolViewModel(private val code: String) {
        private val tool = dao.findAsFlow<Tool>(code)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

        val banner = tool
            .map { it?.bannerId }.distinctUntilChanged()
            .flatMapLatest { it?.let { dao.findAsFlow<Attachment>(it) } ?: flowOf(null) }
            .map { it?.takeIf { it.isDownloaded } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

        private val primaryTranslation =
            settings.primaryLanguageLiveData.switchMap { dao.getLatestTranslationLiveData(code, it) }
        private val defaultTranslation = dao.getLatestTranslationLiveData(code, Settings.defaultLanguage)
        val firstTranslation = primaryTranslation.combineWith(defaultTranslation) { p, d -> p ?: d }
        val parallelTranslation =
            settings.parallelLanguageLiveData.switchMap { dao.getLatestTranslationLiveData(code, it) }

        val firstLanguage = firstTranslation.switchMap { t ->
            t?.languageCode?.let { dao.findLiveData<Language>(it) }.orEmpty()
        }
        val parallelLanguage = parallelTranslation.switchMap { t ->
            t?.languageCode?.let { dao.findLiveData<Language>(it) }.orEmpty()
        }

        val downloadProgress =
            primaryTranslation.switchCombineWith(defaultTranslation, parallelTranslation) { prim, def, para ->
                when {
                    prim != null -> downloadManager.getDownloadProgressLiveData(code, prim.languageCode)
                    def != null -> downloadManager.getDownloadProgressLiveData(code, def.languageCode)
                    para != null -> downloadManager.getDownloadProgressLiveData(code, para.languageCode)
                    else -> emptyLiveData()
                }
            }
    }
}
