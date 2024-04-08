package org.cru.godtools.ui.drawer

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.test
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.cru.godtools.TestUtils.clearAndroidUiDispatcher
import org.cru.godtools.account.GodToolsAccountManager
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class DrawerMenuPresenterTest {
    private val isAuthenticatedFlow = MutableStateFlow(false)

    private val accountManager: GodToolsAccountManager = mockk {
        every { isAuthenticatedFlow } returns this@DrawerMenuPresenterTest.isAuthenticatedFlow
    }

    private val presenter = DrawerMenuPresenter(
        accountManager = accountManager
    )

    @AfterTest
    fun cleanup() = clearAndroidUiDispatcher()

    @Test
    fun `State - isLoggedIn`() = runTest {
        presenter.test {
            assertFalse(expectMostRecentItem().isLoggedIn)

            isAuthenticatedFlow.value = true
            assertTrue(expectMostRecentItem().isLoggedIn)
        }
    }

    @Test
    fun `Event - Logout`() = runTest {
        isAuthenticatedFlow.value = true
        coEvery { accountManager.logout() } just Runs

        presenter.test {
            expectMostRecentItem().eventSink(DrawerMenuScreen.Event.Logout)
            coVerify { accountManager.logout() }
        }
    }
}
