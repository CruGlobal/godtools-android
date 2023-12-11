package org.cru.godtools.db.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import org.cru.godtools.db.room.entity.ToolEntity
import org.cru.godtools.db.room.entity.partial.SyncTool
import org.cru.godtools.db.room.entity.partial.ToolFavorite
import org.cru.godtools.model.Tool

@Dao
internal interface ToolsDao {
    @Query("SELECT * FROM tools WHERE code = :code")
    suspend fun findTool(code: String): ToolEntity?
    @Query("SELECT * FROM tools WHERE code = :code")
    suspend fun findToolFavorite(code: String): ToolFavorite?
    @Query("SELECT * FROM tools WHERE apiId = :apiId")
    fun findToolByApiIdBlocking(apiId: Long): ToolEntity?
    @Query("SELECT * FROM tools WHERE code = :code")
    fun findToolFlow(code: String): Flow<ToolEntity?>

    @Query("SELECT * FROM tools")
    suspend fun getTools(): List<ToolEntity>
    @Query("SELECT * FROM tools")
    fun getToolsFlow(): Flow<List<ToolEntity>>
    @Query("SELECT * FROM tools WHERE type in (:types)")
    suspend fun getToolsByType(types: Collection<Tool.Type>): List<ToolEntity>
    @Query("SELECT * FROM tools WHERE type in (:types)")
    fun getToolsByTypeFlow(types: Collection<Tool.Type>): Flow<List<ToolEntity>>
    @Query(
        "SELECT * FROM tools WHERE type in (:types) AND code IN (SELECT tool FROM translations WHERE locale = :locale)"
    )
    fun getToolsFlowByTypeAndLanguage(types: Collection<Tool.Type>, locale: Locale): Flow<List<ToolEntity>>
    @Query(
        """
            SELECT * FROM tools
            WHERE
                type in (:types) AND
                code IN (SELECT tool FROM translations WHERE locale = :locale AND isDownloaded = 1)
        """
    )
    fun getDownloadedToolsFlowByTypeAndLanguage(types: Collection<Tool.Type>, locale: Locale): Flow<List<ToolEntity>>
    @Query("SELECT * FROM tools")
    suspend fun getToolFavorites(): List<ToolFavorite>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnoreTools(tools: Collection<ToolEntity>)
    @Upsert(entity = ToolEntity::class)
    suspend fun upsertSyncTools(tools: Collection<SyncTool>)
    @Update(entity = ToolEntity::class)
    suspend fun update(tool: ToolFavorite)
    @Update(entity = ToolEntity::class)
    suspend fun updateToolFavorites(tools: Collection<ToolFavorite>)
    @Query("UPDATE tools SET `order` = ${Int.MAX_VALUE}")
    suspend fun resetToolOrder()
    @Query("UPDATE tools SET `order` = :order WHERE code = :code")
    suspend fun updateToolOrder(code: String, order: Int)
    @Query("UPDATE tools SET pendingShares = pendingShares + :views WHERE code = :code")
    suspend fun updateToolViews(code: String, views: Int)
    @Delete
    suspend fun delete(tool: ToolEntity)
}
