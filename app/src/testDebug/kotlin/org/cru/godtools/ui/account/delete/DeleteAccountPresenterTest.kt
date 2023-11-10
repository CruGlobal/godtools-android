package org.cru.godtools.ui.account.delete

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertIs
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import org.cru.godtools.account.GodToolsAccountManager
import org.cru.godtools.ui.account.delete.DeleteAccountScreen.Event
import org.cru.godtools.ui.account.delete.DeleteAccountScreen.State
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class DeleteAccountPresenterTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val deleteAccountResponse = Channel<Boolean>()

    private val accountManager: GodToolsAccountManager = mockk {
        coEvery { deleteAccount() } coAnswers { deleteAccountResponse.receive() }
    }
    private val navigator = FakeNavigator()

    private val presenter = DeleteAccountPresenter(navigator, accountManager)

    @Test
    fun `Delete Account - succeeds`() = runTest {
        presenter.test {
            assertIs<State.Display>(awaitItem())
                .eventSink(Event.DeleteAccount)

            deleteAccountResponse.send(true)
            coVerify { accountManager.deleteAccount() }
            navigator.awaitPop()

            cancelAndIgnoreRemainingEvents()
        }

        confirmVerified(accountManager)
    }

    @Test
    fun `Delete Account - fails`() = runTest {
        presenter.test {
            assertIs<State.Display>(awaitItem())
                .eventSink(Event.DeleteAccount)

            deleteAccountResponse.send(false)
            coVerify { accountManager.deleteAccount() }

            assertIs<State.Error>(expectMostRecentItem())
                .eventSink(Event.ClearError)

            assertIs<State.Display>(awaitItem())
        }

        confirmVerified(accountManager)
    }

    @Test
    fun `Cancel Delete Account`() = runTest {
        coEvery { accountManager.deleteAccount() } returns true

        presenter.test {
            assertIs<State.Display>(awaitItem())
                .eventSink(Event.Close)
            navigator.awaitPop()
            coVerify { accountManager wasNot Called }
        }

        confirmVerified(accountManager)
    }
}
