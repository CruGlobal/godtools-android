package org.cru.godtools.db.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.cru.godtools.db.room.entity.ToolEntity
import org.cru.godtools.db.room.entity.partial.SyncTool
import org.cru.godtools.model.Tool

@Dao
internal interface ToolsDao {
    @Query("SELECT * FROM tools WHERE code = :code")
    suspend fun findTool(code: String): ToolEntity?
    @Query("SELECT * FROM tools WHERE code = :code")
    fun findToolBlocking(code: String): ToolEntity?
    @Query("SELECT * FROM tools WHERE code = :code")
    fun findToolFlow(code: String): Flow<ToolEntity?>

    @Query("SELECT * FROM tools")
    suspend fun getResources(): List<ToolEntity>
    @Query("SELECT * FROM tools")
    fun getResourcesBlocking(): List<ToolEntity>
    @Query("SELECT * FROM tools")
    fun getResourcesFlow(): Flow<List<ToolEntity>>
    @Query("SELECT * FROM tools WHERE type in (:types)")
    suspend fun getToolsByType(types: Collection<Tool.Type>): List<ToolEntity>
    @Query("SELECT * FROM tools WHERE type in (:types)")
    fun getToolsByTypeFlow(types: Collection<Tool.Type>): Flow<List<ToolEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnoreTools(tools: Collection<ToolEntity>)
    @Upsert(entity = ToolEntity::class)
    fun upsert(tool: SyncTool)
    @Query("UPDATE tools SET isAdded = :isAdded WHERE code = :code")
    suspend fun updateIsAdded(code: String, isAdded: Boolean)
    @Query("UPDATE tools SET `order` = ${Int.MAX_VALUE}")
    fun resetToolOrder()
    @Query("UPDATE tools SET `order` = :order WHERE code = :code")
    fun updateToolOrder(code: String, order: Int)
    @Query("UPDATE tools SET pendingShares = pendingShares + :views WHERE code = :code")
    suspend fun updateToolViews(code: String, views: Int)
    @Delete
    fun deleteBlocking(tool: ToolEntity)
}
