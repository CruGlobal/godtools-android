package org.cru.godtools.ui.dashboard

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsLiveData
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao

@HiltViewModel
class DashboardDataModel @Inject constructor(dao: GodToolsDao) : ViewModel() {
    val lessons = Query.select<Tool>()
        .where(ToolTable.FIELD_TYPE.eq(Tool.Type.LESSON))
        .limit(1)
        .getAsLiveData(dao)
}
