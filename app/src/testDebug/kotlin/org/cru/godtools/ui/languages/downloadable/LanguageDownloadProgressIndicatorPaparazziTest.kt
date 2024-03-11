package org.cru.godtools.ui.languages.downloadable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.cash.paparazzi.Paparazzi
import kotlin.test.Test
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.junit.Rule

class LanguageDownloadProgressIndicatorPaparazziTest {
    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun `LanguageDownloadProgressIndicator()`() {
        paparazzi.snapshot {
            GodToolsTheme(disableDagger = true) {
                Box {
                    Row(Modifier.align(Alignment.Center)) {
                        for (it in 0..5) {
                            LanguageDownloadProgressIndicator(isPinned = true, downloaded = it, total = 5)
                        }
                    }
                }
            }
        }
    }
}
