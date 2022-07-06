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
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.findAsFlow
import org.ccci.gto.android.common.db.getAsFlow
import org.ccci.gto.android.common.kotlin.coroutines.flow.StateFlowValue
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao
import org.keynote.godtools.android.db.repository.LanguagesRepository
import org.keynote.godtools.android.db.repository.ToolsRepository
import org.keynote.godtools.android.db.repository.TranslationsRepository

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ToolViewModels @Inject constructor(
    private val dao: GodToolsDao,
    private val downloadManager: GodToolsDownloadManager,
    private val fileSystem: ToolFileSystem,
    private val languagesRepository: LanguagesRepository,
    private val settings: Settings,
    private val toolsRepository: ToolsRepository,
    private val translationsRepository: TranslationsRepository
) : ViewModel() {
    private val toolViewModels = mutableMapOf<String, ToolViewModel>()
    operator fun get(tool: String) = toolViewModels.getOrPut(tool) { ToolViewModel(tool) }
    fun initializeToolViewModel(code: String, tool: Tool) {
        toolViewModels.getOrPut(code) { ToolViewModel(code, tool) }
    }

    private val primaryLanguage = settings.primaryLanguageFlow
        .flatMapLatest { languagesRepository.getLanguageFlow(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    private val parallelLanguage = settings.parallelLanguageFlow
        .flatMapLatest { it?.let { languagesRepository.getLanguageFlow(it) } ?: flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    inner class ToolViewModel(val code: String, initialTool: Tool? = null) {
        val tool = toolsRepository.getToolFlow(code)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), initialTool)

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
            .flatMapLatest { translationsRepository.getLatestTranslationFlow(code, it) }
            .map { StateFlowValue(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), StateFlowValue.Initial<Translation?>(null))
        private val defaultTranslation = translationsRepository.getLatestTranslationFlow(code, Settings.defaultLanguage)
            .map { StateFlowValue(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), StateFlowValue.Initial<Translation?>(null))
        val parallelTranslation = tool.flatMapLatest { t ->
            when {
                t == null || !t.type.supportsParallelLanguage -> flowOf(null)
                else -> settings.parallelLanguageFlow.flatMapLatest {
                    translationsRepository.getLatestTranslationFlow(t.code, it)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

        val primaryLanguage get() = this@ToolViewModels.primaryLanguage
        val parallelLanguage get() = this@ToolViewModels.parallelLanguage

        val firstTranslation = primaryTranslation
            .combine(defaultTranslation) { p, d -> if (p.isInitial || p.value != null) p else d }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), StateFlowValue.Initial<Translation?>(null))
        val secondTranslation = parallelTranslation
            .combine(firstTranslation) { p, f -> p?.takeUnless { p.languageCode == f.value?.languageCode } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

        val secondLanguage = secondTranslation
            .flatMapLatest { it?.languageCode?.let { languagesRepository.getLanguageFlow(it) } ?: flowOf(null) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

        val downloadProgress = firstTranslation
            .combine(secondTranslation) { f, s -> f.value ?: s }
            .flatMapLatest {
                it?.let { downloadManager.getDownloadProgressFlow(code, it.languageCode) } ?: flowOf(null)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

        fun pinTool() = viewModelScope.launch { toolsRepository.pinTool(code) }
        fun unpinTool() = viewModelScope.launch { toolsRepository.unpinTool(code) }
    }
}
