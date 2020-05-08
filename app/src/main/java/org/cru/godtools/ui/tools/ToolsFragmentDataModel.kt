package org.cru.godtools.ui.tools

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.switchMap
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsLiveData
import org.cru.godtools.model.Tool
import org.cru.godtools.ui.tools.ToolsFragment.Companion.MODE_ADDED
import org.cru.godtools.ui.tools.ToolsFragment.Companion.MODE_AVAILABLE
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao
import javax.inject.Inject

class ToolsFragmentDataModel @Inject constructor(private val dao: GodToolsDao) : ViewModel() {
    val mode = MutableLiveData(MODE_ADDED)

    val tools = mode.distinctUntilChanged().switchMap { mode ->
        Query.select<Tool>()
            .where(ToolTable.FIELD_TYPE.ne(Tool.Type.UNKNOWN).let {
                when (mode) {
                    MODE_ADDED, MODE_AVAILABLE -> it.and(ToolTable.FIELD_ADDED.eq(mode == MODE_ADDED))
                    else -> it
                }
            })
            .orderBy(
                when (mode) {
                    MODE_ADDED -> "${ToolTable.COLUMN_ORDER},${ToolTable.COLUMN_DEFAULT_ORDER}"
                    else -> ToolTable.COLUMN_DEFAULT_ORDER
                }
            )
            .getAsLiveData(dao)
    }
}
