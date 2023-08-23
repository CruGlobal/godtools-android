package org.cru.godtools.downloadmanager.compose

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyAll
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.flow.MutableStateFlow
import org.cru.godtools.base.BaseModule
import org.cru.godtools.downloadmanager.GodToolsDownloadManager
import org.cru.godtools.model.TranslationKey
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class DownloadLatestTranslationTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @BindValue
    val downloadManager: GodToolsDownloadManager = mockk {
        coEvery { downloadLatestPublishedTranslation(any()) } returns true
    }
    @Inject
    @Named(BaseModule.IS_CONNECTED_STATE_FLOW)
    lateinit var isConnected: MutableStateFlow<Boolean>

    @Before
    fun setupDependencies() {
        hiltRule.inject()
    }

    @Test
    fun `DownloadLatestTranslation()`() {
        composeTestRule.setContent { DownloadLatestTranslation("kgp", Locale.ENGLISH) }

        composeTestRule.runOnIdle {
            coVerifyAll {
                downloadManager.downloadLatestPublishedTranslation(TranslationKey("kgp", Locale.ENGLISH))
            }
        }
    }

    @Test
    fun `DownloadLatestTranslation() - null tool`() {
        composeTestRule.setContent { DownloadLatestTranslation(null, Locale.ENGLISH) }

        composeTestRule.runOnIdle {
            verify { downloadManager wasNot Called }
        }
    }

    @Test
    fun `DownloadLatestTranslation() - null locale`() {
        composeTestRule.setContent { DownloadLatestTranslation("kgp", null) }

        composeTestRule.runOnIdle {
            verify { downloadManager wasNot Called }
        }
    }

    @Test
    fun `DownloadLatestTranslation() - offline`() {
        isConnected.value = false
        composeTestRule.setContent { DownloadLatestTranslation("kgp", Locale.ENGLISH) }

        composeTestRule.runOnIdle {
            verify { downloadManager wasNot Called }
        }
    }

    @Test
    fun `DownloadLatestTranslation() - Triggers when going online`() {
        isConnected.value = false
        composeTestRule.setContent { DownloadLatestTranslation("kgp", Locale.ENGLISH) }
        composeTestRule.runOnIdle { verify { downloadManager wasNot Called } }

        isConnected.value = true
        composeTestRule.runOnIdle {
            coVerify(exactly = 1) {
                downloadManager.downloadLatestPublishedTranslation(TranslationKey("kgp", Locale.ENGLISH))
            }
            confirmVerified(downloadManager)
        }

        isConnected.value = false
        composeTestRule.runOnIdle {
            coVerify(exactly = 1) {
                downloadManager.downloadLatestPublishedTranslation(TranslationKey("kgp", Locale.ENGLISH))
            }
            confirmVerified(downloadManager)
        }

        isConnected.value = true
        composeTestRule.runOnIdle {
            coVerify(exactly = 2) {
                downloadManager.downloadLatestPublishedTranslation(TranslationKey("kgp", Locale.ENGLISH))
            }
            confirmVerified(downloadManager)
        }
    }
}
