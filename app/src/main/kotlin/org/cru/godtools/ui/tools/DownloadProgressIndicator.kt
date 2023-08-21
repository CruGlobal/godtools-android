package org.cru.godtools.ui.tools

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.cru.godtools.downloadmanager.DownloadProgress

// TODO: this can be moved elsewhere when we need to use it outside of tool cards
@Composable
internal fun DownloadProgressIndicator(downloadProgress: () -> DownloadProgress?, modifier: Modifier = Modifier) {
    val hasProgress by remember { derivedStateOf { downloadProgress() != null } }
    val isIndeterminate by remember { derivedStateOf { downloadProgress()?.isIndeterminate == true } }

    if (hasProgress) {
        if (isIndeterminate) {
            LinearProgressIndicator(modifier = modifier)
        } else {
            val progress by animateFloatAsState(
                targetValue = downloadProgress()
                    ?.takeIf { it.max > 0 }
                    ?.let { it.progress.toFloat() / it.max }
                    ?.coerceIn(0f, 1f) ?: 0f,
                animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
            )
            LinearProgressIndicator(progress, modifier = modifier)
        }
    }
}
