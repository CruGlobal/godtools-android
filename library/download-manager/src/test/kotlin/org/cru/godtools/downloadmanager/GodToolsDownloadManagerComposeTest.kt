package org.cru.godtools.downloadmanager

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.excludeRecords
import io.mockk.mockk
import io.mockk.verify
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GodToolsDownloadManagerComposeTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val downloadManager: GodToolsDownloadManager = mockk {
        coEvery { downloadLatestPublishedTranslation(any(), any()) } returns true

        excludeRecords { this@mockk.equals(any()) }
    }

    // region DownloadLatestTranslation()
    @Test
    fun `DownloadLatestTranslation()`() {
        composeTestRule.setContent { downloadManager.DownloadLatestTranslation("kgp", Locale.ENGLISH, true) }

        composeTestRule.runOnIdle {
            coVerifyAll {
                downloadManager.downloadLatestPublishedTranslation("kgp", Locale.ENGLISH)
            }
        }
    }

    @Test
    fun `DownloadLatestTranslation() - null tool`() {
        composeTestRule.setContent { downloadManager.DownloadLatestTranslation(null, Locale.ENGLISH, true) }

        composeTestRule.runOnIdle {
            verify { downloadManager wasNot Called }
        }
    }

    @Test
    fun `DownloadLatestTranslation() - null locale`() {
        composeTestRule.setContent { downloadManager.DownloadLatestTranslation("kgp", null, true) }

        composeTestRule.runOnIdle {
            verify { downloadManager wasNot Called }
        }
    }

    @Test
    fun `DownloadLatestTranslation() - offline`() {
        composeTestRule.setContent { downloadManager.DownloadLatestTranslation("kgp", Locale.ENGLISH, false) }

        composeTestRule.runOnIdle {
            verify { downloadManager wasNot Called }
        }
    }

    @Test
    fun `DownloadLatestTranslation() - Triggers when going online`() {
        val isConnected = MutableStateFlow(false)
        composeTestRule.setContent {
            downloadManager.DownloadLatestTranslation("kgp", Locale.ENGLISH, isConnected.collectAsState().value)
        }

        composeTestRule.runOnIdle { verify { downloadManager wasNot Called } }

        isConnected.value = true
        composeTestRule.runOnIdle {
            coVerifyAll { downloadManager.downloadLatestPublishedTranslation("kgp", Locale.ENGLISH) }
        }

        isConnected.value = false
        composeTestRule.runOnIdle {
            coVerifyAll { downloadManager.downloadLatestPublishedTranslation("kgp", Locale.ENGLISH) }
        }

        isConnected.value = true
        composeTestRule.runOnIdle {
            coVerifyAll {
                downloadManager.downloadLatestPublishedTranslation("kgp", Locale.ENGLISH)
                downloadManager.downloadLatestPublishedTranslation("kgp", Locale.ENGLISH)
            }
        }
    }
    // endregion DownloadLatestTranslation()
}
