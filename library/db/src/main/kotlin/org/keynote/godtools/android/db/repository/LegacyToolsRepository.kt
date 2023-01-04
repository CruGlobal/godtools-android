package org.keynote.godtools.android.db.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import org.ccci.gto.android.common.androidx.collection.WeakLruCache
import org.ccci.gto.android.common.androidx.collection.getOrPut
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.findAsFlow
import org.ccci.gto.android.common.db.getAsFlow
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao

@Singleton
internal class LegacyToolsRepository @Inject constructor(private val dao: GodToolsDao) : ToolsRepository {
    private val coroutineScope = CoroutineScope(SupervisorJob())

    private val toolsCache = WeakLruCache<String, Flow<Tool?>>(15)
    override fun findToolFlow(code: String) = toolsCache.getOrPut(code) {
        dao.findAsFlow<Tool>(code)
            .shareIn(coroutineScope, SharingStarted.WhileSubscribed(replayExpirationMillis = REPLAY_EXPIRATION), 1)
    }

    private val toolsFlow = Query.select<Tool>()
        .where(ToolTable.SQL_WHERE_IS_TOOL_TYPE)
        .orderBy(ToolTable.COLUMN_DEFAULT_ORDER)
        .getAsFlow(dao)
        .shareIn(coroutineScope, SharingStarted.WhileSubscribed(replayExpirationMillis = REPLAY_EXPIRATION), 1)
    override fun getToolsFlow() = toolsFlow

    private val favoriteTools = toolsFlow
        .map { it.filter { it.isAdded }.sortedWith(Tool.COMPARATOR_FAVORITE_ORDER) }
        .shareIn(coroutineScope, SharingStarted.WhileSubscribed(replayExpirationMillis = REPLAY_EXPIRATION), 1)
    override fun getFavoriteToolsFlow() = favoriteTools

    override suspend fun pinTool(code: String) {
        val tool = Tool().also {
            it.code = code
            it.isAdded = true
        }
        dao.updateAsync(tool, ToolTable.COLUMN_ADDED).await()
    }

    override suspend fun unpinTool(code: String) {
        val tool = Tool().also {
            it.code = code
            it.isAdded = false
        }
        dao.updateAsync(tool, ToolTable.COLUMN_ADDED).await()
    }

    override suspend fun updateToolOrder(tools: List<String>) {
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

    // TODO: For testing only
    override fun insert(vararg tool: Tool) {
        tool.forEach { dao.insert(it) }
    }
}
