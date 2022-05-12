package org.cru.godtools.ui.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
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
import org.ccci.gto.android.common.androidx.lifecycle.combine
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.androidx.lifecycle.orEmpty
import org.ccci.gto.android.common.androidx.lifecycle.switchCombine
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.findAsFlow
import org.ccci.gto.android.common.db.findLiveData
import org.ccci.gto.android.common.db.getAsLiveData
import org.cru.godtools.base.Settings
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.keynote.godtools.android.db.Contract.TranslationTable
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
        val tool = dao.findAsFlow<Tool>(code)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

        val banner = tool
            .map { it?.bannerId }.distinctUntilChanged()
            .flatMapLatest { it?.let { dao.findAsFlow<Attachment>(it) } ?: flowOf(null) }
            .map { it?.takeIf { it.isDownloaded } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

        val firstTranslation = combine(
            settings.primaryLanguageLiveData.switchMap { dao.getLatestTranslationLiveData(code, it) },
            dao.getLatestTranslationLiveData(code, Settings.defaultLanguage)
        ) { p, d -> p ?: d }
        val parallelTranslation =
            settings.parallelLanguageLiveData.switchMap { dao.getLatestTranslationLiveData(code, it) }

        val firstLanguage = firstTranslation.switchMap { t ->
            t?.languageCode?.let { dao.findLiveData<Language>(it) }.orEmpty()
        }
        val parallelLanguage = parallelTranslation.switchMap { t ->
            t?.languageCode?.let { dao.findLiveData<Language>(it) }.orEmpty()
        }

        val availableLanguages = Query.select<Translation>()
            .where(TranslationTable.FIELD_TOOL.eq(code))
            .getAsLiveData(dao)
            .map { it.map { it.languageCode }.distinct() }

        val downloadProgress = switchCombine(firstTranslation, parallelTranslation) { first, para ->
            when {
                first != null -> downloadManager.getDownloadProgressLiveData(code, first.languageCode)
                para != null -> downloadManager.getDownloadProgressLiveData(code, para.languageCode)
                else -> emptyLiveData()
            }
        }
    }
}
