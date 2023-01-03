package org.cru.godtools.db.repository

import kotlinx.coroutines.flow.Flow
import org.cru.godtools.model.Tool

interface ToolsRepository {
    fun findToolFlow(code: String): Flow<Tool?>
    fun getToolsFlow(): Flow<List<Tool>>
    fun getFavoriteToolsFlow(): Flow<List<Tool>>

    suspend fun pinTool(code: String)
    suspend fun unpinTool(code: String)

    suspend fun updateToolOrder(tools: List<String>)

    // TODO: temporary for testing
    fun insert(vararg tool: Tool)
}
