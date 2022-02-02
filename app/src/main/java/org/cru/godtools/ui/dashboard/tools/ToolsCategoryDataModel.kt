package org.cru.godtools.ui.dashboard.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsLiveData
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.GodToolsDao

@HiltViewModel
class ToolsCategoryDataModel @Inject constructor(dao: GodToolsDao) : ViewModel() {

    private val categoryTools = Query.select<Tool>().getAsLiveData(dao)

    val categories = categoryTools.map {
        it.mapNotNull { tool ->
            tool.category
        }.distinct()
    }
}
