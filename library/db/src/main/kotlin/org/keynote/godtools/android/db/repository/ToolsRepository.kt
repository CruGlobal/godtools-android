package org.keynote.godtools.android.db.repository

import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.launch
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao

@Singleton
class ToolsRepository @Inject constructor(private val dao: GodToolsDao) {
    @WorkerThread
    private fun updateToolOrder(vararg tools: Long) {
        val tool = Tool()
        dao.transaction(exclusive = false) {
            dao.update(tool, null, ToolTable.COLUMN_ORDER)

            // set order for each specified tool
            tools.forEachIndexed { index, toolId ->
                tool.order = index
                dao.update(tool, ToolTable.FIELD_ID.eq(toolId), ToolTable.COLUMN_ORDER)
            }
        }
    }

    @AnyThread
    fun updateToolOrderAsync(vararg tools: Long) = dao.coroutineScope.launch { updateToolOrder(*tools) }
}
