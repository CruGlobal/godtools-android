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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

private val MARGIN_HORIZONTAL = 32.dp

internal const val TEST_TAG_ICON_CLOSE = "icon_close"
internal const val TEST_TAG_BUTTON_DELETE = "button_delete"
internal const val TEST_TAG_BUTTON_CANCEL = "button_cancel"

@Composable
@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(DeleteAccountScreen::class, SingletonComponent::class)
fun DeleteAccountLayout(state: DeleteAccountScreen.State, modifier: Modifier = Modifier) {
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
                        onClick = { state.eventSink(DeleteAccountScreen.Event.Cancel) },
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
                modifier = Modifier.padding(horizontal = MARGIN_HORIZONTAL, top = 8.dp, bottom = 32.dp)
            )

            OutlinedButton(
                onClick = { state.eventSink(DeleteAccountScreen.Event.DeleteAccount) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MARGIN_HORIZONTAL)
                    .testTag(TEST_TAG_BUTTON_DELETE)
            ) {
                Text(stringResource(R.string.account_delete_action_delete))
            }
            Button(
                onClick = { state.eventSink(DeleteAccountScreen.Event.Cancel) },
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
