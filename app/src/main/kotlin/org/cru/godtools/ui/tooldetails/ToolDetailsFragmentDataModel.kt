package org.cru.godtools.ui.tooldetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
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
import org.ccci.gto.android.common.androidx.lifecycle.orEmpty
import org.ccci.gto.android.common.androidx.lifecycle.switchCombineWith
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.findAsFlow
import org.ccci.gto.android.common.db.getAsFlow
import org.cru.godtools.base.EXTRA_TOOL
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.shortcuts.GodToolsShortcutManager
import org.keynote.godtools.android.db.Contract.LanguageTable
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao
import org.keynote.godtools.android.db.repository.ToolsRepository
import org.keynote.godtools.android.db.repository.TranslationsRepository

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ToolDetailsFragmentDataModel @Inject constructor(
    private val dao: GodToolsDao,
    private val downloadManager: GodToolsDownloadManager,
    manifestManager: ManifestManager,
    settings: Settings,
    private val shortcutManager: GodToolsShortcutManager,
    private val toolFileSystem: ToolFileSystem,
    toolsRepository: ToolsRepository,
    translationsRepository: TranslationsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val toolCode = savedStateHandle.getStateFlow<String?>(EXTRA_TOOL, null)
    fun setToolCode(code: String) = savedStateHandle.set(EXTRA_TOOL, code)

    val tool = toolCode
        .flatMapLatest { it?.let { toolsRepository.getToolFlow(it) } ?: flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    val banner = tool
        .map { it?.detailsBannerId }.distinctUntilChanged()
        .flatMapLatest { it?.let { dao.findAsFlow<Attachment>(it) } ?: flowOf(null) }
        .map { it?.takeIf { it.isDownloaded }?.getFile(toolFileSystem) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    val bannerAnimation = tool
        .map { it?.detailsBannerAnimationId }.distinctUntilChanged()
        .flatMapLatest { it?.let { dao.findAsFlow<Attachment>(it) } ?: flowOf(null) }
        .map { it?.takeIf { it.isDownloaded }?.getFile(toolFileSystem) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    internal val toolCodeLiveData = savedStateHandle.getLiveData<String?>(EXTRA_TOOL).distinctUntilChanged<String?>()
    val primaryTranslation = toolCodeLiveData.switchCombineWith(settings.primaryLanguageLiveData) { tool, locale ->
        translationsRepository.getLatestTranslationLiveData(tool, locale)
    }
    val parallelTranslation = toolCodeLiveData.switchCombineWith(settings.parallelLanguageLiveData) { tool, locale ->
        translationsRepository.getLatestTranslationLiveData(tool, locale)
    }

    val primaryManifest = toolCodeLiveData.switchCombineWith(settings.primaryLanguageLiveData) { code, locale ->
        code?.let { manifestManager.getLatestPublishedManifestLiveData(code, locale) }.orEmpty()
    }

    val shortcut = tool.map {
        it?.takeIf { shortcutManager.canPinToolShortcut(it) }
            ?.let { shortcutManager.getPendingToolShortcut(it.code) }
    }.asLiveData()

    val downloadProgress = toolCodeLiveData.switchCombineWith(settings.primaryLanguageLiveData) { tool, locale ->
        tool?.let { downloadManager.getDownloadProgressLiveData(tool, locale) }.orEmpty()
    }

    val availableLanguages = toolCode
        .flatMapLatest {
            when (it) {
                null -> flowOf(emptyList())
                else -> Query.select<Language>()
                    .join(LanguageTable.SQL_JOIN_TRANSLATION.andOn(TranslationTable.FIELD_TOOL eq it))
                    .getAsFlow(dao)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val variants = tool.flatMapLatest {
        when (val metatool = it?.metatoolCode) {
            null -> flowOf(emptyList())
            else -> Query.select<Tool>().where(ToolTable.FIELD_META_TOOL.eq(metatool)).getAsFlow(dao)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    internal val pages = variants.map {
        buildList {
            add(ToolDetailsPagerAdapter.Page.DESCRIPTION)
            if (it.isNotEmpty()) add(ToolDetailsPagerAdapter.Page.VARIANTS)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
}
