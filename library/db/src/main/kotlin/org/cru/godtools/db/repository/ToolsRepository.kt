package org.cru.godtools.db.repository

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import org.cru.godtools.model.Lesson
import org.cru.godtools.model.Resource
import org.cru.godtools.model.Tool

interface ToolsRepository {
    suspend fun findTool(code: String): Tool?
    @WorkerThread
    fun findResourceBlocking(code: String): Tool?
    suspend fun getResources(): List<Resource>
    @WorkerThread
    fun getResourcesBlocking(): List<Resource>
    suspend fun getTools(): List<Tool>

    fun findToolFlow(code: String): Flow<Tool?>
    fun getResourcesFlow(): Flow<List<Resource>>
    fun getToolsFlow(): Flow<List<Tool>>
    fun getMetaToolsFlow(): Flow<List<Tool>>
    fun getFavoriteToolsFlow(): Flow<List<Tool>>
    fun getLessonsFlow(): Flow<List<Lesson>>

    fun toolsChangeFlow(): Flow<Any?>

    suspend fun pinTool(code: String)
    suspend fun unpinTool(code: String)

    suspend fun storeToolOrder(tools: List<String>)
    suspend fun updateToolViews(code: String, delta: Int)

    // region Initial Content Methods
    suspend fun storeInitialResources(tools: Collection<Resource>)
    // endregion Initial Content Methods

    // region Sync Methods
    fun storeToolsFromSync(tools: Collection<Tool>) = tools.forEach { storeToolFromSync(it) }
    fun storeToolFromSync(tool: Tool)
    @WorkerThread
    fun deleteIfNotFavoriteBlocking(code: String)
    // endregion Sync Methods
}
