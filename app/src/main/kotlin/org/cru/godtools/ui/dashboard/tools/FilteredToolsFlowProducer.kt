package org.cru.godtools.ui.dashboard.tools

import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.ccci.gto.android.common.kotlin.coroutines.flow.combineTransformLatest
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Tool
import org.cru.godtools.ui.dashboard.tools.ToolsPresenter.UiState.Mode

internal class FilteredToolsFlowProducer @Inject constructor(
    private val settings: Settings,
    private val toolsRepository: ToolsRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getFlow(mode: Mode, category: String? = null, language: Locale? = null): Flow<List<Tool>> {
        val baseFlow = when {
            mode == Mode.PERSONALIZATION -> {
                val languageFlow = if (language != null) flowOf(language) else settings.appLanguageFlow
                val fallbackFlow = languageFlow.flatMapLatest { toolsRepository.getPersonalizedToolsFlow(it, null) }

                languageFlow
                    .combineTransformLatest(settings.getPersonalizationCountryFlow()) { language, country ->
                        emitAll(toolsRepository.getPersonalizedToolsFlow(language, country))
                    }
                    .combine(fallbackFlow) { personalized, fallback -> personalized.ifEmpty { fallback } }
                    .distinctUntilChanged()
            }

            language != null -> toolsRepository.getNormalToolsFlowByLanguage(language)
                .map { it.sortedBy { it.defaultOrder } }

            else -> toolsRepository.getNormalToolsFlow().map { it.sortedBy { it.defaultOrder } }
        }

        return baseFlow.map {
            it
                .filterNot { it.isHidden }
                .filter { category == null || it.category == category }
        }
    }
}
