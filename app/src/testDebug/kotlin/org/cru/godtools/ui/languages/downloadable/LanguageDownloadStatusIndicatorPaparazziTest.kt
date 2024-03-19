package org.cru.godtools.ui.languages.downloadable

import androidx.compose.foundation.layout.Row
import kotlin.test.Test
import org.cru.godtools.ui.BasePaparazziTest

class LanguageDownloadStatusIndicatorPaparazziTest : BasePaparazziTest() {
    @Test
    fun `LanguageDownloadStatusIndicator()`() = centerInSnapshot {
        Row {
            LanguageDownloadStatusIndicator(
                isPinned = false,
                downloadedTools = 0,
                totalTools = 0,
                isConfirmRemoval = false
            )
            for (it in 0..5) {
                LanguageDownloadStatusIndicator(
                    isPinned = true,
                    downloadedTools = it,
                    totalTools = 5,
                    isConfirmRemoval = false
                )
            }
            LanguageDownloadStatusIndicator(
                isPinned = true,
                downloadedTools = 2,
                totalTools = 5,
                isConfirmRemoval = true
            )
        }
    }
}
