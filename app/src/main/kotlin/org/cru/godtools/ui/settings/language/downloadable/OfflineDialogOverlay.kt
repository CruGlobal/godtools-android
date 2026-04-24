package org.cru.godtools.ui.settings.language.downloadable

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayNavigator
import org.cru.godtools.R

class OfflineDialogOverlay : Overlay<Unit> {
    @Composable
    override fun Content(navigator: OverlayNavigator<Unit>) {
        AlertDialog(
            onDismissRequest = { navigator.finish(Unit) },
            title = {
                Text(
                    stringResource(R.string.language_settings_downloadable_languages_offline_notification_title)
                )
            },
            text = {
                Text(
                    stringResource(R.string.language_settings_downloadable_languages_offline_notification_message)
                )
            },
            confirmButton = {
                TextButton(onClick = { navigator.finish(Unit) }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
        )
    }
}
