package org.cru.godtools.ui.dashboard.tools

import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Tool

internal class FeaturedToolsFlowProducer @Inject constructor(
    private val settings: Settings,
    private val toolsRepository: ToolsRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getFlow(locale: Locale): Flow<List<Tool>> = settings.getPersonalizationCountryFlow()
        .flatMapLatest { country ->
            when (country) {
                // the featured tools section is hidden when no country has been selected
                null -> flowOf(emptyList())

                else -> toolsRepository.getFeaturedToolsFlow(locale, country)
            }
        }
        .distinctUntilChanged()
        .map { it.filterNot { it.isHidden } }
}
