package org.cru.godtools.ui.dashboard.tools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.MutableStateFlow
import org.cru.godtools.ui.dashboard.tools.ToolFiltersStateProducer.Filters
import org.cru.godtools.ui.dashboard.tools.ToolsPresenter.UiState.Mode

class FakeToolFiltersStateProducer : ToolFiltersStateProducer {
    val filters = MutableStateFlow(Filters())
    var lastMode: Mode? = null

    @Composable
    override fun produce(mode: Mode): Filters {
        lastMode = mode
        return filters.collectAsState().value
    }
}
