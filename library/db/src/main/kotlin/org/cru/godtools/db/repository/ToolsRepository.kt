package org.cru.godtools.db.repository

import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.cru.godtools.model.Resource
import org.cru.godtools.model.Tool

interface ToolsRepository {
    suspend fun findTool(code: String): Tool?
    fun findToolFlow(code: String): Flow<Tool?>

    suspend fun getResources(): List<Resource>
    suspend fun getToolsByType(types: Collection<Tool.Type>): List<Tool>
    suspend fun getNormalTools() = getToolsByType(Tool.Type.NORMAL_TYPES)

    fun getResourcesFlow(): Flow<List<Resource>>
    fun getToolsFlowByType(vararg types: Tool.Type) = getToolsFlowByType(types.toSet())
    fun getToolsFlowByType(types: Collection<Tool.Type>): Flow<List<Tool>>
    fun getToolsFlowForLanguage(locale: Locale): Flow<List<Tool>>
    fun getMetaToolsFlow() = getToolsFlowByType(Tool.Type.META)
    fun getLessonsFlow() = getToolsFlowByType(Tool.Type.LESSON)
    fun getNormalToolsFlow() = getToolsFlowByType(Tool.Type.NORMAL_TYPES)
    fun getFavoriteToolsFlow() = getNormalToolsFlow()
        .map { it.filter { it.isFavorite }.sortedWith(Tool.COMPARATOR_FAVORITE_ORDER) }

    fun toolsChangeFlow(): Flow<Any?>

    suspend fun pinTool(code: String, trackChanges: Boolean = true)
    suspend fun unpinTool(code: String)

    suspend fun storeToolOrder(tools: List<String>)
    suspend fun updateToolViews(code: String, delta: Int)

    // region Initial Content Methods
    suspend fun storeInitialResources(tools: Collection<Resource>)
    // endregion Initial Content Methods

    // region Sync Methods
    suspend fun storeToolsFromSync(tools: Collection<Tool>)
    suspend fun storeFavoriteToolsFromSync(tools: Collection<Tool>)
    suspend fun deleteIfNotFavorite(code: String)
    // endregion Sync Methods
}
