package org.cru.godtools.ui.settings.country

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayNavigator
import org.ccci.gto.android.common.androidx.compose.ui.text.res.annotatedStringResource
import org.cru.godtools.R

class CountrySettingsConfirmationOverlay(val countryName: String?) : Overlay<Boolean> {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navigator: OverlayNavigator<Boolean>) {
        AlertDialog(
            onDismissRequest = { navigator.finish(false) },
            iconContentColor = MaterialTheme.colorScheme.primary,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_settings_country_globe),
                    contentDescription = null,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (countryName != null) {
                        Text(
                            annotatedStringResource(
                                R.string.country_settings_confirm_title,
                                buildAnnotatedString {
                                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                        append(countryName)
                                    }
                                }
                            )
                        )
                    } else {
                        Text(stringResource(R.string.country_settings_confirm_title_none))
                    }

                    Text(stringResource(R.string.country_settings_confirm_description))
                    Text(
                        stringResource(R.string.country_settings_confirm_note),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { navigator.finish(true) }) {
                    Text(stringResource(R.string.country_settings_confirm_action_continue))
                }
            },
            dismissButton = {
                TextButton(onClick = { navigator.finish(false) }) {
                    Text(stringResource(R.string.country_settings_confirm_action_dismiss))
                }
            },
        )
    }
}
