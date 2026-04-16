package org.cru.godtools.ui.dashboard.tools

import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Tool

internal class FilteredToolsFlowProducer @Inject constructor(private val toolsRepository: ToolsRepository) {
    fun getFlow(category: String? = null, language: Locale? = null): Flow<List<Tool>> {
        val baseFlow = when (language) {
            null -> toolsRepository.getNormalToolsFlow()
            else -> toolsRepository.getNormalToolsFlowByLanguage(language)
        }

        return baseFlow.map {
            it
                .filterNot { it.isHidden }
                .filter { category == null || it.category == category }
                .sortedBy { it.defaultOrder }
        }
    }
}
