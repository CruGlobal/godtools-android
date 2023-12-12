package org.cru.godtools.ui.tools

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.cru.godtools.R
import org.cru.godtools.model.getName

@Composable
internal fun FavoriteAction(
    viewModel: ToolViewModels.ToolViewModel,
    modifier: Modifier = Modifier,
    confirmRemoval: Boolean = true,
) {
    val tool by viewModel.tool.collectAsState()
    val translation by viewModel.firstTranslation.collectAsState()
    val eventSink: (ToolCard.Event) -> Unit = remember(viewModel) {
        {
            when (it) {
                ToolCard.Event.PinTool -> viewModel.pinTool()
                ToolCard.Event.UnpinTool -> viewModel.unpinTool()
                ToolCard.Event.OpenTool -> TODO()
                ToolCard.Event.OpenToolDetails -> TODO()
            }
        }
    }

    FavoriteAction(
        state = ToolCard.State(tool, translation = translation.value, eventSink = eventSink),
        modifier = modifier,
        confirmRemoval = confirmRemoval,
    )
}

@Composable
internal fun FavoriteAction(state: ToolCard.State, modifier: Modifier = Modifier, confirmRemoval: Boolean = true) {
    val tool by rememberUpdatedState(state.tool)
    val isFavorite by remember { derivedStateOf { tool?.isFavorite == true } }
    val eventSink by rememberUpdatedState(state.eventSink)

    var showRemovalConfirmation by rememberSaveable { mutableStateOf(false) }

    Surface(
        onClick = {
            when {
                !isFavorite -> eventSink(ToolCard.Event.PinTool)
                confirmRemoval -> showRemovalConfirmation = true
                else -> eventSink(ToolCard.Event.UnpinTool)
            }
        },
        shape = CircleShape,
        shadowElevation = 6.dp,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(
                if (isFavorite) R.drawable.ic_favorite_24dp else R.drawable.ic_favorite_border_24dp
            ),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 6.dp, horizontal = 5.dp, bottom = 4.dp)
                .size(18.dp)
        )
    }

    if (showRemovalConfirmation) {
        val translation by rememberUpdatedState(state.translation)

        AlertDialog(
            onDismissRequest = { showRemovalConfirmation = false },
            text = {
                Text(
                    stringResource(
                        R.string.tools_list_remove_favorite_dialog_title,
                        translation.getName(tool).orEmpty()
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        eventSink(ToolCard.Event.UnpinTool)
                        showRemovalConfirmation = false
                    }
                ) { Text(stringResource(R.string.tools_list_remove_favorite_dialog_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showRemovalConfirmation = false }) {
                    Text(stringResource(R.string.tools_list_remove_favorite_dialog_dismiss))
                }
            }
        )
    }
}
