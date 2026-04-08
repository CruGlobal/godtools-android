package org.cru.godtools.db.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Update
import androidx.room.Upsert
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import org.cru.godtools.db.room.entity.PersonalizedToolOrderEntity
import org.cru.godtools.db.room.entity.ToolEntity
import org.cru.godtools.db.room.entity.partial.SyncPersonalizedTool
import org.cru.godtools.db.room.entity.partial.SyncTool
import org.cru.godtools.db.room.entity.partial.ToolFavorite
import org.cru.godtools.model.Tool

@Dao
internal interface ToolsDao {
    @Query("SELECT * FROM tools WHERE code = :code")
    suspend fun findTool(code: String): ToolEntity?
    @Query("SELECT * FROM tools WHERE code = :code")
    @RewriteQueriesToDropUnusedColumns
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
    @RewriteQueriesToDropUnusedColumns
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
    @Query("UPDATE tools SET primaryLocale = :primary, parallelLocale = :parallel WHERE code = :code")
    suspend fun updateToolLocales(code: String, primary: Locale?, parallel: Locale?)
    @Query("UPDATE tools SET progress = :progress, progressLastPageId = :lastPageId WHERE code = :code")
    suspend fun updateToolProgress(code: String, progress: Double?, lastPageId: String?)
    @Query("UPDATE tools SET pendingShares = pendingShares + :views WHERE code = :code")
    suspend fun updateToolViews(code: String, views: Int)
    @Delete
    suspend fun delete(tool: ToolEntity)

    // region Personalized Tools
    @Query(
        """
            SELECT t.*
            FROM personalized_tool_order AS o JOIN tools AS t ON t.code = o.tool
            WHERE o.locale = :locale AND o.country = :country AND t.type IN (:type)
            ORDER BY o.`order` ASC
        """
    )
    fun getPersonalizedToolsFlow(locale: Locale, country: String, vararg type: Tool.Type): Flow<List<ToolEntity>>

    @Upsert(entity = ToolEntity::class)
    suspend fun upsertToolsPersonalized(tools: Collection<SyncPersonalizedTool>)
    @Upsert
    suspend fun upsertPersonalizedToolOrder(order: Collection<PersonalizedToolOrderEntity>)

    @Query("DELETE FROM personalized_tool_order WHERE locale = :locale AND country = :country")
    suspend fun resetPersonalizedToolOrder(locale: Locale, country: String)
    // endregion Personalized Tools
}
