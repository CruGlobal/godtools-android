package org.cru.godtools.ui.dashboard.tools

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.db.Expression.Companion.constants
import org.ccci.gto.android.common.db.Query
import org.cru.godtools.base.Settings
import org.cru.godtools.model.Tool
import org.cru.godtools.sync.GodToolsSyncService
import org.cru.godtools.ui.banner.BannerType
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao

private const val ATTR_SELECTED_CATEGORY = "selectedCategory"

private val QUERY_TOOLS_BASE = Query.select<Tool>().where(
    ToolTable.FIELD_TYPE.`in`(*constants(Tool.Type.TRACT, Tool.Type.ARTICLE, Tool.Type.CYOA)) and
        (ToolTable.FIELD_HIDDEN ne true)
).orderBy(ToolTable.COLUMN_DEFAULT_ORDER)

@VisibleForTesting
internal val QUERY_TOOLS = QUERY_TOOLS_BASE.join(ToolTable.SQL_JOIN_METATOOL.type("LEFT"))
    .andWhere(
        ToolTable.FIELD_META_TOOL.isNull() or
            (ToolTable.FIELD_CODE eq ToolTable.TABLE_META.field(ToolTable.COLUMN_DEFAULT_VARIANT))
    )
@VisibleForTesting
internal val QUERY_TOOLS_SPOTLIGHT = QUERY_TOOLS_BASE.andWhere(ToolTable.FIELD_SPOTLIGHT eq true)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ToolsViewModel @Inject constructor(
    dao: GodToolsDao,
    settings: Settings,
    private val syncService: GodToolsSyncService,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    val primaryLanguage = settings.primaryLanguageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), settings.primaryLanguage)

    val banner = settings.isFeatureDiscoveredFlow(Settings.FEATURE_TOOL_FAVORITE)
        .map { if (!it) BannerType.TOOL_LIST_FAVORITES else null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val spotlightTools = dao.getAsFlow(QUERY_TOOLS_SPOTLIGHT)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val tools = dao.getAsFlow(QUERY_TOOLS)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val categories = tools.mapLatest { it.mapNotNull { it.category }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // region Filters
    val filterCategory = savedState.getStateFlow<String?>(ATTR_SELECTED_CATEGORY, null)
    fun setFilterCategory(category: String?) = savedState.set(ATTR_SELECTED_CATEGORY, category)

    val filteredTools = tools.combine(filterCategory) { tools, category ->
        tools.filter { category == null || it.category == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())
    // endregion Filters

    // region Sync logic
    private val syncsRunning = MutableStateFlow(0)
    val isSyncRunning = syncsRunning.map { it > 0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    fun triggerSync(force: Boolean = false) {
        viewModelScope.launch {
            syncsRunning.value++
            syncService.suspendAndSyncTools(force)
            syncsRunning.value--
        }
    }

    init {
        triggerSync()
    }
    // endregion Sync logic
}
