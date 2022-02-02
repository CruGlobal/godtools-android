package org.cru.godtools.ui.dashboard.tools

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.androidx.lifecycle.switchCombineWith
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsLiveData
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.model.Tool
import org.cru.godtools.tool.model.Category
import org.keynote.godtools.android.db.GodToolsDao
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ToolsCategoryDataModel @Inject constructor(dao: GodToolsDao) : ViewModel() {

    private val categoryTools = Query.select<Tool>().getAsLiveData(dao)

    val categories = categoryTools.map {
        it.mapNotNull { tool ->
            tool.category
        }.distinct()
    }

}
