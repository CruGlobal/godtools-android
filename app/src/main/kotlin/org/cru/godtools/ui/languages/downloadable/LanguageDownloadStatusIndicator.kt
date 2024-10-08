package org.cru.godtools.ui.languages.downloadable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.cru.godtools.base.ui.theme.GodToolsTheme

private val DEFAULT_ICON_SIZE = 24.dp

@Composable
internal fun LanguageDownloadStatusIndicator(
    isPinned: Boolean,
    downloadedTools: Int,
    totalTools: Int,
    isConfirmRemoval: Boolean,
    modifier: Modifier = Modifier,
) {
    val total = totalTools.coerceAtLeast(0)
    val downloaded = downloadedTools.coerceIn(0, total)

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        propagateMinConstraints = true,
        modifier = modifier
            .size(DEFAULT_ICON_SIZE)
            .aspectRatio(1f)
    ) {
        when {
            !isPinned -> Icon(
                Icons.Outlined.DownloadForOffline,
                null,
                tint = MaterialTheme.colorScheme.outline,
            )
            isConfirmRemoval -> Icon(
                Icons.Outlined.Cancel,
                null,
                tint = GodToolsTheme.GT_RED,
            )
            downloaded == total -> Icon(
                Icons.Outlined.CheckCircle,
                null,
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

                val size = with(LocalDensity.current) { constraints.maxWidth.toDp() }
                val iconPadding = size / 12
                CircularProgressIndicator(
                    progress = { progress },
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = (size / 2) - iconPadding,
                    trackColor = Color.Transparent,
                    strokeCap = StrokeCap.Butt,
                    modifier = Modifier
                        .padding(iconPadding)
                        .border(size / 12, MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }
    }
}
