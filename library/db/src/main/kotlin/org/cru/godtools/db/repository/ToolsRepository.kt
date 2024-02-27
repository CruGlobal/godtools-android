package org.cru.godtools.db.repository

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.cru.godtools.model.Tool

interface ToolsRepository {
    suspend fun findTool(code: String): Tool?
    fun findToolFlow(code: String): Flow<Tool?>

    suspend fun getAllTools(): List<Tool>
    suspend fun getToolsByType(types: Collection<Tool.Type>): List<Tool>
    suspend fun getNormalTools() = getToolsByType(Tool.Type.NORMAL_TYPES)

    fun getAllToolsFlow(): Flow<List<Tool>>
    fun getToolsFlowByType(vararg types: Tool.Type) = getToolsFlowByType(types.toSet())
    fun getToolsFlowByType(types: Collection<Tool.Type>): Flow<List<Tool>>
    fun getDownloadedToolsFlowByTypesAndLanguage(types: Collection<Tool.Type>, locale: Locale): Flow<List<Tool>>
    fun getMetaToolsFlow() = getToolsFlowByType(Tool.Type.META)
    fun getLessonsFlow() = getToolsFlowByType(Tool.Type.LESSON)
    fun getNormalToolsFlow() = getToolsFlowByType(Tool.Type.NORMAL_TYPES)
    fun getNormalToolsFlowByLanguage(locale: Locale): Flow<List<Tool>>
    fun getFavoriteToolsFlow() = getNormalToolsFlow()
        .map { it.filter { it.isFavorite }.sortedWith(Tool.COMPARATOR_FAVORITE_ORDER) }

    fun toolsChangeFlow(): Flow<Any?>

    suspend fun pinTool(code: String, trackChanges: Boolean = true)
    suspend fun unpinTool(code: String)

    suspend fun storeToolOrder(tools: List<String>)
    suspend fun updateToolViews(code: String, delta: Int)

    // region Initial Content Methods
    suspend fun storeInitialTools(tools: Collection<Tool>)
    // endregion Initial Content Methods

    // region Sync Methods
    suspend fun storeToolsFromSync(tools: Collection<Tool>)
    suspend fun storeFavoriteToolsFromSync(tools: Collection<Tool>)
    suspend fun deleteIfNotFavorite(code: String)
    // endregion Sync Methods
}

@Composable
fun ToolsRepository.produceToolState(toolCode: String?) =
    remember(toolCode) { toolCode?.let { findToolFlow(it) } ?: flowOf(null) }.collectAsState(null)
