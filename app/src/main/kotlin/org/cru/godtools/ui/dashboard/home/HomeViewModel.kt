package org.cru.godtools.ui.dashboard.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsFlow
import org.cru.godtools.base.Settings
import org.cru.godtools.model.Tool
import org.cru.godtools.sync.GodToolsSyncService
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao
import org.keynote.godtools.android.db.repository.ToolsRepository

@HiltViewModel
class HomeViewModel @Inject constructor(
    dao: GodToolsDao,
    settings: Settings,
    private val syncService: GodToolsSyncService,
    toolsRepository: ToolsRepository
) : ViewModel() {
    val showTutorialFeaturesBanner = settings.isFeatureDiscoveredFlow(Settings.FEATURE_TUTORIAL_FEATURES)
        .map { !it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val spotlightLessons = Query.select<Tool>()
        .where(ToolTable.FIELD_TYPE.eq(Tool.Type.LESSON) and ToolTable.FIELD_SPOTLIGHT.eq(true))
        .orderBy(ToolTable.COLUMN_DEFAULT_ORDER)
        .getAsFlow(dao)
        .map { it.mapNotNull { it.code } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val favoriteTools = toolsRepository.favoriteTools
        .map { it.mapNotNull { it.code }.take(3) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

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
