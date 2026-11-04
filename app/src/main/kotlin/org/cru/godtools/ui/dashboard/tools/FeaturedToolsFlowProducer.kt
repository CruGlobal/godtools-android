package org.cru.godtools.ui.dashboard.tools

import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import org.ccci.gto.android.common.kotlin.coroutines.flow.combineTransformLatest
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Tool

internal class FeaturedToolsFlowProducer @Inject constructor(
    private val settings: Settings,
    private val toolsRepository: ToolsRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getFlow(): Flow<List<Tool>> = settings.appLanguageFlow
        .combineTransformLatest(settings.getPersonalizationCountryFlow()) { lang, country ->
            when (country) {
                // the featured tools section is hidden when no country has been selected
                null -> emit(emptyList())

                else -> emitAll(toolsRepository.getFeaturedToolsFlow(lang, country))
            }
        }
        .distinctUntilChanged()
        .map { it.filterNot { it.isHidden } }
}
