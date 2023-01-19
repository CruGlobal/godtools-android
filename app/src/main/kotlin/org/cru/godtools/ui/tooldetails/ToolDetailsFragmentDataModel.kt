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
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsFlow
import org.cru.godtools.base.EXTRA_TOOL
import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.model.Tool
import org.cru.godtools.shortcuts.GodToolsShortcutManager
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao
import org.keynote.godtools.android.db.repository.ToolsRepository

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ToolDetailsFragmentDataModel @Inject constructor(
    attachmentsRepository: AttachmentsRepository,
    private val dao: GodToolsDao,
    private val shortcutManager: GodToolsShortcutManager,
    private val toolFileSystem: ToolFileSystem,
    toolsRepository: ToolsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val toolCode = savedStateHandle.getStateFlow<String?>(EXTRA_TOOL, null)
    fun setToolCode(code: String) = savedStateHandle.set(EXTRA_TOOL, code)

    val tool = toolCode
        .flatMapLatest { it?.let { toolsRepository.getToolFlow(it) } ?: flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    val banner = tool
        .map { it?.detailsBannerId }.distinctUntilChanged()
        .flatMapLatest { it?.let { attachmentsRepository.findAttachmentFlow(it) } ?: flowOf(null) }
        .map { it?.takeIf { it.isDownloaded }?.getFile(toolFileSystem) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    internal val toolCodeLiveData = savedStateHandle.getLiveData<String?>(EXTRA_TOOL).distinctUntilChanged<String?>()

    val shortcut = tool.map {
        it?.takeIf { shortcutManager.canPinToolShortcut(it) }
            ?.let { shortcutManager.getPendingToolShortcut(it.code) }
    }.asLiveData()

    val variants = tool.flatMapLatest {
        when (val metatool = it?.metatoolCode) {
            null -> flowOf(emptyList())
            else -> Query.select<Tool>().where(ToolTable.FIELD_META_TOOL.eq(metatool)).getAsFlow(dao)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    internal val pages = variants.map {
        buildList {
            add(ToolDetailsPage.DESCRIPTION)
            if (it.isNotEmpty()) add(ToolDetailsPage.VARIANTS)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), listOf(ToolDetailsPage.DESCRIPTION))
}
