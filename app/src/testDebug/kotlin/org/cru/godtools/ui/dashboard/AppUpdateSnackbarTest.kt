package org.cru.godtools.ui.dashboard

import android.app.Application
import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.excludeRecords
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertTrue
import org.cru.godtools.BuildConfig
import org.cru.godtools.R
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class AppUpdateSnackbarTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context: Context by lazy { ApplicationProvider.getApplicationContext() }
    private val appUpdateManager by lazy { FakeAppUpdateManager(context) }
    private val snackbarHostState: SnackbarHostState = mockk {
        excludeRecords { this@mockk.equals(any()) }
    }

    private fun composeAppUpdateSnackbar() = composeTestRule.setContent {
        CompositionLocalProvider(LocalAppUpdateManager provides appUpdateManager) {
            AppUpdateSnackbar(snackbarHostState)
        }
    }

    @Test
    fun `Update Not Available`() {
        appUpdateManager.setUpdateNotAvailable()
        composeAppUpdateSnackbar()

        composeTestRule.runOnIdle {
            coVerify { snackbarHostState wasNot Called }
        }
    }

    @Test
    fun `Update Available - Client version not stale`() {
        appUpdateManager.setUpdateAvailable(BuildConfig.VERSION_CODE + 1)
        appUpdateManager.setClientVersionStalenessDays(1)
        composeAppUpdateSnackbar()

        composeTestRule.runOnIdle {
            coVerify { snackbarHostState wasNot Called }
            confirmVerified(snackbarHostState)
        }
    }

    @Test
    fun `Update Available - Dismissed`() {
        coEvery { snackbarHostState.showSnackbar(any(), any(), any(), any()) } returns SnackbarResult.Dismissed
        appUpdateManager.setUpdateAvailable(BuildConfig.VERSION_CODE + 1)
        appUpdateManager.setClientVersionStalenessDays(14)
        composeAppUpdateSnackbar()

        composeTestRule.runOnIdle {
            coVerify {
                snackbarHostState.showSnackbar(
                    context.getString(R.string.play_update_available),
                    context.getString(R.string.play_update_available_action),
                    true,
                    SnackbarDuration.Indefinite
                )
            }
            confirmVerified(snackbarHostState)
        }
    }

    @Test
    fun `Update Available - Accepted - Default`() {
        coEvery { snackbarHostState.showSnackbar(any(), any(), any(), any()) } returns SnackbarResult.ActionPerformed
        appUpdateManager.setUpdateAvailable(BuildConfig.VERSION_CODE + 1)
        appUpdateManager.setClientVersionStalenessDays(14)
        composeAppUpdateSnackbar()

        composeTestRule.runOnIdle {
            coVerify {
                snackbarHostState.showSnackbar(
                    context.getString(R.string.play_update_available),
                    context.getString(R.string.play_update_available_action),
                    true,
                    SnackbarDuration.Indefinite
                )
            }
            confirmVerified(snackbarHostState)
            assertTrue(appUpdateManager.isConfirmationDialogVisible, "Default to Flexible Update")
        }
    }

    @Test
    fun `Update Available - Accepted - Flexible`() {
        coEvery { snackbarHostState.showSnackbar(any(), any(), any(), any()) } returns SnackbarResult.ActionPerformed
        appUpdateManager.setUpdateAvailable(BuildConfig.VERSION_CODE + 1, AppUpdateType.FLEXIBLE)
        appUpdateManager.setClientVersionStalenessDays(14)
        composeAppUpdateSnackbar()

        composeTestRule.runOnIdle {
            coVerify {
                snackbarHostState.showSnackbar(
                    context.getString(R.string.play_update_available),
                    context.getString(R.string.play_update_available_action),
                    true,
                    SnackbarDuration.Indefinite
                )
            }
            confirmVerified(snackbarHostState)
            assertTrue(appUpdateManager.isConfirmationDialogVisible)
        }
    }

    @Test
    fun `Update Available - Accepted - Immediate`() {
        coEvery { snackbarHostState.showSnackbar(any(), any(), any(), any()) } returns SnackbarResult.ActionPerformed
        appUpdateManager.setUpdateAvailable(BuildConfig.VERSION_CODE + 1, AppUpdateType.IMMEDIATE)
        appUpdateManager.setClientVersionStalenessDays(14)
        composeAppUpdateSnackbar()

        composeTestRule.runOnIdle {
            coVerify {
                snackbarHostState.showSnackbar(
                    context.getString(R.string.play_update_available),
                    context.getString(R.string.play_update_available_action),
                    true,
                    SnackbarDuration.Indefinite
                )
            }
            confirmVerified(snackbarHostState)
            assertTrue(appUpdateManager.isImmediateFlowVisible)
        }
    }
}
