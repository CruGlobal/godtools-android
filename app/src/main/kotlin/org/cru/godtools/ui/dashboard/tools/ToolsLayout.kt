package org.cru.godtools.ui.dashboard.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.cru.godtools.R
import org.cru.godtools.ui.tools.PreloadTool
import org.cru.godtools.ui.tools.SquareToolCard

@Composable
fun ToolSpotlight(
    viewModel: ToolsFragmentDataModel = viewModel(),
    modifier: Modifier = Modifier,
    onOpenToolDetails: (String) -> Unit = {}
) = Column(modifier = modifier.fillMaxWidth()) {
    val spotlightTools by viewModel.spotlightTools.collectAsState()

    Text(
        stringResource(R.string.dashboard_tools_section_spotlight_label),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
    Text(
        stringResource(R.string.dashboard_tools_section_spotlight_description),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .padding(top = 4.dp, horizontal = 16.dp)
            .fillMaxWidth()
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
    ) {
        items(spotlightTools, key = { it.code.orEmpty() }) {
            PreloadTool(it)

            SquareToolCard(
                toolCode = it.code.orEmpty(),
                showCategory = false,
                showActions = false,
                floatParallelLanguageUp = false,
                confirmRemovalFromFavorites = false,
                onClick = { tool, _, _ -> tool?.code?.let(onOpenToolDetails) }
            )
        }
    }
}
