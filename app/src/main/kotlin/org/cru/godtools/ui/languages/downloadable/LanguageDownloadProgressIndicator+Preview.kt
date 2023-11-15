package org.cru.godtools.ui.languages.downloadable

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.cru.godtools.base.ui.theme.GodToolsTheme

@Preview
@Composable
private fun LanguageDownloadProgressIndicatorNotPinned() {
    GodToolsTheme { LanguageDownloadProgressIndicator(isPinned = false, downloaded = 0, total = 5) }
}

@Preview
@Composable
private fun LanguageDownloadProgressIndicatorInProgress() {
    GodToolsTheme { LanguageDownloadProgressIndicator(isPinned = true, downloaded = 2, total = 5) }
}

@Preview
@Composable
private fun LanguageDownloadProgressIndicatorDownloaded() {
    GodToolsTheme { LanguageDownloadProgressIndicator(isPinned = true, downloaded = 5, total = 5) }
}
