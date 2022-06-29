package org.keynote.godtools.android.db.repository

import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.db.Expression.Companion.constants
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsFlow
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao

@Singleton
class ToolsRepository @Inject constructor(private val dao: GodToolsDao) {
    private val coroutineScope = CoroutineScope(SupervisorJob())

    val favoriteTools = Query.select<Tool>()
        .where(
            ToolTable.FIELD_TYPE.`in`(*constants(Tool.Type.TRACT, Tool.Type.ARTICLE, Tool.Type.CYOA)) and
                ToolTable.FIELD_HIDDEN.ne(true) and
                ToolTable.FIELD_ADDED.eq(true)
        )
        .orderBy(ToolTable.SQL_ORDER_BY_ORDER)
        .getAsFlow(dao)
        .shareIn(coroutineScope, SharingStarted.WhileSubscribed(replayExpirationMillis = REPLAY_EXPIRATION), 1)

    suspend fun pinTool(code: String) {
        val tool = Tool().also {
            it.code = code
            it.isAdded = true
        }
        withContext(dao.coroutineDispatcher) { dao.update(tool, ToolTable.COLUMN_ADDED) }
    }

    suspend fun unpinTool(code: String) {
        val tool = Tool().also {
            it.code = code
            it.isAdded = false
        }
        withContext(dao.coroutineDispatcher) { dao.update(tool, ToolTable.COLUMN_ADDED) }
    }

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
