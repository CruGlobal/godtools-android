package org.cru.godtools.ui.languages.downloadable

import androidx.compose.foundation.layout.Row
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.Test
import org.cru.godtools.ui.BasePaparazziTest
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class LanguageDownloadStatusIndicatorPaparazziTest(
    @TestParameter nightMode: NightMode
) : BasePaparazziTest(nightMode = nightMode) {
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
