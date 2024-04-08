package org.cru.godtools.downloadmanager.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import java.util.Locale
import org.cru.godtools.downloadmanager.GodToolsDownloadManager

@Composable
fun DownloadLatestTranslation(
    downloadManager: GodToolsDownloadManager,
    tool: String?,
    locale: Locale?,
    isConnected: Boolean,
) {
    if (tool == null || locale == null || !isConnected) return

    LaunchedEffect(downloadManager, tool, locale) {
        downloadManager.downloadLatestPublishedTranslation(tool, locale)
    }
}
