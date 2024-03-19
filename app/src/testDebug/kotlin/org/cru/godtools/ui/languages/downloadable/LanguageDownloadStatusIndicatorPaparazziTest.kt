package org.cru.godtools.ui.languages.downloadable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.cash.paparazzi.Paparazzi
import kotlin.test.Test
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.junit.Rule

class LanguageDownloadStatusIndicatorPaparazziTest {
    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun `LanguageDownloadStatusIndicator()`() {
        paparazzi.snapshot {
            GodToolsTheme {
                Box {
                    Row(Modifier.align(Alignment.Center)) {
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
        }
    }
}
