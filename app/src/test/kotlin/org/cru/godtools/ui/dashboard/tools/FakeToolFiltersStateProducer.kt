package org.cru.godtools.ui.dashboard.tools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.MutableStateFlow
import org.cru.godtools.ui.dashboard.tools.ToolFiltersStateProducer.Filters

class FakeToolFiltersStateProducer : ToolFiltersStateProducer {
    val filters = MutableStateFlow(Filters())

    @Composable
    override fun produce() = filters.collectAsState().value
}
