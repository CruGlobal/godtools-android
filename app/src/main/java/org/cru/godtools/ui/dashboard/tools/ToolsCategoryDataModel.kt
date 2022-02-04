package org.cru.godtools.ui.dashboard.tools

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import org.ccci.gto.android.common.androidx.lifecycle.ImmutableLiveData
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import javax.inject.Inject
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsLiveData
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.GodToolsDao

@HiltViewModel
class ToolsCategoryDataModel @Inject constructor(dao: GodToolsDao) : ViewModel() {

    val selectedCategory = MutableLiveData<String?>(null)

    private val categoryTools = Query.select<Tool>().getAsLiveData(dao)

    val categories = categoryTools.map {
        it.mapNotNull { tool ->
            tool.category
        }.distinct()
    }

    val viewTools: LiveData<List<Tool>> = selectedCategory.combineWith(categoryTools) { selectedCategory, tools ->
        if (selectedCategory == null) {
            return@combineWith tools
        } else {
            return@combineWith tools.filter { it.category == selectedCategory }
        }
    }
}
