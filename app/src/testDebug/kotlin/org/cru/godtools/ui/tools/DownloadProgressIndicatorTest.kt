package org.cru.godtools.ui.tools

import android.app.Application
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import kotlin.test.assertEquals
import org.cru.godtools.downloadmanager.DownloadProgress
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class DownloadProgressIndicatorTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `ProgressIndicator - Hidden when not downloading`() {
        composeTestRule.setContent {
            DownloadProgressIndicator(downloadProgress = { null })
        }

        composeTestRule
            .onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
            .assertDoesNotExist()
    }

    @Test
    fun `ProgressIndicator - Shows Progress`() {
        composeTestRule.setContent {
            DownloadProgressIndicator(downloadProgress = { DownloadProgress(1, 4) })
        }

        val progressRangeInfo = composeTestRule
            .onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
            .assertExists()
            .fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo]
        assertEquals(0.25f, progressRangeInfo.current)
    }

    @Test
    fun `ProgressIndicator - Shows Indeterminate Progress`() {
        composeTestRule.setContent {
            DownloadProgressIndicator(downloadProgress = { DownloadProgress(1, 0) })
        }

        val progressRangeInfo = composeTestRule
            .onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
            .assertExists()
            .fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo]
        assertEquals(ProgressBarRangeInfo.Indeterminate, progressRangeInfo)
    }
}
