package org.cru.godtools.db.room.repository

import androidx.room.Dao
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.ccci.gto.android.common.androidx.room.changeFlow
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.room.GodToolsRoomDatabase
import org.cru.godtools.db.room.entity.ToolEntity
import org.cru.godtools.db.room.entity.partial.SyncTool
import org.cru.godtools.model.Resource
import org.cru.godtools.model.Tool

private val TOOL_TYPES = setOf(Tool.Type.TRACT, Tool.Type.CYOA, Tool.Type.ARTICLE)

@Dao
internal abstract class ToolsRoomRepository(private val db: GodToolsRoomDatabase) : ToolsRepository {
    private val dao get() = db.toolsDao

    override suspend fun findTool(code: String) = dao.findTool(code)?.toModel()
    override fun findResourceBlocking(code: String) = dao.findToolBlocking(code)?.toModel()
    override fun findToolFlow(code: String) = dao.findToolFlow(code).map { it?.toModel() }

    override suspend fun getResources() = dao.getResources().map { it.toModel() }
    override fun getResourcesBlocking() = dao.getResourcesBlocking().map { it.toModel() }
    override fun getResourcesFlow() = dao.getResourcesFlow().map { it.map { it.toModel() } }
    override suspend fun getTools() = dao.getToolsByType(TOOL_TYPES).map { it.toModel() }
    override fun getToolsFlow() = dao.getToolsByTypeFlow(TOOL_TYPES).map { it.map { it.toModel() } }
    override fun getMetaToolsFlow() = dao.getToolsByTypeFlow(setOf(Tool.Type.META)).map { it.map { it.toModel() } }
    override fun getLessonsFlow() = dao.getToolsByTypeFlow(setOf(Tool.Type.LESSON)).map { it.map { it.toModel() } }

    override fun toolsChangeFlow(): Flow<Any?> = db.changeFlow("tools")

    override suspend fun pinTool(code: String) = dao.updateIsAdded(code, true)
    override suspend fun unpinTool(code: String) = dao.updateIsAdded(code, false)

    @Transaction
    override suspend fun storeToolOrder(tools: List<String>) {
        dao.resetToolOrder()
        tools.forEachIndexed { i, tool ->
            dao.updateToolOrder(tool, i)
        }
    }

    override suspend fun updateToolViews(code: String, delta: Int) = dao.updateToolViews(code, delta)

    override suspend fun storeInitialResources(tools: Collection<Resource>) =
        dao.insertOrIgnoreTools(tools.map { ToolEntity(it) })

    override suspend fun storeToolsFromSync(tools: Collection<Tool>) = dao.upsertSyncTools(tools.map { SyncTool(it) })

    @Transaction
    override suspend fun deleteIfNotFavorite(code: String) {
        val tool = dao.findTool(code)?.takeUnless { it.isAdded } ?: return
        dao.delete(tool)
    }
}
