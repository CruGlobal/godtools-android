package org.cru.godtools.ui.tooldetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.cru.godtools.base.EXTRA_TOOL
import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.shortcuts.GodToolsShortcutManager

internal const val EXTRA_ADDITIONAL_LANGUAGE = "additionalLanguage"

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ToolDetailsViewModel @Inject constructor(
    attachmentsRepository: AttachmentsRepository,
    private val shortcutManager: GodToolsShortcutManager,
    private val toolFileSystem: ToolFileSystem,
    toolsRepository: ToolsRepository,
    languagesRepository: LanguagesRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val toolCode = savedStateHandle.getStateFlow<String?>(EXTRA_TOOL, null)
    fun setToolCode(code: String) = savedStateHandle.set(EXTRA_TOOL, code)
    val additionalLocale = savedStateHandle.getStateFlow<Locale?>(EXTRA_ADDITIONAL_LANGUAGE, null)

    val tool = toolCode
        .flatMapLatest { it?.let { toolsRepository.findToolFlow(it) } ?: flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    val banner = tool
        .map { it?.detailsBannerId }.distinctUntilChanged()
        .flatMapLatest { it?.let { attachmentsRepository.findAttachmentFlow(it) } ?: flowOf(null) }
        .map { it?.takeIf { it.isDownloaded }?.getFile(toolFileSystem) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val additionalLanguage = additionalLocale
        .flatMapLatest { it?.let { languagesRepository.findLanguageFlow(it) } ?: flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val shortcut = tool.map {
        it?.takeIf { shortcutManager.canPinToolShortcut(it) }
            ?.let { shortcutManager.getPendingToolShortcut(it.code) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val variants = tool.map { it?.metatoolCode }
        .distinctUntilChanged()
        .flatMapLatest { metatool ->
            when (metatool) {
                null -> flowOf(emptyList())
                else -> toolsRepository.getToolsFlow().map { it.filter { it.metatoolCode == metatool } }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    internal val pages = variants.map {
        buildList {
            add(ToolDetailsPage.DESCRIPTION)
            if (it.isNotEmpty()) add(ToolDetailsPage.VARIANTS)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), listOf(ToolDetailsPage.DESCRIPTION))
}
