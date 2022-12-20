package org.keynote.godtools.android.db.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import org.ccci.gto.android.common.androidx.collection.WeakLruCache
import org.ccci.gto.android.common.androidx.collection.getOrPut
import org.ccci.gto.android.common.db.Expression.Companion.constants
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.findAsFlow
import org.ccci.gto.android.common.db.getAsFlow
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao

@Singleton
class ToolsRepository @Inject constructor(private val dao: GodToolsDao) {
    private val coroutineScope = CoroutineScope(SupervisorJob())

    private val toolsCache = WeakLruCache<String, Flow<Tool?>>(15)
    fun getToolFlow(code: String) = toolsCache.getOrPut(code) {
        dao.findAsFlow<Tool>(code)
            .shareIn(coroutineScope, SharingStarted.WhileSubscribed(replayExpirationMillis = REPLAY_EXPIRATION), 1)
    }

    val favoriteTools = Query.select<Tool>()
        .where(
            ToolTable.FIELD_TYPE.`in`(*constants(Tool.Type.TRACT, Tool.Type.ARTICLE, Tool.Type.CYOA)) and
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
        dao.updateAsync(tool, ToolTable.COLUMN_ADDED).await()
    }

    suspend fun unpinTool(code: String) {
        val tool = Tool().also {
            it.code = code
            it.isAdded = false
        }
        dao.updateAsync(tool, ToolTable.COLUMN_ADDED).await()
    }

    suspend fun updateToolOrder(tools: List<String>) {
        dao.transactionAsync(exclusive = false) {
            val tool = Tool()
            dao.update(tool, null, ToolTable.COLUMN_ORDER)

            // set order for each specified tool
            tools.forEachIndexed { index, code ->
                tool.order = index
                dao.update(tool, ToolTable.FIELD_CODE.eq(code), ToolTable.COLUMN_ORDER)
            }
        }.await()
    }
}
