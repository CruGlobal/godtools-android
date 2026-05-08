package org.cru.godtools.ui.dashboard.lessons

import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Tool
import org.cru.godtools.ui.dashboard.lessons.LessonsPresenter.UiState.Mode

internal class LessonsFlowProducer @Inject constructor(
    private val settings: Settings,
    private val toolsRepository: ToolsRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getFlow(mode: Mode, locale: Locale): Flow<List<Tool>> {
        val baseFlow = when (mode) {
            Mode.PERSONALIZATION -> settings.getPersonalizationCountryFlow()
                .flatMapLatest { toolsRepository.getPersonalizedLessonsFlow(locale, it) }
                .combine(toolsRepository.getPersonalizedLessonsFlow(locale, null)) { lessons, fallback ->
                    lessons.ifEmpty { fallback }
                }
                .distinctUntilChanged()

            Mode.ALL_LESSONS -> toolsRepository.getLessonsFlowByLanguage(locale).map { it.sortedBy { it.defaultOrder } }
        }

        return baseFlow.map { it.filterNot { it.isHidden } }
    }
}
