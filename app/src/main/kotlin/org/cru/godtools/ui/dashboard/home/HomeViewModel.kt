package org.cru.godtools.ui.dashboard.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsFlow
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao

@HiltViewModel
class HomeViewModel @Inject constructor(dao: GodToolsDao) : ViewModel() {
    val spotlightLessons = Query.select<Tool>()
        .where(ToolTable.FIELD_TYPE.eq(Tool.Type.LESSON) and ToolTable.FIELD_SPOTLIGHT.eq(true))
        .orderBy(ToolTable.COLUMN_DEFAULT_ORDER)
        .getAsFlow(dao)
        .map { it.mapNotNull { it.code } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
}
