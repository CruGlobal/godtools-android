package org.cru.godtools.ui.account.delete

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.cru.godtools.account.GodToolsAccountManager
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class DeleteAccountPresenterTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val accountManager: GodToolsAccountManager = mockk()
    private val navigator = FakeNavigator()

    private val presenter = DeleteAccountPresenter(navigator, accountManager)

    @Test
    fun `State Display - Event DeleteAccount - succeeds`() = runTest {
        coEvery { accountManager.deleteAccount() } returns true

        presenter.test {
            awaitItem().eventSink(DeleteAccountScreen.Event.DeleteAccount)
            navigator.awaitPop()
        }

        coVerifyAll {
            accountManager.deleteAccount()
        }
    }

    @Test
    fun `State Display - Event DeleteAccount - fails`() = runTest {
        coEvery { accountManager.deleteAccount() } returns false

        presenter.test {
            awaitItem().eventSink(DeleteAccountScreen.Event.DeleteAccount)

            // TODO: there is currently no way to assert that the navigator wasn't popped
        }

        coVerifyAll {
            accountManager.deleteAccount()
        }
    }

    @Test
    fun `State Display - Event Cancel`() = runTest {
        coEvery { accountManager.deleteAccount() } returns true

        presenter.test {
            awaitItem().eventSink(DeleteAccountScreen.Event.Cancel)
            navigator.awaitPop()
        }

        coVerifyAll {
            accountManager wasNot Called
        }
    }
}
