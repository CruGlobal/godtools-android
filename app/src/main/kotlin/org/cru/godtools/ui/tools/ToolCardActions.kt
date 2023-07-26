package org.cru.godtools.ui.tools

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.cru.godtools.R
import org.cru.godtools.ui.tools.ToolCardEvent.OpenTool as OpenToolEvent

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ToolCardActions(
    viewModel: ToolViewModels.ToolViewModel,
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier,
    buttonWeightFill: Boolean = true,
    onEvent: (ToolCardEvent) -> Unit = {},
) = Row(modifier = modifier) {
    val tool by viewModel.tool.collectAsState()
    val firstTranslation by viewModel.firstTranslation.collectAsState()
    val secondTranslation by viewModel.secondTranslation.collectAsState()

    val buttonContentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
    val buttonMinHeight = 30.dp

    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        OutlinedButton(
            onClick = { onEvent(ToolCardEvent.OpenToolDetails(tool)) },
            contentPadding = buttonContentPadding,
            modifier = buttonModifier
                .alignByBaseline()
                .heightIn(min = buttonMinHeight)
                .weight(1f, buttonWeightFill)
        ) {
            Text(
                stringResource(R.string.action_tools_about),
                style = MaterialTheme.typography.labelMedium
            )
        }
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = {
                onEvent(OpenToolEvent(tool, firstTranslation.value?.languageCode, secondTranslation?.languageCode))
            },
            contentPadding = buttonContentPadding,
            modifier = buttonModifier
                .alignByBaseline()
                .heightIn(min = buttonMinHeight)
                .weight(1f, buttonWeightFill)
        ) {
            Text(
                stringResource(R.string.action_tools_open),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
