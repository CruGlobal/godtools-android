package org.cru.godtools.db.room.repository

import androidx.room.Dao
import androidx.room.Transaction
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.ccci.gto.android.common.androidx.room.changeFlow
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.room.GodToolsRoomDatabase
import org.cru.godtools.db.room.entity.ToolEntity
import org.cru.godtools.db.room.entity.partial.SyncTool
import org.cru.godtools.model.Tool
import org.cru.godtools.model.trackChanges

@Dao
internal abstract class ToolsRoomRepository(private val db: GodToolsRoomDatabase) : ToolsRepository {
    private val dao get() = db.toolsDao

    override suspend fun findTool(code: String) = dao.findTool(code)?.toModel()
    override fun findToolFlow(code: String) = dao.findToolFlow(code).map { it?.toModel() }

    override suspend fun getAllTools() = dao.getTools().map { it.toModel() }
    override suspend fun getToolsByType(types: Collection<Tool.Type>) = dao.getToolsByType(types).map { it.toModel() }
    override fun getAllToolsFlow() = dao.getToolsFlow().map { it.map { it.toModel() } }
    override fun getToolsFlowByType(types: Collection<Tool.Type>) =
        dao.getToolsByTypeFlow(types).map { it.map { it.toModel() } }
    override fun getToolsFlowForLanguage(locale: Locale) =
        dao.getToolsFlowByTypeAndLanguage(Tool.Type.NORMAL_TYPES, locale).map { it.map { it.toModel() } }
    override fun getDownloadedToolsFlowByTypesAndLanguage(types: Collection<Tool.Type>, locale: Locale) =
        dao.getDownloadedToolsFlowByTypeAndLanguage(types, locale).map { it.map { it.toModel() } }

    override fun toolsChangeFlow(): Flow<Any?> = db.changeFlow("tools")

    @Transaction
    override suspend fun pinTool(code: String, trackChanges: Boolean) {
        val tool = dao.findToolFavorite(code) ?: return
        if (trackChanges) tool.isTrackingChanges = true
        tool.isFavorite = true
        dao.update(tool)
    }
    @Transaction
    override suspend fun unpinTool(code: String) {
        val tool = dao.findToolFavorite(code) ?: return
        tool.trackChanges { it.isFavorite = false }
        dao.update(tool)
    }

    @Transaction
    override suspend fun storeToolOrder(tools: List<String>) {
        dao.resetToolOrder()
        tools.forEachIndexed { i, tool ->
            dao.updateToolOrder(tool, i)
        }
    }

    override suspend fun updateToolViews(code: String, delta: Int) = dao.updateToolViews(code, delta)

    override suspend fun storeInitialTools(tools: Collection<Tool>) =
        dao.insertOrIgnoreTools(tools.map { ToolEntity(it) })

    // region Sync Methods
    override suspend fun storeToolsFromSync(tools: Collection<Tool>) = dao.upsertSyncTools(tools.map { SyncTool(it) })

    @Transaction
    override suspend fun storeFavoriteToolsFromSync(tools: Collection<Tool>) {
        val favorites = tools.mapNotNullTo(mutableSetOf()) { it.code }
        val toolFavorites = dao.getToolFavorites().onEach {
            val isFavorite = it.code in favorites
            if (isFavorite == it.isFavorite) it.clearChanged(Tool.ATTR_IS_FAVORITE)
            if (!it.isFieldChanged(Tool.ATTR_IS_FAVORITE)) it.isFavorite = isFavorite
        }
        dao.updateToolFavorites(toolFavorites)
    }

    @Transaction
    override suspend fun deleteIfNotFavorite(code: String) {
        val tool = dao.findTool(code)?.takeUnless { it.isFavorite } ?: return
        dao.delete(tool)
    }
    // endregion Sync Methods
}
