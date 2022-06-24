package org.cru.godtools.ui.banner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.unit.dp

@Composable
internal fun Banner(
    text: String,
    primaryButton: String,
    modifier: Modifier = Modifier,
    primaryAction: () -> Unit = {},
    secondaryButton: String? = null,
    secondaryAction: () -> Unit = {}
) = Surface(modifier = modifier.fillMaxWidth()) {
    Column {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            Spacer(
                modifier = Modifier
                    .height(36.dp)
                    .alignBy { it.measuredHeight }
            )
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .alignBy(FirstBaseline)
            )
        }
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .padding(all = 8.dp)
                .fillMaxWidth()
        ) {
            if (secondaryButton != null) {
                TextButton(onClick = secondaryAction, modifier = Modifier.padding(end = 8.dp)) { Text(secondaryButton) }
            }
            TextButton(onClick = primaryAction) { Text(primaryButton) }
        }
        Divider(modifier = Modifier.alpha(0.12f))
    }
}
