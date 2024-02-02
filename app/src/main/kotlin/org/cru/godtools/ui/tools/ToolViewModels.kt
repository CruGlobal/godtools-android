package org.cru.godtools.ui.tools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.kotlin.coroutines.flow.StateFlowValue
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.downloadmanager.GodToolsDownloadManager
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.sync.GodToolsSyncService

internal const val EXTRA_ADDITIONAL_LANGUAGE = "additionalLanguage"

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ToolViewModels @Inject constructor(
    private val attachmentsRepository: AttachmentsRepository,
    private val downloadManager: GodToolsDownloadManager,
    private val fileSystem: ToolFileSystem,
    private val languagesRepository: LanguagesRepository,
    private val manifestManager: ManifestManager,
    private val settings: Settings,
    private val syncService: GodToolsSyncService,
    private val toolsRepository: ToolsRepository,
    private val translationsRepository: TranslationsRepository,
    savedState: SavedStateHandle,
) : ViewModel() {
    private val toolViewModels = mutableMapOf<String, ToolViewModel>()
    operator fun get(tool: String) = toolViewModels.getOrPut(tool) { ToolViewModel(tool) }
    operator fun get(code: String, tool: Tool?) = toolViewModels.getOrPut(code) { ToolViewModel(code, tool) }

    private val additionalLocale = savedState.getStateFlow<Locale?>(EXTRA_ADDITIONAL_LANGUAGE, null)

    private val appLanguage = settings.appLanguageFlow
        .flatMapLatest { languagesRepository.findLanguageFlow(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // TODO: determine if we need to repurpose this functionality for the Language Update
    private val parallelLanguage: StateFlow<Language?> = MutableStateFlow(null)

    inner class ToolViewModel(val code: String, initialTool: Tool? = null) {
        val tool = toolsRepository.findToolFlow(code)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), initialTool)

        val banner = tool
            .map { it?.bannerId }.distinctUntilChanged()
            .flatMapLatest { it?.let { attachmentsRepository.findAttachmentFlow(it) } ?: flowOf(null) }
            .map { it?.takeIf { it.isDownloaded } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
        val bannerFile = tool.attachmentFileFlow { it?.bannerId }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

        val detailsBanner = tool.attachmentFileFlow { it?.detailsBannerId ?: it?.bannerId }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
        val detailsBannerAnimation = tool.attachmentFileFlow { it?.detailsBannerAnimationId }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

        val availableLanguages = translationsRepository.getTranslationsFlowForTool(code)
            .map { it.map { it.languageCode }.toSet() }
            .distinctUntilChanged()
            .flatMapLatest { languagesRepository.getLanguagesFlowForLocales(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

        val appTranslation = settings.appLanguageFlow
            .flatMapLatest { translationsRepository.findLatestTranslationFlow(code, it) }
            .map { StateFlowValue(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), StateFlowValue.Initial<Translation?>(null))
        private val defaultTranslation = tool
            .map { it?.code to it?.defaultLocale }
            .distinctUntilChanged()
            .flatMapLatest { (code, locale) -> translationsRepository.findLatestTranslationFlow(code, locale) }
            .map { StateFlowValue(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), StateFlowValue.Initial<Translation?>(null))
        private val additionalTranslation = tool.flatMapLatest { t ->
            when {
                t == null || !t.type.supportsParallelLanguage -> flowOf(null)
                else -> additionalLocale.flatMapLatest { translationsRepository.findLatestTranslationFlow(t.code, it) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

        val appLanguage get() = this@ToolViewModels.appLanguage
        val parallelLanguage get() = this@ToolViewModels.parallelLanguage

        val firstTranslation = appTranslation
            .combine(defaultTranslation) { p, d -> if (p.isInitial || p.value != null) p else d }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), StateFlowValue.Initial<Translation?>(null))
        val secondTranslation = additionalTranslation
            .combine(firstTranslation) { p, f -> p?.takeUnless { p.languageCode == f.value?.languageCode } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

        val secondLanguage = secondTranslation
            .flatMapLatest { it?.languageCode?.let { languagesRepository.findLanguageFlow(it) } ?: flowOf(null) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

        val firstManifest = firstTranslation
            .map { it.value?.let { manifestManager.getManifest(it) } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

        val downloadProgress = firstTranslation
            .combine(secondTranslation) { f, s -> f.value ?: s }
            .flatMapLatest {
                it?.let { downloadManager.getDownloadProgressFlow(code, it.languageCode) } ?: flowOf(null)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

        fun pinTool() {
            viewModelScope.launch {
                toolsRepository.pinTool(code)
                syncService.syncDirtyFavoriteTools()
            }
            settings.setFeatureDiscovered(Settings.FEATURE_TOOL_FAVORITE)
        }
        fun unpinTool() = viewModelScope.launch {
            toolsRepository.unpinTool(code)
            syncService.syncDirtyFavoriteTools()
        }

        @Composable
        fun toState(
            secondLanguage: Language? = this.secondLanguage.collectAsState().value,
            eventSink: (ToolCard.Event) -> Unit = {}
        ): ToolCard.State {
            val translation by firstTranslation.collectAsState()

            return ToolCard.State(
                tool = tool.collectAsState().value,
                isLoaded = translation !is StateFlowValue.Initial,
                banner = bannerFile.collectAsState().value,
                translation = translation.value,
                appLanguage = appLanguage.collectAsState().value,
                appTranslation = appTranslation.collectAsState().value.value,
                secondLanguage = secondLanguage,
                secondTranslation = secondTranslation.collectAsState().value,
                availableLanguages = availableLanguages.collectAsState().value.size,
                downloadProgress = downloadProgress.collectAsState().value,
                eventSink = eventSink,
            )
        }
    }

    private fun Flow<Tool?>.attachmentFileFlow(transform: (value: Tool?) -> Long?) = this
        .map(transform).distinctUntilChanged()
        .flatMapLatest { it?.let { attachmentsRepository.findAttachmentFlow(it) } ?: flowOf(null) }
        .map { it?.takeIf { it.isDownloaded } }
        .map { it?.getFile(fileSystem) }
        .distinctUntilChanged()
}
