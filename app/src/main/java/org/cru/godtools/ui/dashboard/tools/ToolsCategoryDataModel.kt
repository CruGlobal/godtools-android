package org.cru.godtools.ui.dashboard.tools

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.db.Expression.Companion.constants
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsLiveData
import org.cru.godtools.base.ui.util.getLocalStringFromToolCategory
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.Contract
import org.keynote.godtools.android.db.GodToolsDao

@HiltViewModel
class ToolsCategoryDataModel @Inject constructor(dao: GodToolsDao, context: Application) : AndroidViewModel(context) {

    val selectedCategory = MutableLiveData<String?>(null)

    private val allTools = Query.select<Tool>().where(
        Contract.ToolTable.FIELD_TYPE.`in`(*constants(Tool.Type.TRACT, Tool.Type.ARTICLE, Tool.Type.CYOA))
            .and(Contract.ToolTable.FIELD_HIDDEN.ne(true))
    ).orderBy(Contract.ToolTable.COLUMN_DEFAULT_ORDER).getAsLiveData(dao)

    val categories = allTools.map {
        it.mapNotNull { tool -> tool.category }.distinct()
    }

    fun getFormattedCategory(category: String) = category.getLocalStringFromToolCategory(getApplication())

    val viewTools: LiveData<List<Tool>> = allTools.combineWith(selectedCategory) { tools, selectedCategory ->
        tools.filter { tool -> selectedCategory?.let { tool.category == it } ?: true }
    }
}
