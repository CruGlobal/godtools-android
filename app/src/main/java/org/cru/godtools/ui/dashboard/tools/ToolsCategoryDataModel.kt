package org.cru.godtools.ui.dashboard.tools

import android.content.Context
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
import org.cru.godtools.base.ui.util.getCategory
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.Contract
import org.keynote.godtools.android.db.GodToolsDao
import java.util.*

@HiltViewModel
class ToolsCategoryDataModel @Inject constructor(dao: GodToolsDao, context: Context) : ViewModel() {

    val selectedCategory = MutableLiveData<String?>(null)

    private val allTools = Query.select<Tool>().where(
        Contract.ToolTable.FIELD_TYPE.`in`(*constants(Tool.Type.TRACT, Tool.Type.ARTICLE, Tool.Type.CYOA))
            .and(Contract.ToolTable.FIELD_HIDDEN.ne(true))
    ).getAsLiveData(dao)

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
