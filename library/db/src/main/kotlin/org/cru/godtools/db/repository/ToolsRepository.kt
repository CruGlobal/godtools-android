package org.cru.godtools.db.repository

import kotlinx.coroutines.flow.Flow
import org.cru.godtools.model.Tool

interface ToolsRepository {
    suspend fun findTool(code: String): Tool?
    suspend fun getTools(): List<Tool>

    fun findToolFlow(code: String): Flow<Tool?>
    fun getToolsFlow(): Flow<List<Tool>>
    fun getMetaToolsFlow(): Flow<List<Tool>>
    fun getFavoriteToolsFlow(): Flow<List<Tool>>

    suspend fun pinTool(code: String)
    suspend fun unpinTool(code: String)

    suspend fun updateToolOrder(tools: List<String>)
    suspend fun updateToolViews(code: String, delta: Int)

    // TODO: temporary for testing
    fun insert(vararg tool: Tool)
}
