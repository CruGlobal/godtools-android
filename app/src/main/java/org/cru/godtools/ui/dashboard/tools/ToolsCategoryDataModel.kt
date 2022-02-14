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
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao

private const val ATTR_SELECTED_CATEGORY = "selectedCategory"

@HiltViewModel
class ToolsCategoryDataModel @Inject constructor(dao: GodToolsDao, private val savedState: SavedStateHandle) :
    ViewModel() {
    var selectedCategory: String?
        get() = savedState.get(ATTR_SELECTED_CATEGORY)
        set(value) { savedState.set(ATTR_SELECTED_CATEGORY, value) }

    val selectedCategoryLiveData = savedState.getLiveData<String?>(ATTR_SELECTED_CATEGORY)

    private val tools = Query.select<Tool>().where(
        ToolTable.FIELD_TYPE.`in`(*constants(Tool.Type.TRACT, Tool.Type.ARTICLE, Tool.Type.CYOA))
            .and(ToolTable.FIELD_HIDDEN.ne(true))
    ).orderBy(ToolTable.COLUMN_DEFAULT_ORDER).getAsLiveData(dao)

    val categories = tools.map { t -> t.mapNotNull { it.category }.distinct() }

    val filteredTools = tools.combineWith(selectedCategoryLiveData) { tools, category ->
        tools.filter { category == null || it.category == category } ?: tools
    }
}
