package org.cru.godtools.ui.dashboard.tools

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.db.Expression.Companion.constants
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsLiveData
import org.cru.godtools.base.Settings
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao

private const val ATTR_SELECTED_CATEGORY = "selectedCategory"

@HiltViewModel
class ToolsFragmentDataModel @Inject constructor(dao: GodToolsDao, savedState: SavedStateHandle) : ViewModel() {
    private val tools = Query.select<Tool>().where(
        ToolTable.FIELD_TYPE.`in`(*constants(Tool.Type.TRACT, Tool.Type.ARTICLE, Tool.Type.CYOA))
            .and(ToolTable.FIELD_HIDDEN.ne(true))
    ).orderBy(ToolTable.COLUMN_DEFAULT_ORDER).getAsLiveData(dao)

    val categories = tools.map { it.mapNotNull { it.category }.distinct() }
    val selectedCategory = savedState.getLiveData<String?>(ATTR_SELECTED_CATEGORY, null)

    val spotlightTools = tools.map { it.filter { t -> t.isSpotlight } }
    val filteredTools = tools.combineWith(selectedCategory) { tools, category ->
        tools.filter { category == null || it.category == category }
    }
}
