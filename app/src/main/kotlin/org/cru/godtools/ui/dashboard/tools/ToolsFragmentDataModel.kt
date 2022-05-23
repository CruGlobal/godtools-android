package org.cru.godtools.ui.dashboard.tools

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.db.Expression.Companion.constants
import org.ccci.gto.android.common.db.Query
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao

private const val ATTR_SELECTED_CATEGORY = "selectedCategory"

private val QUERY_TOOLS_BASE = Query.select<Tool>().where(
    ToolTable.FIELD_TYPE.`in`(*constants(Tool.Type.TRACT, Tool.Type.ARTICLE, Tool.Type.CYOA)) and
        (ToolTable.FIELD_HIDDEN ne true)
).orderBy(ToolTable.COLUMN_DEFAULT_ORDER)

private val QUERY_TOOLS = QUERY_TOOLS_BASE.join(ToolTable.SQL_JOIN_METATOOL.type("LEFT"))
    .andWhere(
        ToolTable.FIELD_META_TOOL.isNull() or
            (ToolTable.FIELD_CODE eq ToolTable.TABLE_META.field(ToolTable.COLUMN_DEFAULT_VARIANT))
    )
private val QUERY_TOOLS_SPOTLIGHT = QUERY_TOOLS_BASE.andWhere(ToolTable.FIELD_SPOTLIGHT eq true)

@HiltViewModel
class ToolsFragmentDataModel @Inject constructor(dao: GodToolsDao, savedState: SavedStateHandle) : ViewModel() {
    private val tools = dao.getLiveData(QUERY_TOOLS)

    val categories = tools.map { it.mapNotNull { it.category }.distinct() }
    val selectedCategory = savedState.getLiveData<String?>(ATTR_SELECTED_CATEGORY, null)

    val spotlightTools = dao.getLiveData(QUERY_TOOLS_SPOTLIGHT)
    val filteredTools = tools.combineWith(selectedCategory) { tools, category ->
        tools.filter { category == null || it.category == category }
    }
}
