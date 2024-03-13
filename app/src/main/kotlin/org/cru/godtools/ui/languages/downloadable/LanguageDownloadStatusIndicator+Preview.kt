package org.cru.godtools.ui.languages.downloadable

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.cru.godtools.base.ui.theme.GodToolsTheme

@Preview
@Composable
private fun LanguageDownloadStatusIndicatorNotPinned() {
    GodToolsTheme {
        LanguageDownloadStatusIndicator(
            isPinned = false,
            downloadedTools = 0,
            totalTools = 5,
            isConfirmRemoval = false
        )
    }
}

@Preview
@Composable
private fun LanguageDownloadStatusIndicatorInProgress() {
    GodToolsTheme {
        LanguageDownloadStatusIndicator(
            isPinned = true,
            downloadedTools = 2,
            totalTools = 5,
            isConfirmRemoval = false
        )
    }
}

@Preview
@Composable
private fun LanguageDownloadStatusIndicatorDownloaded() {
    GodToolsTheme {
        LanguageDownloadStatusIndicator(
            isPinned = true,
            downloadedTools = 5,
            totalTools = 5,
            isConfirmRemoval = false
        )
    }
}

@Preview
@Composable
private fun LanguageDownloadStatusIndicatorConfirmRemoval() {
    GodToolsTheme {
        LanguageDownloadStatusIndicator(
            isPinned = true,
            downloadedTools = 5,
            totalTools = 5,
            isConfirmRemoval = true
        )
    }
}
