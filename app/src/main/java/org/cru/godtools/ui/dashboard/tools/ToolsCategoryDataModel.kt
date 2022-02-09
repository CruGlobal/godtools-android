package org.cru.godtools.ui.dashboard.tools

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.db.Expression.Companion.constants
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsLiveData
import org.cru.godtools.base.ui.util.getCategory
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.Contract
import org.keynote.godtools.android.db.GodToolsDao

@HiltViewModel
class ToolsCategoryDataModel @Inject constructor(dao: GodToolsDao, context: Application) : AndroidViewModel(context) {

    val selectedCategory = MutableLiveData<String?>(null)

    private val allTools = Query.select<Tool>().where(
        Contract.ToolTable.FIELD_TYPE.`in`(*constants(Tool.Type.TRACT, Tool.Type.ARTICLE, Tool.Type.CYOA))
            .and(Contract.ToolTable.FIELD_HIDDEN.ne(true))
    ).getAsLiveData(dao)

    val categories = allTools.map {
        it.mapNotNull { tool ->
            tool.category?.let { category -> Pair(category, tool.getCategory(getApplication(), Locale.getDefault())) }
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
