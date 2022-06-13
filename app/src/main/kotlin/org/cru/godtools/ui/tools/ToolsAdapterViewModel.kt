package org.cru.godtools.ui.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.findAsFlow
import org.ccci.gto.android.common.db.getAsFlow
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ToolFileSystem
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
    private val fileSystem: ToolFileSystem,
    private val settings: Settings
) : ViewModel() {
    private val toolViewModels = mutableMapOf<String, ToolViewModel>()
    fun getToolViewModel(tool: String) = toolViewModels.getOrPut(tool) { ToolViewModel(tool) }

    inner class ToolViewModel(val code: String) {
        val tool = dao.findAsFlow<Tool>(code)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

        val banner = tool
            .map { it?.bannerId }.distinctUntilChanged()
            .flatMapLatest { it?.let { dao.findAsFlow<Attachment>(it) } ?: flowOf(null) }
            .map { it?.takeIf { it.isDownloaded } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
        val bannerFile = banner
            .map { it?.getFile(fileSystem) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

        val availableLanguages = Query.select<Translation>()
            .where(TranslationTable.FIELD_TOOL.eq(code))
            .getAsFlow(dao)
            .map { it.map { it.languageCode }.distinct() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

        val primaryTranslation = settings.primaryLanguageFlow
            .flatMapLatest { dao.getLatestTranslationFlow(code, it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
        private val defaultTranslation = dao.getLatestTranslationFlow(code, Settings.defaultLanguage)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
        val parallelTranslation = tool.flatMapLatest { t ->
            when {
                t == null || !t.type.supportsParallelLanguage -> flowOf(null)
                else -> settings.parallelLanguageFlow.flatMapLatest { dao.getLatestTranslationFlow(t.code, it) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

        val primaryLanguage = settings.primaryLanguageFlow
            .flatMapLatest { dao.findAsFlow<Language>(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
        val parallelLanguage = settings.parallelLanguageFlow
            .flatMapLatest { it?.let { dao.findAsFlow<Language>(it) } ?: flowOf(null) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

        val firstTranslation = combine(primaryTranslation, defaultTranslation) { p, d -> p ?: d }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
        val secondTranslation = combine(parallelTranslation, firstTranslation) { p, f ->
            p?.takeUnless { p.languageCode == f?.languageCode }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

        val firstLanguage = firstTranslation
            .flatMapLatest { it?.languageCode?.let { dao.findAsFlow<Language>(it) } ?: flowOf(null) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
        val secondLanguage = secondTranslation
            .flatMapLatest { it?.languageCode?.let { dao.findAsFlow<Language>(it) } ?: flowOf(null) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

        val downloadProgress = combine(firstTranslation, secondTranslation) { f, s -> f ?: s }
            .flatMapLatest {
                it?.let { downloadManager.getDownloadProgressFlow(code, it.languageCode) } ?: flowOf(null)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    }
}
