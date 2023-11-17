package org.cru.godtools.ui.languages.downloadable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun LanguageDownloadProgressIndicator(
    isPinned: Boolean,
    downloaded: Int,
    total: Int,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
) {
    val total = total.coerceAtLeast(0)
    val downloaded = downloaded.coerceIn(0, total)
    val contentModifier = modifier.size(iconSize)

    when {
        !isPinned -> Icon(
            Icons.Outlined.DownloadForOffline,
            null,
            modifier = contentModifier,
            tint = MaterialTheme.colorScheme.outline,
        )
        downloaded == total -> Icon(
            Icons.Outlined.CheckCircle,
            null,
            modifier = contentModifier,
            tint = MaterialTheme.colorScheme.primary,
        )
        else -> {
            val progress by animateFloatAsState(
                label = "Download Progress",
                targetValue = when (total) {
                    0 -> 1f
                    else -> downloaded.toFloat() / total
                },
            )

            val iconPadding = iconSize / 12
            CircularProgressIndicator(
                progress = progress,
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = (iconSize / 2) - iconPadding,
                modifier = contentModifier
                    .padding(iconPadding)
                    .border(iconSize / 12, MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}
