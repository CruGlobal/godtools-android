package org.cru.godtools.ui.dashboard.tools

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.db.Expression.Companion.constants
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsLiveData
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.Contract
import org.keynote.godtools.android.db.GodToolsDao

@HiltViewModel
class ToolsCategoryDataModel @Inject constructor(dao: GodToolsDao) : ViewModel() {

    val selectedCategory = MutableLiveData<String?>(null)

    private val where =
        Contract.ToolTable.FIELD_TYPE.`in`(*constants(Tool.Type.TRACT, Tool.Type.ARTICLE, Tool.Type.CYOA))
            .and(Contract.ToolTable.FIELD_HIDDEN.ne(true))
    private val allTools = Query.select<Tool>()
        .where(where)
        .getAsLiveData(dao)

    val categories = allTools.map {
        it.mapNotNull { tool ->
            tool.category
        }.distinct()
    }

    val viewTools: LiveData<List<Tool>> = selectedCategory.combineWith(allTools) { selectedCategory, tools ->
        if (selectedCategory == null) {
            return@combineWith tools
        } else {
            return@combineWith tools.filter { it.category == selectedCategory }
        }
    }
}
