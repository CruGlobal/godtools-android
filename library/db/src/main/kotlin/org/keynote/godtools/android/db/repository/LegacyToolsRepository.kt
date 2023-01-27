package org.keynote.godtools.android.db.repository

import android.database.sqlite.SQLiteDatabase
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
import org.ccci.gto.android.common.db.findAsync
import org.ccci.gto.android.common.db.getAsFlow
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao

private val QUERY_TOOLS = Query.select<Tool>()
    .where(ToolTable.SQL_WHERE_IS_TOOL_TYPE)
    .orderBy(ToolTable.COLUMN_DEFAULT_ORDER)
private val QUERY_META_TOOLS = Query.select<Tool>()
    .where(ToolTable.FIELD_TYPE eq Tool.Type.META)

@Singleton
internal class LegacyToolsRepository @Inject constructor(private val dao: GodToolsDao) : ToolsRepository {
    private val coroutineScope = CoroutineScope(SupervisorJob())

    override suspend fun findTool(code: String) = dao.findAsync<Tool>(code).await()
    override suspend fun getTools() = dao.getAsync(QUERY_TOOLS).await()

    private val toolsCache = WeakLruCache<String, Flow<Tool?>>(15)
    override fun findToolFlow(code: String) = toolsCache.getOrPut(code) {
        dao.findAsFlow<Tool>(code)
            .shareIn(coroutineScope, SharingStarted.WhileSubscribed(replayExpirationMillis = REPLAY_EXPIRATION), 1)
    }

    private val toolsFlow = QUERY_TOOLS.getAsFlow(dao)
        .shareIn(coroutineScope, SharingStarted.WhileSubscribed(replayExpirationMillis = REPLAY_EXPIRATION), 1)
    override fun getToolsFlow() = toolsFlow
    override fun getMetaToolsFlow() = dao.getAsFlow(QUERY_META_TOOLS)

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
    override suspend fun updateToolViews(code: String, delta: Int) = dao.updateSharesDelta(code, delta)

    // region Sync Methods
    override fun storeToolFromSync(tool: Tool) {
        dao.updateOrInsert(
            tool, SQLiteDatabase.CONFLICT_REPLACE,
            ToolTable.COLUMN_CODE, ToolTable.COLUMN_TYPE, ToolTable.COLUMN_NAME, ToolTable.COLUMN_DESCRIPTION,
            ToolTable.COLUMN_CATEGORY, ToolTable.COLUMN_SHARES, ToolTable.COLUMN_BANNER,
            ToolTable.COLUMN_DETAILS_BANNER, ToolTable.COLUMN_DETAILS_BANNER_ANIMATION,
            ToolTable.COLUMN_DETAILS_BANNER_YOUTUBE, ToolTable.COLUMN_DEFAULT_ORDER, ToolTable.COLUMN_HIDDEN,
            ToolTable.COLUMN_SCREEN_SHARE_DISABLED, ToolTable.COLUMN_SPOTLIGHT, ToolTable.COLUMN_META_TOOL,
            ToolTable.COLUMN_DEFAULT_VARIANT
        )
    }
    // endregion Sync Methods

    // TODO: For testing only
    override fun insert(vararg tool: Tool) {
        tool.forEach { dao.insert(it) }
    }
}
