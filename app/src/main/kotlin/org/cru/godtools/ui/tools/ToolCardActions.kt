package org.cru.godtools.ui.tools

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.cru.godtools.R
import org.cru.godtools.ui.tools.ToolCardPresenter.ToolCardEvent
import org.cru.godtools.ui.tools.ToolCardPresenter.UiState

@Composable
internal fun ToolCardActions(
    state: UiState,
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier,
    buttonWeightFill: Boolean = true,
) = Row(modifier = modifier) {
    val buttonContentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
    val buttonMinHeight = 30.dp

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides buttonMinHeight) {
        OutlinedButton(
            onClick = { state.eventSink(ToolCardEvent.OpenToolDetails) },
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
            onClick = { state.eventSink(ToolCardEvent.OpenTool) },
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
