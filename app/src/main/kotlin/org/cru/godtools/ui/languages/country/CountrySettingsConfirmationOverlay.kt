package org.cru.godtools.ui.languages.country

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayNavigator
import org.cru.godtools.R

class CountrySettingsConfirmationOverlay(val countryName: String?) : Overlay<Boolean> {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navigator: OverlayNavigator<Boolean>) {
        BasicAlertDialog(
            onDismissRequest = { navigator.finish(false) },
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.TopEnd,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { navigator.finish(false) },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.country_setting_overlay_icon),
                        contentDescription = null,
                        Modifier
                            .align(Alignment.CenterHorizontally)
                    )
                    val fullText = stringResource(R.string.country_settings_overlay_text, countryName)
                    val start = fullText.indexOf(countryName)
                    val annotated = buildAnnotatedString {
                        append(fullText)
                        addStyle(
                            SpanStyle(color = MaterialTheme.colorScheme.primary),
                            start,
                            start + countryName.length
                        )
                    }
                    Text(annotated, modifier = Modifier.fillMaxWidth())
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { navigator.finish(false) },
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) {
                            Text(
                                stringResource(R.string.country_settings_overlay_dismiss)
                            )
                        }
                        Button(
                            onClick = { navigator.finish(true) },
                            shape = RoundedCornerShape(4.dp),
                        ) {
                            Text(stringResource(R.string.country_settings_overlay_confirm_selection))
                        }
                    }
                }
            }
        }
    }
}
