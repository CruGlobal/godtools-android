package org.cru.godtools.ui.account.delete

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.cru.godtools.R
import org.cru.godtools.ui.account.delete.DeleteAccountScreen.Event
import org.cru.godtools.ui.account.delete.DeleteAccountScreen.State

private val MARGIN_HORIZONTAL = 32.dp

internal const val TEST_TAG_ICON_CLOSE = "icon_close"
internal const val TEST_TAG_BUTTON_DELETE = "button_delete"
internal const val TEST_TAG_BUTTON_CANCEL = "button_cancel"
internal const val TEST_TAG_ERROR_DIALOG = "error_dialog"
internal const val TEST_TAG_ERROR_DIALOG_BUTTON_CONFIRM = "error_dialog_button_confirm"

@Composable
@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(DeleteAccountScreen::class, SingletonComponent::class)
fun DeleteAccountLayout(state: State, modifier: Modifier = Modifier) {
    DeleteAccountError(state)

    Scaffold(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = { state.eventSink(Event.Close) },
                        modifier = Modifier.testTag(TEST_TAG_ICON_CLOSE)
                    ) {
                        Icon(Icons.Default.Close, null)
                    }
                },
            )

            Spacer(Modifier.weight(1f))
            Image(
                painterResource(R.drawable.banner_account_login),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.weight(1f))

            Text(
                stringResource(R.string.account_delete_heading),
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier
                    .padding(horizontal = MARGIN_HORIZONTAL)
                    .align(Alignment.Start)
            )
            Text(
                stringResource(R.string.account_delete_description),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(horizontal = MARGIN_HORIZONTAL, top = 8.dp, bottom = 32.dp)
                    .align(Alignment.Start)
            )

            val actionsEnabled = state !is State.Deleting && state !is State.Error
            OutlinedButton(
                enabled = actionsEnabled,
                onClick = { state.eventSink(Event.DeleteAccount) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MARGIN_HORIZONTAL)
                    .testTag(TEST_TAG_BUTTON_DELETE)
            ) {
                Text(stringResource(R.string.account_delete_action_delete))
            }
            Button(
                enabled = actionsEnabled,
                onClick = { state.eventSink(Event.Close) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MARGIN_HORIZONTAL)
                    .testTag(TEST_TAG_BUTTON_CANCEL)
            ) {
                Text(stringResource(R.string.account_delete_action_cancel))
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DeleteAccountError(state: State) {
    if (state is State.Error) {
        AlertDialog(
            text = { Text(stringResource(R.string.account_delete_error)) },
            confirmButton = {
                TextButton(
                    onClick = { state.eventSink(Event.ClearError) },
                    modifier = Modifier.testTag(TEST_TAG_ERROR_DIALOG_BUTTON_CONFIRM),
                ) {
                    Text(stringResource(R.string.account_delete_error_dismiss))
                }
            },
            onDismissRequest = { state.eventSink(Event.ClearError) },
            modifier = Modifier.testTag(TEST_TAG_ERROR_DIALOG),
        )
    }
}
