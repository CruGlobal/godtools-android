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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.cru.godtools.R
import org.cru.godtools.ui.tools.ToolCardEvent.OpenTool as OpenToolEvent

@Composable
internal fun ToolCardActions(
    viewModel: ToolViewModels.ToolViewModel,
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier,
    buttonWeightFill: Boolean = true,
    onEvent: (ToolCardEvent) -> Unit = {},
) {
    val tool by viewModel.tool.collectAsState()
    val firstTranslation by viewModel.firstTranslation.collectAsState()
    val secondTranslation by viewModel.secondTranslation.collectAsState()
    val onEvent by rememberUpdatedState(onEvent)
    val state = remember(viewModel) {
        ToolCard.State(
            eventSink = {
                when (it) {
                    ToolCard.Event.OpenTool -> onEvent(
                        OpenToolEvent(
                            tool = tool?.code,
                            type = tool?.type,
                            lang1 = firstTranslation.value?.languageCode,
                            lang2 = secondTranslation?.languageCode
                        )
                    )
                    ToolCard.Event.OpenToolDetails -> onEvent(ToolCardEvent.OpenToolDetails(tool?.code))
                    ToolCard.Event.Click -> TODO()
                    ToolCard.Event.PinTool -> TODO()
                    ToolCard.Event.UnpinTool -> TODO()
                }
            }
        )
    }

    ToolCardActions(
        state = state,
        modifier = modifier,
        buttonModifier = buttonModifier,
        buttonWeightFill = buttonWeightFill,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ToolCardActions(
    state: ToolCard.State,
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier,
    buttonWeightFill: Boolean = true,
) = Row(modifier = modifier) {
    val eventSink by rememberUpdatedState(state.eventSink)

    val buttonContentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
    val buttonMinHeight = 30.dp

    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        OutlinedButton(
            onClick = { eventSink(ToolCard.Event.OpenToolDetails) },
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
            onClick = { eventSink(ToolCard.Event.OpenTool) },
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
