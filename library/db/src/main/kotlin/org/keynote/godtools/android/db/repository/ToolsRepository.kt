package org.keynote.godtools.android.db.repository

import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.db.Expression
import org.ccci.gto.android.common.db.Expression.Companion.constants
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsFlow
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao

@Singleton
class ToolsRepository @Inject constructor(private val dao: GodToolsDao) {
    val favoriteTools get() = Query.select<Tool>()
        .where(
            ToolTable.FIELD_ADDED.eq(true) and
                ToolTable.FIELD_HIDDEN.ne(true) and
                ToolTable.FIELD_TYPE.`in`(*constants(Tool.Type.TRACT, Tool.Type.ARTICLE, Tool.Type.CYOA))
        )
        .orderBy(ToolTable.SQL_ORDER_BY_ORDER)
        .getAsFlow(dao)

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
